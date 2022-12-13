package _0.playground.kvs;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0.Jdbc;
import _0._0;
import _0.debug.StopWatch;
import _0.playground.udf.XFuncPlugin;

public final class Kvs implements Flushable, Closeable {

	private static final Logger log = LoggerFactory.getLogger(Kvs.class);

	private static final String name = Kvs.class.getSimpleName().toLowerCase();

	private static final Path dbfile = Path.of(".").resolve(name + ".db");

	private static final int cachesize = 8192; // Jdbc.fetchsize;

	private static final Map<String, Map<String, Map<String, Object>>> cache = new HashMap<>();

	private static final String default_table = "default";

	// TODO: private
	public Jdbc jdbc = null;

	// TODO: private
	public Connection con = null;

	public Kvs()
			throws SQLException {

		this.jdbc = new Jdbc("sqlite").file(dbfile);
		this.con  = jdbc.connect();

		XFuncPlugin.load(con);

	}

	private void create_table(String name)
			throws SQLException {

//		Jdbc.execute(con, "DROP TABLE IF EXISTS " + Jdbc.esc(con, name));

		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE IF NOT EXISTS " + Jdbc.esc(con, name) + " ( ");
		query.append("   ins_date TEXT NOT NULL ");
		query.append("  ,upd_date TEXT NOT NULL ");
		query.append("  ,key      TEXT NOT NULL ");
		query.append("  ,val      TEXT ");
		query.append("  ,PRIMARY KEY (key) ");
		query.append(")");
		Jdbc.execute(con, query.toString());

	}

	public void set(final String key)
			throws IOException, SQLException {
		set(default_table, key, null);
	}

	public void set(final String key, final Map<String, Object> val)
			throws IOException, SQLException {
		set(default_table, key, null);
	}

	public void set(final String table, final String key)
			throws IOException, SQLException {
		set(table, key, null);
	}

	public void set(final String table, final String key, final Map<String, Object> val)
			throws IOException, SQLException {

		String now = _0.ymdhmss();

		Map<String, Object> rec = new HashMap<>();
		rec.put("ins_date", now);
		rec.put("upd_date", now);
		rec.put("val",      val);

		synchronized (cache) {

		}
		Map<String, Map<String, Object>> recs = _0.select(cache, table);
		if (null == recs) {
			recs = new HashMap<>();
			cache.put(table, recs);
		}

		log.trace("kvs table={} rec={}", table, rec);
		recs.put(key, rec);

		int sum = cache.values().stream()
				.parallel()
				.map(Map::values)
				.mapToInt(Collection::size)
				.sum();

		if (cachesize <= sum) {
			flush();
		}

	}

	public Map<String, Object> get(final String key)
			throws SQLException {
		return get(default_table, key);
	}

	public synchronized Map<String, Object> get(final String table, final String key)
			throws SQLException {

		Map<String, Object> ret = null;

		Map<String, Object> rec = _0.select(cache, table, key);
		if (null != rec) {

			ret = new HashMap<>();
			ret.putAll(rec);
			ret.put("key", key);

		} else {

			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append("    * ");
			query.append("  FROM ");
			query.append("    " + Jdbc.esc(con, table) + " ");
			query.append("  WHERE ");
			query.append("    key = ? ");

			try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

				stmt.setObject(1, key);

				try (ResultSet rs = stmt.executeQuery()) {

					if (rs.next()) {
						ret = Jdbc.map(rs);
					}

					String val = _0.select(ret, "val");
					if (null != val) {
						ret.put("val", new JSONObject(val).toMap());
					}

					if (rs.next()) {
						throw new SQLException(key);
					}

				}

			}

		}

		return ret;

	}

	public void vacuum()
			throws IOException, SQLException {

		flush();

		Jdbc.execute(con, "VACUUM");

	}

	@Override
	public synchronized void flush()
			throws IOException {

		Iterator<Entry<String, Map<String, Map<String, Object>>>> ite = cache.entrySet().iterator();
		while (!_0.empty(ite)) {

			Entry<String, Map<String, Map<String, Object>>> entry = ite.next();
			ite.remove();

			try {
				flush(entry.getKey(), entry.getValue());
			} catch (SQLException e) {
				throw new IOException(e);
			}

		}

	}

	private void flush(final String table, final Map<String, Map<String, Object>> recs)
			throws SQLException {

		if (!recs.isEmpty()) {

			create_table(table);

			StringBuilder query  = new StringBuilder();
			query.append("INSERT INTO ");
			query.append("    " + Jdbc.esc(con, table) + " ");
			query.append("  VALUES ");
			for (int i = 0; i < recs.size(); i++) { // TODO: bulksize limit
				query.append(i == 0 ? "" : ",");
				query.append("(?,?,?,?)");
			}
			query.append("  ON CONFLICT ");
			query.append("    (key) ");
			query.append("  DO UPDATE SET ");
			query.append("     upd_date = excluded.upd_date ");
			query.append("    ,val      = excluded.val "); // TODO: merge json

			List<Object> params = new LinkedList<>();
			Iterator<Entry<String, Map<String, Object>>> ite = recs.entrySet().iterator();
			while (ite.hasNext()) {

				Entry<String, Map<String, Object>> entry = ite.next();
				ite.remove();

				String              ins_key  = entry.getKey();
				Map<String, Object> ins_vals = entry.getValue();

				params.add(ins_vals.get("ins_date"));
				params.add(ins_vals.get("upd_date"));
				params.add(ins_key);
				params.add(ins_vals.get("val"));

			}

			try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

				int idx = 1;
				while (!params.isEmpty()) {
					stmt.setObject(idx++, params.remove(0));
				}

				stmt.executeUpdate();

			}

		}

	}

	@Override
	public void close()
			throws IOException {

		flush();

		_0.close(con);

	}

	public void output(final String table, final Path file)
			throws IOException, SQLException {
		output(table, file, CSVFormat.TDF.builder().setRecordSeparator('\n'));
	}

	public void output(final String table, final Path file, final Builder builder)
			throws IOException, SQLException {

		StopWatch sw = new StopWatch();

		Files.createDirectories(file.getParent());

		try (Statement stmt = con.createStatement()) {

			stmt.setFetchSize(Jdbc.fetchsize);

			try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + Jdbc.esc(con, table)); CSVPrinter ptr = builder.build().printer()) {

				ptr.printHeaders(rs);
				ptr.printRecords(rs);

				_0.flush(ptr);

			}

		}

		log.debug("output time={} {}", sw.stop(), table);

	}

}
