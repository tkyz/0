package _0.playground;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
import _0.NetworkInterfaceFilter;
import _0._0;
import _0.debug.StopWatch;

public final class Idx {

	private static final Logger log = LoggerFactory.getLogger(Idx.class);

	// 定義データ投入
//	Map<String, Object> kvs_yml = _0.select(root_yml, method_name);
//	for (String table : kvs_yml.keySet()) {
//
//		List<?> recs = _0.select(kvs_yml, table);
//		if (_0.empty(recs)) {
//			continue;
//		}
//
//		for (Object rec : recs) {
//
//			String              key = null;
//			Map<String, Object> val = null;
//
//			if (rec instanceof Map) {
//				key = _0.select(rec, "key");
//				val = _0.select(rec, "val");
//
//			} else if (rec instanceof CharSequence) {
//				key = (String)rec;
//
//			} else {
//				throw new UnsupportedOperationException();
//			}
//
//			kvs.set(table, key, val);
//
//		}
//
//	}

	public void run()
			throws IOException, SQLException {

		fs();
		lan();

		rdb(Main.kvs.jdbc);

	}

	private void fs() {

		boolean currentdir = true;
		boolean userhome   = !currentdir && true;
		boolean rootdir    = !userhome   && true;

		Main.submit(null, () -> {

			if (currentdir) {
				fs(Path.of("."));

			} else if (userhome) {
				fs(_0.userhome);

			} else if (rootdir) {

				if (_0.windows) {
					int max = 'Z' - 'A';
					for (int i = 0; i <= max; i++) {
						fs(Path.of((char)('A' + i) + ":/"));
					}

				} else {
					fs(Path.of("/"));
				}

			} else {
				throw new UnsupportedOperationException();
			}

			return null;

		});

	}

