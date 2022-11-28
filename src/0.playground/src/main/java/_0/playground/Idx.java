package _0.playground;

import java.io.Closeable;
import java.io.FileFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.impl.UnsupportedCodecException;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import _0.Jdbc;
import _0._0;

public final class Idx implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(Idx.class);

	private static final String name = Idx.class.getSimpleName().toLowerCase();

	private static final String dbfile = name + ".db";

	private static final Jdbc jdbc = new Jdbc("sqlite").file(dbfile);

	private static final Map<String, Map<String, Object>> cache = new HashMap<>();

	public static boolean ip = true;

	private Connection con = null;

	public Idx()
			throws SQLException {

		con = jdbc.connect();

//		execute("DROP TABLE IF EXISTS " + name);

		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE IF NOT EXISTS " + name + " ( ");
		query.append("   ins_date TEXT NOT NULL ");
		query.append("  ,upd_date TEXT NOT NULL ");
		query.append("  ,type     TEXT NOT NULL ");
		query.append("  ,key      TEXT NOT NULL ");
		query.append("  ,val      TEXT ");
		query.append("  ,PRIMARY KEY (key) ");
		query.append(")");
		execute(query);

		execute("DROP INDEX IF EXISTS [idx/idx1]");
		execute("CREATE INDEX [idx/idx1] ON idx (type, key)");

		List<String> types = new LinkedList<>();
		types.add("file");
		types.add("table");
		for (String type : types) {

			execute("DROP VIEW IF EXISTS [idx/" + type + "]");

			query.setLength(0);
			query.append("CREATE VIEW [idx/" + type + "] AS ");
			query.append("  SELECT ");
			query.append("       key ");
			query.append("      ,val ");
			query.append("    FROM ");
			query.append("      idx ");
			query.append("    WHERE ");
			query.append("      type = '" + type + "' ");
			execute(query);

		}

		log.info("{} init.", name);

	}

	@Override
	public void close() {
		_0.close(con);
	}

	public static Jdbc jdbc() {
		return jdbc;
	}

	public void set(final String type, final String key)
			throws SQLException {
		set(type, key, (JSONObject)null);
	}

	public void set(final String type, final String key, final Map<String, Object> val)
			throws SQLException {
		set(type, key, new JSONObject(val));
	}

	public synchronized void set(final String type, final String key, final JSONObject val)
			throws SQLException {

		String now = _0.ymdhmss();

		Map<String, Object> map = new HashMap<>();
		map.put("ins_date", now);
		map.put("upd_date", now);
		map.put("type",     type);
		map.put("val",      val);

		cache.put(key, map);

		// TODO: cachesize
		if (8192 <= cache.size()) {
			flush();
		}

	}

	public synchronized String get(final String key)
			throws SQLException {

		String val = null;

		if (cache.containsKey(key)) {
			val = (String)cache.get(key).get("val");

		} else {

			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append("    val ");
			query.append("  FROM ");
			query.append("    idx ");
			query.append("  WHERE ");
			query.append("    key = ? ");

			try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

				stmt.setObject(1, key);

				try (ResultSet rs = stmt.executeQuery()) {

					if (rs.next()) {
						val = (String)rs.getObject("val");
					}

					if (rs.next()) {
						throw new SQLException(key);
					}

				}

			}

		}

		return val;

	}

