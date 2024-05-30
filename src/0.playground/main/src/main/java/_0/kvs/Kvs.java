package _0.kvs;

import java.io.Flushable;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.Function;

import _0.kvs.udf.Eq;
import _0.kvs.udf.Getenv;
import _0.kvs.udf.Matches;
import _0.kvs.udf.Nvl;
import _0.kvs.udf.Replace;
import _0.kvs.udf.Sha256;
import _0.kvs.udf.Split;
import _0.playground.core.Jdbc;
import _0.playground.core.Regex;
import _0.playground.core._0;
import _0.playground.debug.StopWatch;

public final class Kvs implements Flushable, AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(Kvs.class);

	private static final long timeout = 5 * 60 * 1000;

	private Connection con = null;

	private int         bulkmax         = -1;
	private List<Entry> entry_cache_upd = null;
	private List<Entry> entry_cache_del = null;

	public Kvs(final String dbfile)
			throws ReflectiveOperationException, IOException, SQLException {
		this(Path.of(dbfile));
	}

	public Kvs(final Path dbfile)
			throws ReflectiveOperationException, IOException, SQLException {

		con = new Jdbc("sqlite").path(dbfile).connect();

		bulkmax         = Math.min(Jdbc.bulksize(con, 2), 0x2000); // SQLITE_TOOBIG
		entry_cache_upd = Collections.synchronizedList(new LinkedList<>());
		entry_cache_del = Collections.synchronizedList(new LinkedList<>());

		udf(Eq.class);
		udf(Getenv.class);
		udf(Nvl.class);
		udf(Matches.class);
		udf(Replace.class);
		udf(Sha256.class);
		udf(Split.class);
		udf("json_merge", new Function() {

			@Override
			protected final void xFunc()
					throws SQLException {

				try {

					Map<String, Object> json1  = _0.json(value_text(0));
					Map<String, Object> json2  = _0.json(value_text(1));
					Map<String, Object> merged = _0.merge(json1, json2);

					result(_0.json(merged));

				} catch (IOException e) {
					throw new SQLException(e);
				}

			}

		});

		execute("PRAGMA busy_timeout = " + timeout);
//		execute("DROP TABLE IF EXISTS kvs");
		execute("""
CREATE TABLE IF NOT EXISTS kvs (

   ins TEXT NOT NULL
  ,upd TEXT NOT NULL

  ,key TEXT NOT NULL
  ,val JSON

  ,PRIMARY KEY (key)

)
""");

	}

	private <T extends Function> void udf(final Class<T> clazz)
			throws ReflectiveOperationException, SQLException {

		@SuppressWarnings("unchecked")
		T      impl = (T)clazz.getConstructors()[0].newInstance();
		String name = clazz.getSimpleName().toLowerCase();

		udf(name, impl);

	}

	private synchronized void udf(final String name, final Function impl)
			throws SQLException {
		Function.create(con, name, impl);
	}

	private synchronized void execute(final String queries)
			throws SQLException {

		try (Statement stmt = con.createStatement()) {
			for (String query : queries.split(";")) {

				StopWatch sw = new StopWatch();

				stmt.executeUpdate(query);

				log.trace("{} {}", sw.stop(), _0.trim(query.replaceAll("--[^\\r\\n]*[\\r\\n]+", "").replaceAll(Regex.spaces, " ").replaceAll(" ,", ", ")));

			}
		}

	}

	public synchronized Entry get(final String key)
			throws IOException, SQLException {

		Entry entry = null;

		try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM kvs WHERE key = ?")) {

			stmt.setFetchSize(1);
			Jdbc.bind(stmt, key);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					entry = Entry.of(rs);
				}
			}

		}

		return entry;

	}

	public synchronized List<Entry> rand(int size)
			throws IOException, SQLException {

		List<Entry> entries = new LinkedList<>();

		String query = """
WITH random_id(id) AS (
            SELECT ABS(RANDOM() % (SELECT MAX(rowid) FROM kvs))
  UNION ALL SELECT ABS(RANDOM() % (SELECT MAX(rowid) FROM kvs)) FROM random_id
)
SELECT * FROM kvs
  INNER JOIN
    (SELECT DISTINCT id AS rid FROM random_id LIMIT ?)
      ON kvs.rowid == rid;
""";

		try (PreparedStatement stmt = con.prepareStatement(query)) {

			stmt.setFetchSize(size);
			Jdbc.bind(stmt, size);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					entries.add(Entry.of(rs));
				}
			}

		}

		return entries;

	}

	public void add(final Entry entry) {
		entry_cache_upd.add(entry);
	}

	public void del(final Entry entry) {
		entry_cache_del.add(entry);
	}

	public synchronized long size()
			throws SQLException {

		long size = 0;

//		try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT max(rowid) AS cnt FROM kvs")) {
		try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT count(1)   AS cnt FROM kvs")) {
			if (rs.next()) {
				size = rs.getLong("cnt");
			}
		}

		return size;

	}

	public synchronized void vacuum()
			throws SQLException {

		try (Statement stmt = con.createStatement()) {
			stmt.execute("VACUUM");
		}

	}

	@Override
	public void flush()
			throws IOException {
		try {
			del();
			upd();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	private synchronized void del()
			throws SQLException {

		int rowcnt = entry_cache_del.size();
		if (0 < rowcnt) {

			StringBuilder query = new StringBuilder();
			query.append("DELETE FROM kvs WHERE key IN (");
			for (int i = 0; i < rowcnt; i++) {
				query.append(0 == i ? "" : ",");
				query.append("?");
			}
			query.append(")");

			try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

				for (int i = 0; i < rowcnt; i++) {
					Entry entry = entry_cache_del.remove(0);
					stmt.setObject(1 + i, entry.key());
				}

				stmt.executeUpdate();

			}

		}

	}

	private synchronized void upd()
			throws IOException, SQLException {

		int rowcnt = Math.min(entry_cache_upd.size(), bulkmax);
		if (0 < rowcnt) {

			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO kvs VALUES ");
			for (int i = 0; i < rowcnt; i++) {
				query.append(0 == i ? "" : ",");
				query.append("(datetime('now', 'localtime'), datetime('now', 'localtime'), ?, ?)");
			}
			query.append("  ON CONFLICT (key) DO UPDATE SET ");
			query.append("     upd = excluded.upd ");
			query.append("    ,val = json_merge(val, excluded.val) ");

			try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

				for (int i = 0; i < rowcnt; i++) {
					Entry entry = entry_cache_upd.remove(0);
					stmt.setObject(i * 2 + 1, entry.key());
					stmt.setObject(i * 2 + 2, _0.json(entry.val()));
				}

				stmt.executeUpdate();

			}

		}

	}

	@Override
	public synchronized void close() {
		_0.close(con);
	}

}
