package _0.playground.idx;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.impl.UnsupportedCodecException;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import _0.FileExtFilter;
import _0.Jdbc;
import _0._0;
import _0.debug.StopWatch;
import _0.playground.xfunc.XFuncPlugin;

public final class Idx implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(Idx.class);

	private static final String name = Idx.class.getSimpleName().toLowerCase();

	private static final Path dbfile = Path.of(".").resolve(name + ".db");

	private static final Map<String, Map<String, Object>> cache = new HashMap<>();

	public static boolean ip = true;

	private Jdbc jdbc = null;

	private Connection con = null;

	public Idx()
			throws SQLException {
		this(dbfile);
	}

	public Idx(Path file)
			throws SQLException {
		this(new Jdbc("sqlite").file(file));
	}

	public Idx(Jdbc jdbc)
			throws SQLException {
		init(jdbc);
	}

	private void init(Jdbc jdbc)
			throws SQLException {

		this.jdbc = jdbc;
		this.con  = jdbc.connect();

		XFuncPlugin.load(con);

//		Jdbc.execute(con, "DROP TABLE IF EXISTS " + name);
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE IF NOT EXISTS " + name + " ( ");
		query.append("   ins_date TEXT NOT NULL ");
		query.append("  ,upd_date TEXT NOT NULL ");
		query.append("  ,type     TEXT NOT NULL ");
		query.append("  ,key      TEXT NOT NULL ");
		query.append("  ,val      TEXT ");
		query.append("  ,PRIMARY KEY (key) ");
		query.append(")");
		Jdbc.execute(con, query.toString());

//		Jdbc.execute(con, "DROP INDEX IF EXISTS [idx/idx1]");
		Jdbc.execute(con, "CREATE INDEX IF NOT EXISTS [idx/idx1] ON idx (type, key)");

		// TODO: SELECT DISTINCT type FROM idx WHERE type IS NOT NULL
		List<String> types = new LinkedList<>();
		types.add("file");
		types.add("table");
		for (String type : types) {

			Jdbc.execute(con, "DROP VIEW IF EXISTS [idx/" + type + "]");

			query.setLength(0);
			query.append("CREATE VIEW IF NOT EXISTS [idx/" + type + "] AS ");
			query.append("  SELECT ");
			query.append("       key ");
			query.append("      ,val ");
			query.append("    FROM ");
			query.append("      idx ");
			query.append("    WHERE ");
			query.append("      type = '" + type + "' ");
			Jdbc.execute(con, query.toString());

		}

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

	public synchronized Map<String, Object> get(final String key)
			throws SQLException {

		Map<String, Object> rec = null;

		if (cache.containsKey(key)) {

			rec = new HashMap<>();
			rec.put("key", key);
			rec.putAll(cache.get(key));

		} else {

			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append("    * ");
			query.append("  FROM ");
			query.append("    idx ");
			query.append("  WHERE ");
			query.append("    key = ? ");

			try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

				stmt.setObject(1, key);

				try (ResultSet rs = stmt.executeQuery()) {

					if (rs.next()) {
						rec = Jdbc.map(rs);
						rec.put("val", new JSONObject(rec.get("val")).toMap());
					}

					if (rs.next()) {
						throw new SQLException(key);
					}

				}

			}

		}

		return rec;

	}

	public void vacuum()
			throws SQLException {
		flush();
		execute("VACUUM");
	}

	public synchronized void flush()
			throws SQLException {

		// delete
		boolean enabled = false;
		if (enabled) {

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

	@Override
	public void close() {
		_0.close(con);
	}

	public void idx()
			throws IOException, SQLException {

		if (null != jdbc) {
			idx_table(jdbc);
		}

		List<Path> paths = new LinkedList<>();
		paths.add(_0.userhome);
		if (_0.windows) {
			for (int i = 0; i <= 'Z' - 'A'; i++) {
				paths.add(Path.of((char)('A' + i) + ":/"));
			}
		} else {
			paths.add(Path.of("/"));
		}

		while (!paths.isEmpty()) {
			idx_file(_0.ip(), paths.remove(0), f -> false);
		}

	}

	private static FileFilter filter() {

		Set<String> exts = new HashSet<>();

		// source
		if (true) {
			exts.add("c"); exts.add("cpp"); exts.add("cs"); exts.add("h");
			exts.add("rs");
			exts.add("go");
			exts.add("java");
			exts.add("php");
			exts.add("pl");
			exts.add("py");
			exts.add("js");
			exts.add("sh");
			exts.add("bat");
			exts.add("bas");
			exts.add("frm");
			exts.add("cob");
			exts.add("sql"); exts.add("ddl");
			exts.add("Makefile");
			exts.add("Dockerfile");
			exts.add("Vagrantfile");
			exts.add("htm"); exts.add("html");
			exts.add("css");
			exts.add("cnf"); exts.add("conf"); exts.add("config");
			exts.add("ini");
			exts.add("properties");
			exts.add("json");
			exts.add("yml"); exts.add("yaml");
			exts.add("xml");
		}

		// data
		if (true) {
			exts.add("tsv");
			exts.add("csv");
			exts.add("avro");
			exts.add("avsc");
			exts.add("mdb");
			exts.add("accdb");
		}

		// sec
		if (true) {
			exts.add("key");
			exts.add("pub");
			exts.add("crt");
			exts.add("csr");
			exts.add("pem");
			exts.add("id_rsa");
		}

		// archive
		if (true) {
			exts.add("jar");
			exts.add("war");
			exts.add("ear");
			exts.add("tar");
			exts.add("bz2");
			exts.add("gz");
			exts.add("rar");
			exts.add("zip");
			exts.add("7z");
			exts.add("lzh");
		}

		// media
		if (true) {
			exts.add("png");
			exts.add("gif");
			exts.add("jpg");
			exts.add("jpeg");
			exts.add("bmp");
			exts.add("mp3");
			exts.add("mp4");
			exts.add("flv");
			exts.add("avi");
			exts.add("wav");
		}

		if (true) {
			exts.add("txt");
			exts.add("xls");
			exts.add("xlsx");
			exts.add("doc");
			exts.add("docx");
			exts.add("ppt");
			exts.add("pptx");
			exts.add("pdf");
			exts.add("iso");
			exts.add("img");
		}

		return new FileExtFilter(exts);

	}

	/**
	 * <pre>
	 * ファイルをインデックス化します。
	 * </pre>
	 *
	 * @param host
	 * @param path
	 * @throws IOException
	 */
	public void idx_file(final InetAddress host, final Path path)
			throws IOException {
		idx_file(host, path, null);
	}

	/**
	 * <pre>
	 * ファイルをインデックス化します。
	 * </pre>
	 *
	 * @param host
	 * @param path
	 * @param filter
	 * @throws IOException
	 */
	public void idx_file(final InetAddress host, final Path path, final FileFilter filter)
			throws IOException {

		InetAddress host_ = null == host || host.getHostAddress().startsWith("127.") ? _0.ip() : host;

		Path uncpath = _0.uncpath(host, path);

		FileFilter filter_ = null == filter ? filter() : filter;

		Files.walkFileTree(uncpath, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path uncpath, final BasicFileAttributes attrs)
					throws IOException {

				if (filter_.accept(uncpath.toFile())) {

					String key      = uncpath.toString().replace('\\', '/');
					String hostpath = _0.hostpath(uncpath);

					try {

						Map<String, Object> val = new HashMap<>();
						val.put("host",   host_.getHostAddress());
						val.put("path",   hostpath);
						val.put("latest", _0.ymdhmss(_0.latest(attrs)));

						set("file", key, val);

					} catch (SQLException e) {
						log.trace("{}", uncpath, e);
					}

					String lower = key.toLowerCase();
					if (lower.endsWith(".mdb") || lower.endsWith(".accdb")) {

						try {

							idx_table(host_, Path.of(hostpath));

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

	/**
	 * <pre>
	 * テーブルをインデックス化します。
	 * </pre>
	 *
	 * @param jdbc
	 * @throws IOException
	 * @throws SQLException
	 */
	public void idx_table(final Jdbc jdbc)
			throws IOException, SQLException {

		try (Connection con = jdbc.connect()) {

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
//					schemas.addAll(Jdbc.schemas(con, catalog).stream().map(e -> (String)e.get("table_schem")).toList());
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

						Map<String, Object> val = jdbc.attrs();
						if (null != catalog_) {
							val.put("catalog", catalog_);
						}
						if (null != schema_) {
							val.put("schema", schema_);
						}
						val.put("table", table_);

						set("table", tbl_key, val);

//						List<Map<String, Object>> columnmaps = Jdbc.columns(con, catalog_, schema_, table_);
//						for (Map<String, Object> columnmap : columnmaps) {
//
//							String column    = (String)columnmap.get("COLUMN_NAME");
//							int    data_type = _0.cast(int.class, columnmap.get("DATA_TYPE"));
//
//							String col_key = tbl_key + "/" + column;
//
//							set("column", col_key, jdbc.attrs());
//
//						}

					}

				}

			}

		}

	}

	private void idx_table(final InetAddress host, final Path path)
			throws IOException, SQLException {

		InetAddress host_ = null == host || host.getHostAddress().startsWith("127.") ? _0.ip() : host;

		Path uncpath = _0.uncpath(host_, path);

		String hostpath = _0.hostpath(uncpath);

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

				Map<String, Object> val = new HashMap<>();
				val.put("host",  host_.getHostAddress());
				val.put("path",  hostpath);
				val.put("table", name);

				set("table", key, val);

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

		} catch (IllegalArgumentException e) {
			log.trace("{} {} {}", host_.getHostAddress(), hostpath, e.toString());

		} catch (IOException e) {
			log.trace("{} {} {}", host_.getHostAddress(), hostpath, e.toString());

		} catch (UnsupportedCodecException e) {
			log.trace("{} {} {}", host_.getHostAddress(), hostpath, e.toString());
		}

	}

	public void load(final String key)
			throws IOException, SQLException {

		Map<String, Object> rec = get(key);

		if (null != rec) {
			for (TableLoader loader : loaders) {

				if (!loader.is_load(rec)) {
					continue;
				}

				StopWatch sw = new StopWatch();

				loader.load(rec);

				log.debug("load time={} {}", sw.stop(), key);

			}
		}

	}

	private interface TableLoader {

		public boolean is_load(final Map<String, Object> rec);

		public void load(final Map<String, Object> rec)
				throws IOException, SQLException;

	}

	// TODO: 外部拡張化
	private List<TableLoader> loaders = new LinkedList<>() {

		private static final long serialVersionUID = 1L;

		{

			add(new TableLoader() {

				@Override
				public boolean is_load(final Map<String, Object> rec) {

					String type = _0.select(rec, "type");
					String path = _0.select(rec, "val", "path");

					boolean is_load = true;
					is_load &= "table".equals(type);
					is_load &= null != path && (path.endsWith(".mdb") || path.endsWith(".accdb"));

					return is_load;

				}

				@Override
				public void load(final Map<String, Object> rec)
						throws IOException, SQLException {

					String val_host  = _0.select(rec, "val", "host");
					String val_path  = _0.select(rec, "val", "path");
					String val_table = _0.select(rec, "val", "table");

					Path uncpath = _0.uncpath(InetAddress.getByName(val_host), Path.of(val_path));

					try (Database mdb = DatabaseBuilder.open(uncpath)) {

						Table table = mdb.getTable(val_table);

						List<? extends Column> columns = table.getColumns();

						// TODO: create table
						// TODO: transfer

					}

				}

			});

			add(new TableLoader() {

				@Override
				public boolean is_load(final Map<String, Object> rec) {

					String type = _0.select(rec, "type");
					String uri  = _0.select(rec, "val", "uri");

					boolean is_load = true;
					is_load &= "table".equals(type);
					is_load &= null != uri;

					return is_load;

				}

				@Override
				public void load(final Map<String, Object> rec)
						throws IOException, SQLException {

					Map<String, Object> val = _0.select(rec, "val");

					try (Connection in_con = new Jdbc(val).connect()) {

						String key = _0.select(rec, "key");

						String in_table  = null;
						{

							String catalog = _0.select(val, "catalog");
							String schema  = _0.select(val, "schema");
							String table   = _0.select(val, "table");

							in_table = Jdbc.esc(in_con, Arrays.asList(catalog, schema, table).stream()
									.filter(Objects::nonNull)
									.collect(Collectors.joining("/")));

						}

						String out_table = Jdbc.esc(con, key);

						Jdbc.transfer(in_con, in_table, con, out_table);

					}

				}

			});

		}
	};

	public void execute(final String query)
			throws SQLException {
		Jdbc.execute(con, query);
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