//	public synchronized void del(final String key)
//			throws SQLException {
//
//		cache.put(key, null);
//
//		// TODO: cachesize
//		if (8192 <= cache.size()) {
//			flush();
//		}
//
//	}

	public synchronized void flush()
			throws SQLException {

		// delete
		{

			List<String> del_keys = new LinkedList<>();

			Iterator<Entry<String, Map<String, Object>>> ite = cache.entrySet().iterator();
			while (ite.hasNext()) {

				Entry<String, Map<String, Object>> entry = ite.next();

				String del_key = entry.getKey();
				Object del_rec = entry.getValue();

				if (null != del_rec) {
					continue;
				}

				del_keys.add(del_key);

				ite.remove();

			}

			if (!del_keys.isEmpty()) {

				// TODO: in limit
				StringBuilder query = new StringBuilder();
				query.append("DELETE ");
				query.append("  FROM ");
				query.append("    idx ");
				query.append("  WHERE ");
				query.append("    key IN (");
				for (int i = 0; i < del_keys.size(); i++) {
					query.append(0 == i ? "" : ",");
					query.append("?");
				}
				query.append(") ");

				try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

					int idx = 1;
					while (!del_keys.isEmpty()) {
						stmt.setObject(idx++, del_keys.remove(0));
					}

					stmt.executeUpdate();

				}

			}

		}

		// insert-update
		if (!cache.isEmpty()) {

			// TODO: bulksize limit
			StringBuilder query  = new StringBuilder();
			query.append("INSERT INTO ");
			query.append("    idx ");
			query.append("  VALUES ");
			for (int i = 0; i < cache.size(); i++) {
				query.append(i == 0 ? "" : ",");
				query.append("(?,?,?,?,?)");
			}
			query.append("  ON CONFLICT ");
			query.append("    (key) ");
			query.append("  DO UPDATE SET ");
			query.append("     upd_date = excluded.upd_date ");
			query.append("    ,type     = excluded.type ");
			query.append("    ,val      = excluded.val ");

			List<Object> params = new LinkedList<>();
			Iterator<Entry<String, Map<String, Object>>> ite = cache.entrySet().iterator();
			while (ite.hasNext()) {

				Entry<String, Map<String, Object>> entry = ite.next();
				ite.remove();

				String              ins_key = entry.getKey();
				Map<String, Object> ins_rec = entry.getValue();

				params.add(ins_rec.get("ins_date"));
				params.add(ins_rec.get("upd_date"));
				params.add(ins_rec.get("type"));
				params.add(ins_key);
				params.add(ins_rec.get("val"));

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

	public void vacuum()
			throws SQLException {
		flush();
		execute("VACUUM");
	}

	private void execute(final CharSequence query)
			throws SQLException {

		try (Statement stmt = con.createStatement()) {
			stmt.execute(query.toString());
		}

	}

	private static final String type(final Object type) {

		Integer key = Integer.valueOf(type.toString());

		Map<Integer, String> types = new HashMap<>();
		types.put(Types.BIT,           "bool");
		types.put(Types.BOOLEAN,       "bool");
		types.put(Types.TINYINT,       "int");
		types.put(Types.SMALLINT,      "int");
		types.put(Types.INTEGER,       "int");
		types.put(Types.BIGINT,        "int");
		types.put(Types.NUMERIC,       "int");
		types.put(Types.FLOAT,         "decimal");
		types.put(Types.DOUBLE,        "decimal");
		types.put(Types.REAL,          "decimal");
		types.put(Types.DECIMAL,       "decimal");
		types.put(Types.CHAR,          "text");
		types.put(Types.NCHAR,         "text");
		types.put(Types.VARCHAR,       "text");
		types.put(Types.NVARCHAR,      "text");
		types.put(Types.LONGVARCHAR,   "text");
		types.put(Types.LONGNVARCHAR,  "text");
		types.put(Types.TIMESTAMP,     "text");
		types.put(Types.DATE,          "text");
		types.put(Types.TIME,          "text");
		types.put(Types.BINARY,        "binary");
		types.put(Types.VARBINARY,     "binary");
		types.put(Types.LONGVARBINARY, "binary");
		types.put(Types.DISTINCT,      "unknown");
		types.put(Types.ARRAY,         "unknown");
		types.put(Types.OTHER,         "unknown");

		if (!types.containsKey(key)) {
			throw new UnsupportedOperationException(String.valueOf(key));
		}

		return types.get(key);

	}

	public void file(final InetAddress host, final Path path, final FileFilter filter)
			throws IOException {

		InetAddress host_ = null == host || host.getHostAddress().startsWith("127.") ? _0.ip() : host;

		Path uncpath = _0.uncpath(host, path);

		Files.walkFileTree(uncpath, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path uncpath, final BasicFileAttributes attrs)
					throws IOException {

				if (filter.accept(uncpath.toFile())) {

					String key      = uncpath.toString().replace('\\', '/');
					String hostpath = _0.hostpath(uncpath);

					try {

						Map<String, Object> val = new HashMap<>();
						val.put("host", host_.getHostAddress());
						val.put("path", hostpath);

						set("file", key, val);

					} catch (SQLException e) {
						log.trace("{}", uncpath, e);
					}

					String lower = key.toLowerCase();
					if (lower.endsWith(".mdb") || lower.endsWith(".accdb")) {

						try {

							table(host_, Path.of(hostpath));

						} catch (IOException e) {
							log.trace("{}", uncpath, e);

						} catch (SQLException e) {
							log.trace("{}", uncpath, e);
						}

					}

				}

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult visitFileFailed(final Path file, final IOException e)
					throws IOException {

				if (null == e) {
					// pass
				} else if (e instanceof AccessDeniedException) {
					log.trace("{}", e.toString());
				} else {
					log.trace("{}", file, e);
				}

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
					throws IOException {

				if (null != e) {
					log.trace("{}", dir, e);
				}

				return FileVisitResult.CONTINUE;

			}

		});

	}

	public void table(final Jdbc jdbc)
			throws IOException, SQLException {

		try (Connection con = jdbc.connect()) {
			table(jdbc, con);
		}

	}

	public void table(final Jdbc jdbc, final Connection con)
			throws IOException, SQLException {

		List<String> catalogs = new ArrayList<>();
		try {
			catalogs.addAll(Jdbc.catalogs(con).stream().map(e -> (String)e.get("TABLE_CAT")).toList());
		} catch (SQLException e) {
			log.trace("", e);
		}
		if (catalogs.isEmpty()) {
			catalogs.add(null);
		}

		for (String catalog : catalogs) {

			if (Jdbc.meta(con, catalog, null, null)) {
				continue;
			}

			List<String> schemas = new ArrayList<>();
			try {
//				schemas.addAll(Jdbc.schemas(con, catalog).stream().map(e -> (String)e.get("table_schem")).toList());
				schemas.addAll(Jdbc.schemas(con, catalog).stream().map(e -> (String)e.get("TABLE_SCHEM")).toList());
			} catch (SQLServerException e) {
				log.trace("{}", e.toString());
				continue;
			} catch (SQLFeatureNotSupportedException e) {
				// pass
			} catch (AbstractMethodError e) {
				// pass
			} catch (SQLException e) {
				log.trace("", e);
			}
			if (schemas.isEmpty()) {
				schemas.add(null);
			}

			for (String schema : schemas) {

				if (Jdbc.meta(con, catalog, schema, null)) {
					continue;
				}

				List<Map<String, Object>> tablemaps = new ArrayList<>();
				try {
					tablemaps.addAll(Jdbc.tables(con, catalog, schema));
				} catch (SQLException e) {
					log.trace("", e);
				}

				for (Map<String, Object> tablemap : tablemaps) {

					String catalog_ = _0.nvl(catalog, (String)tablemap.get("TABLE_CAT"));
					String schema_  = _0.nvl(schema,  (String)tablemap.get("TABLE_SCHEM"));
					String table_   = (String)tablemap.get("TABLE_NAME");

					if (Jdbc.meta(con, catalog_, schema_, table_)) {
						continue;
					}

					String tbl_key = null;
					if (Jdbc.sqlite(con)) {
						tbl_key = "//" + _0.ip().getHostAddress() + "/" + jdbc.file().toAbsolutePath() + "/" + table_;
					} else if (ip) {
						tbl_key = "//" + InetAddress.getByName(jdbc.host()).getHostAddress() + "/" + String.join("/", Arrays.asList(catalog_, schema_, table_).stream().filter(Objects::nonNull).toList());
					} else {
						tbl_key = "//" + jdbc.host() + "/" + String.join("/", Arrays.asList(catalog_, schema_, table_).stream().filter(Objects::nonNull).toList());
					}

					set("table", tbl_key, jdbc.attrs());

//					List<Map<String, Object>> columnmaps = Jdbc.columns(con, catalog_, schema_, table_);
//					for (Map<String, Object> columnmap : columnmaps) {
//
//						String column    = (String)columnmap.get("COLUMN_NAME");
//						int    data_type = _0.cast(int.class, columnmap.get("DATA_TYPE"));
//
//						String col_key = tbl_key + "/" + column;
//
//						set("column", col_key, jdbc.attrs());
//
//					}

				}

			}

		}

	}

	public void table(final InetAddress host, final Path path)
			throws IOException, SQLException {

		InetAddress host_ = null == host || host.getHostAddress().startsWith("127.") ? _0.ip() : host;

		Path uncpath = _0.uncpath(host_, path);

		// TODO: InputStream
		try (Database mdb = DatabaseBuilder.open(uncpath)) {

			Table sys_table = mdb.getSystemTable("MSysObjects");
			for (com.healthmarketscience.jackcess.Row sys_row : sys_table) {

				String name = (String)sys_row.get("Name");
				short  type = _0.cast(short.class, sys_row.get("Type"));

				if (1 != type) {
					continue;
				}
				if (name.matches("^f_[0-9A-F]{32}_Data$")) {
					continue;
				}
				if (name.matches("^~TMPCLP[0-9]+$")) {
					continue;
				}

				Table table = mdb.getTable(name);
				if (null == table) {
					continue;
				}

				String key = uncpath.toString().replace('\\', '/') + "/" + name;

				set("table", key);

//				List<? extends Column> columns = table.getColumns();
//				for (Column column : columns) {
//
//					String col_key = key_prefix + "/" + key(null, null, name, column.getName());
//
//					Map<String, Object> col_val = new HashMap<>();
//					col_val.put("type", type(column.getType().getSQLType()));
//
//					Main.set("column", col_key);
//
//				}

			}

			// クエリ抽出
//			Map<String, String> queries_mdb = new HashMap<>();
//			Map<String, String> queries_raw = new HashMap<>();
//			for (Query query : mdb.getQueries()) {
//
//				String name = query.getName();
//
//				if (Type.UNKNOWN == query.getType()) {
//
//					for (QueryImpl.Row row : ((QueryImpl)query).getRows()) {
//
//						String sql = row.expression;
//						if (null == sql) {
//							continue;
//						}
//
//						if (queries_mdb.containsKey(name) || queries_raw.containsKey(name)) {
//							log.debug("", new IOException("duplicated"));
//						}
//
//						queries_raw.put(name, sql);
//
//					}
//
//				} else {
//
//					if (queries_mdb.containsKey(name) || queries_raw.containsKey(name)) {
//						log.debug("", new IOException("duplicated"));
//					}
//
//					String sql = null;
//					try {
//						sql = query.toSQLString();
//					} catch (IllegalStateException e) {
//						sql = Utils.stackTrace(e);
//					}
//
//					queries_mdb.put(name, sql);
//
//				}
//
//			}

		} catch (UnsupportedCodecException e) {
			log.trace("{}", e.toString());
		}

	}

}