	private void fs(final Path start)
			throws IOException {

		FileExtFilter filter = new FileExtFilter();

		Files.walkFileTree(start.toAbsolutePath().normalize(), new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)
					throws IOException {

				if (filter.apply(path)) {

					String key = path.toString().replace('\\', '/');

					if (key.startsWith("//")) {
						// pass
					} else if (key.startsWith("/")) {
						key = "//" + _0.ip().getHostAddress() + key;
					} else {
						throw new UnsupportedOperationException(key);
					}

					try {

						Map<String, Object> val = new HashMap<>();
						val.put("size",   attrs.size());
						val.put("latest", _0.ymdhmss(_0.latest(attrs)));

						Main.kvs.set("file", key, val);

					} catch (SQLException e) {
						throw new IOException(e);
					}

//					String lower = key.toLowerCase();
//					if (lower.endsWith(".mdb") || lower.endsWith(".accdb")) {
//
//						try {
//
//							mdb(path);
//
//						} catch (SQLException e) {
//							log.trace("{}", path, e);
//						}
//
//					}

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

	private void lan()
			throws IOException {

		List<InterfaceAddress> ifaddrs = NetworkInterface.networkInterfaces()
				.parallel()
				.filter(new NetworkInterfaceFilter())
				.map(NetworkInterface::getInterfaceAddresses)
				.flatMap(Collection::stream)
				.filter(e -> e.getAddress().isSiteLocalAddress())
				.toList();

		for (InterfaceAddress ifaddr : ifaddrs) {

			if (ifaddr.getAddress() instanceof Inet4Address) {

				Inet4Address ipv4  = (Inet4Address)ifaddr.getAddress();
				int          ipv4_ = _0.ip(ipv4);

				short len        = ifaddr.getNetworkPrefixLength();
				int   subnetmask = _0.subnetmask(len);

				int end = (int)Math.pow(2, 32 - len);
				for (int i = 1; i < end - 1; i++) {

					int ip = ipv4_ & subnetmask | i;

					byte[] ip_ = new byte[4];
					ip_[0] = (byte)(0xff & (ip >> 24));
					ip_[1] = (byte)(0xff & (ip >> 16));
					ip_[2] = (byte)(0xff & (ip >>  8));
					ip_[3] = (byte)(0xff & (ip >>  0));

					lan((Inet4Address)InetAddress.getByAddress(ip_), len);

				}

			} else {
				log.trace("{}", ifaddr);
			}

		}

	}

	private void lan(Inet4Address addr, int len) {

		Main.submit(addr.getHostAddress(), () -> {

			if (_0.icmp(addr)) {

				int ip = _0.ip(addr);

				StringBuilder ip2  = new StringBuilder(_0.bin(ip));
//				StringBuilder ip10 = new StringBuilder(addr.getHostAddress());
//				StringBuilder ip16 = new StringBuilder(_0.hex(ip));

				ip2.insert(len, "/");

				// host
				{

					Map<String, Object> val = new HashMap<>();
					val.put("addr", addr.getHostAddress());
					if (!addr.getHostAddress().equals(addr.getCanonicalHostName())) {
						val.put("name", addr.getCanonicalHostName());
					}

					Main.kvs.set("lan", ip2.toString(), val);

				}

				// port
				int start = 1;
				int end   = 10240; // 0xffff;
				for (int port = start; port <= end; port++) {

					if (_0.tcp(addr, port)) {

						StringBuilder port2  = new StringBuilder(ip2);
//						StringBuilder port10 = new StringBuilder(ip10);
//						StringBuilder port16 = new StringBuilder(ip16);

						port2.append("/tcp/" + _0.bin(port).substring(16));

						Map<String, Object> val = new HashMap<>();
						val.put("port", port);

						Main.kvs.set("lan", port2.toString(), val);

					}

				}

			}

			return null;

		});

	}

//	public void nw()
//			throws IOException {
//
//		if (!_0.empty(curr_yml)) {
//			for (Map<String, Object> target : curr_yml) {
//
//				String type = _0.select(target, "type");
//				String host = _0.select(target, "host");
//				String path = _0.select(target, "path");
//
//				submit(host + "/" + type, () -> {
//
//					if ("file".equals(type)) {
//						idx_file(Path.of(path));
//
//					} else {
//
//						Map<String, Object> auth = auth(host, type);
//						if (null != auth) {
//							idx_table_rdb(new Jdbc(auth));
//						}
//
//					}
//
//					return null;
//
//				});
//
//			}
//		}

//		// queries
//		{
//
//			Map<String, Object> queries_yml = _0.select(curr_yml, "queries");
//			Path dir   = Path.of((String)_0.select(queries_yml, "dir"));
//			int  delay = _0.select(queries_yml, "delay");
//
//			Callable<Void> impl = new Callable<>() {
//
//				@Override
//				public Void call()
//						throws Exception {
//
//					List<Path> files = Files.list(dir)
//							.filter(new FileExtFilter("sql"))
//							.sorted((o1, o2) -> o1.compareTo(o2))
//							.toList();
//
//					for (Path file : files) {
//
//						log.debug("file: {}", file);
//
//						String[] queries = new String(Files.readAllBytes(file)).split(";");
//						for (String query : queries) {
//
//							query = _0.trim(query);
//							if ("".equals(query)) {
//								continue;
//							}
//
//							// TODO: クエリ内の文字列に--を含む場合
//							query = query.replaceAll("--[^\\r\\n]*[^\\r\\n]", "");
//
//							log.debug("  query: {}", query.replaceAll(_0.regex_spaces, " ").replace(" ,", ", "));
//
//							Jdbc.execute(kvs.con, query);
//
//						}
//
//					}
//
//					// TODO: cancel
//					if (-1 < delay) {
//						submit("queries", this, delay);
//					}
//
//					return null;
//
//				}
//
//			};
//			submit("queries", impl, 0);
//
//		}
//
//	}

	/**
	 * <pre>
	 * テーブルをインデックス化します。
	 * </pre>
	 *
	 * @param jdbc
	 * @throws IOException
	 * @throws SQLException
	 */
	private void rdb(final Jdbc jdbc)
			throws IOException, SQLException {

		String host = (null == jdbc.host() ? _0.ip() : InetAddress.getByName(jdbc.host())).getHostAddress();
		String path = null == jdbc.path() ? null : jdbc.path().toAbsolutePath().normalize().toString().replace('\\', '/');

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
							tbl_key = "//" + host + path + "/" + table_;
						} else {
							tbl_key = "//" + host + "/" + String.join("/", Arrays.asList(catalog_, schema_, table_).stream().filter(Objects::nonNull).toList());
						}

						Map<String, Object> val = new HashMap<>();
						val.put("type", jdbc.type());
						if (null != host) {
							val.put("host", host);
						}
						if (null != path) {
							val.put("path", path);
						}
						if (null != catalog_) {
							val.put("catalog", catalog_);
						}
						if (null != schema_) {
							val.put("schema", schema_);
						}
						val.put("table", table_);

						Main.kvs.set("table", tbl_key, val);

						List<Map<String, Object>> columnmaps = Jdbc.columns(con, catalog_, schema_, table_);
						for (Map<String, Object> columnmap : columnmaps) {

							String column    = (String)columnmap.get("COLUMN_NAME");
							Object data_type = columnmap.get("DATA_TYPE");

							if (data_type instanceof CharSequence) {
								data_type = Integer.valueOf(data_type.toString());
							} else if (data_type instanceof String) {
								data_type = _0.cast(Integer.class, data_type);
							}

							val = new HashMap<>();
							val.put("type", data_type);

							String col_key = tbl_key + "/" + column;

							Main.kvs.set("column", col_key, val);

						}

					}

				}

			}

		}

	}

	private void mdb(final Path path)
			throws SQLException {

		// TODO: InputStream
		try (Database mdb = DatabaseBuilder.open(path)) {

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

				String tbl_key = path.toString().replace('\\', '/') + "/" + name;

				Map<String, Object> val = new HashMap<>();
				val.put("path",  path);
				val.put("table", name);

				Main.kvs.set("table", tbl_key, val);

				List<? extends Column> columns = table.getColumns();
				for (Column column : columns) {

					String col_key = tbl_key + "/" + column.getName();

					val = new HashMap<>();
					val.put("type", column.getType().getSQLType());

					Main.kvs.set("column", col_key, val);

				}

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
			log.trace("{} {}", path, e.toString());

		} catch (IOException e) {
			log.trace("{} {}", path, e.toString());

		} catch (UnsupportedCodecException e) {
			log.trace("{} {}", path, e.toString());
		}

	}

	public void load(final String key)
			throws IOException, SQLException {

		Map<String, Object> rec = Main.kvs.get("table", key);

		if (null == rec) {
			throw new IOException(key);
		}

		String path = _0.select(rec, "val", "path");

		StopWatch sw = new StopWatch();

		if (null != path && (path.endsWith(".mdb") || path.endsWith(".accdb"))) {
			load_mdb(rec);

		} else {
			load_rdb(rec);
		}

		log.debug("load time={} {}", sw.stop(), key);

	}

	private void load_mdb(final Map<String, Object> rec)
			throws IOException, SQLException {

		String key       = _0.select(rec, "key");
		String val_path  = _0.select(rec, "val", "path");
		String val_table = _0.select(rec, "val", "table");

		try (Database mdb = DatabaseBuilder.open(Path.of(val_path))) {

			Table table = mdb.getTable(val_table);

			List<? extends Column> columns = table.getColumns();

			Map<String, Integer> columns_ = new LinkedHashMap<>();
			for (int i = 0; i < columns.size(); i++) {

				String name = columns.get(i).getName();
				int    type = columns.get(i).getSQLType();

				columns_.put(name, Integer.valueOf(type));

			}

			Jdbc.drop_table(  Main.kvs.con, key);
			Jdbc.create_table(Main.kvs.con, key, columns_);
			// TODO: transfer

		}

	}

	private void load_rdb(final Map<String, Object> rec)
			throws IOException, SQLException {

		String              key         = _0.select(rec, "key");
		Map<String, Object> val         = _0.select(rec, "val");
//		String              val_type    = _0.select(val, "type");
//		String              val_host    = _0.select(val, "host");
		String              val_catalog = _0.select(val, "catalog");
		String              val_schema  = _0.select(val, "schema");
		String              val_table   = _0.select(val, "table");

//		Map<String, Object> auth = auth(val_host, val_type);

		try (Connection in_con = new Jdbc(val).connect()) {

			val_catalog = Jdbc.esc(in_con, val_catalog);
			val_schema  = Jdbc.esc(in_con, val_schema);
			val_table   = Jdbc.esc(in_con, val_table);

			String in_table = Arrays.asList(val_catalog, val_schema, val_table).stream()
					.filter(Objects::nonNull)
					.collect(Collectors.joining("."));

			String out_table = Jdbc.esc(Main.kvs.con, key);

			Jdbc.transfer(in_con, in_table, Main.kvs.con, out_table);

		}

	}

}
