package _0.playground;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.impl.UnsupportedCodecException;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import _0.Ansi;
import _0.FileExtFilter;
import _0.Jdbc;
import _0.ThreadFactory;
import _0._0;
import _0.debug.StopWatch;
import _0.playground.kvs.Kvs;
import _0.playground.sshd.Sshd;

public final class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static ScheduledExecutorService worker = Executors.newScheduledThreadPool(Math.max(4, _0.availableProcessors >> 1), new ThreadFactory("worker/"));

	private static Kvs kvs = null;

	private Main() {
	}

	public static void main(final String... args)
			throws Throwable {

		log.trace("start");

		StopWatch sw = new StopWatch();
		try {

			Map<String, Object> root_yml = _0.select(map("playground.yml"), "playground");

			debug(args);
			kvs(root_yml);
			clipboard();
			sshd(root_yml);
			idx(root_yml);

			cli();

		} finally {
			_0.exit = true;
			worker.shutdown();
			log.trace("end time={}", sw.stop());
		}

	}

	private static void debug(final String... args)
			throws IOException {

		if (log.isDebugEnabled()) {

			Comparator<Entry<?, ?>> sort = (o1, o2) -> o1.toString().compareTo(o2.toString());

			String path_separator = System.getProperty("path.separator");

			log.debug("{}---", Ansi.gray);
			log.debug("debug:");

			log.debug("  args:");
			if (null != args) {
				for (String arg : args) {
					log.debug("    - " + arg);
				}
			}

			log.debug("  env:");
			if (true) {

				Set<String> set = new HashSet<>();
				set.add("classpath");
				set.add("path");
				set.add("pathext");
				set.add("psmodulepath");
				set.add("session_manager");

				for (Entry<String, String> entry : System.getenv().entrySet().stream().sorted(sort).collect(Collectors.toList())) {

					if (set.contains(entry.getKey().toLowerCase())) {

						String[] paths = entry.getValue().toString().split(Pattern.quote(path_separator));
						log.debug("    " + entry.getKey() + ":");
						for (String path : paths) {
							log.debug("      - \"" + path + "\"");
						}

					} else {
						log.debug("    " + entry.getKey() + ": \"" + entry.getValue() + "\"");
					}

				}

			}

			log.debug("  prop:");
			if (true) {

				Set<String> set = new HashSet<>();
				set.add("java.class.path");
				set.add("java.library.path");

				for (Entry<Object, Object> entry : System.getProperties().entrySet().stream().sorted(sort).collect(Collectors.toList())) {

					if (set.contains(entry.getKey().toString().toLowerCase())) {

						String[] paths = entry.getValue().toString().split(Pattern.quote(path_separator));
						log.debug("    " + entry.getKey() + ":");
						for (String path : paths) {
							log.debug("      - \"" + path + "\"");
						}

					} else {
						log.debug("    " + entry.getKey() + ": \"" + entry.getValue() + "\"");
					}

				}

			}

			log.debug("  ni:");
			if (true) {

				List<NetworkInterface> nics = Collections.list(NetworkInterface.getNetworkInterfaces());
				Collections.sort(nics, (o1, o2) -> o1.getName().compareTo(o2.getName()));
				for (NetworkInterface nic : nics) {

					List<InterfaceAddress> v4 = new LinkedList<>();
					List<InterfaceAddress> v6 = new LinkedList<>();
					{
						Iterator<InterfaceAddress> addrs = nic.getInterfaceAddresses().iterator();
						while (addrs.hasNext()) {
							InterfaceAddress addr = addrs.next();
							InetAddress ipaddr = addr.getAddress();
							if (ipaddr instanceof Inet4Address) {
								v4.add(addr);
							} else if (ipaddr instanceof Inet6Address) {
								v6.add(addr);
							} else {
								System.err.println(addr);
							}
						}
					}

					log.debug("    - name: " + nic.getName());
					log.debug("      mtu: " + nic.getMTU());
					if (!v4.isEmpty()) {
						log.debug("      v4:");
						for (InterfaceAddress addr : v4) {
							InetAddress ipaddr = addr.getAddress();
							log.debug("        - " + ipaddr.getHostAddress() + "/" + addr.getNetworkPrefixLength());
						}
					}
					if (!v6.isEmpty()) {
						log.debug("      v6:");
						for (InterfaceAddress addr : v6) {
							InetAddress ipaddr = addr.getAddress();
							log.debug("        - " + ipaddr.getHostAddress().replace("%" + nic.getName(), "") + "/" + addr.getNetworkPrefixLength());
						}
					}

				}

			}

			log.debug("{}", Ansi.reset);

		}

	}

	private static void kvs(final Map<String, Object> root_yml)
			throws IOException, SQLException {

		String method_name = _0.current().getMethodName();

		kvs = new Kvs();

		// 定義データ投入
		Map<String, Object> kvs_yml = _0.select(root_yml, method_name);
		for (String table : kvs_yml.keySet()) {

			List<?> recs = _0.select(kvs_yml, table);
			if (_0.empty(recs)) {
				continue;
			}

			for (Object rec : recs) {

				String              key = null;
				Map<String, Object> val = null;

				if (rec instanceof Map) {
					key = _0.select(rec, "key");
					val = _0.select(rec, "val");

				} else if (rec instanceof CharSequence) {
					key = (String)rec;

				} else {
					throw new UnsupportedOperationException();
				}

				kvs.set(table, key, val);

			}

		}

		_0.shutdown(method_name, () -> {

			try {
				kvs.vacuum();
			} catch (IOException e) {
				log.trace("", e);
			} catch (SQLException e) {
				log.trace("", e);
			}

			_0.close(kvs);

		});

	}

	private static void sshd(final Map<String, Object> root_yml)
			throws IOException {

		String method_name = _0.current().getMethodName();

		Map<String, Object> curr_yml = _0.select(root_yml, method_name);

		Integer port = _0.select(curr_yml, "port");
		int port_ = null == port ? 0 : Math.max(0, port.intValue());

		Sshd sshd = new Sshd(port_);

		_0.shutdown(method_name, () -> {
			_0.close(sshd);
		});

	}

	private static void idx(final Map<String, Object> root_yml)
			throws IOException, SQLException {

		Collection<Map<String, Object>> curr_yml = _0.select(root_yml, _0.current().getMethodName());

		// TODO: 1ホスト:1スレッド

		submit(_0.ip().getHostAddress(), () -> {

			List<Path> paths = new LinkedList<>();
			{

				boolean homeonly = true;

				if (homeonly) {
					paths.add(_0.userhome);
				} else if (_0.windows) {
					int max = 'Z' - 'A';
					for (int i = 0; i <= max; i++) {
						paths.add(Path.of((char)('A' + i) + ":/"));
					}
				} else {
					paths.add(Path.of("/"));
				}

			}

			while (!_0.empty(paths)) {
				idx_file(paths.remove(0));
			}
			idx_table_rdb(kvs.jdbc);
			idx_host();

			_0.flush(kvs);

			return null;

		});

		if (!_0.empty(curr_yml)) {
			for (Map<String, Object> target : curr_yml) {

				String type = _0.select(target, "type");
				String host = _0.select(target, "host");
				String path = _0.select(target, "path");

				submit(host + "/" + type, () -> {

					if ("file".equals(type)) {
						idx_file(Path.of(path));

					} else {

						Map<String, Object> auth = auth(host, type);
						if (null != auth) {
							idx_table_rdb(new Jdbc(auth));
						}

					}

					return null;

				});

			}
		}

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

	}

	private static void clipboard() {

		_0.clipboard(val -> worker.submit(() -> {

			String[] items = val.toString().split("[\\r\\n]+");
			for (String item : items) {

				item = _0.trim(item);
				if ("".equals(item)) {
					continue;
				}

				try {
					kvs.set("clipboard", item.toString());
				} catch (IOException e) {
					log.trace("", e);
				} catch (SQLException e) {
					log.trace("", e);
				}

			}

		}));

	}

	private static void cli()
			throws Exception {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {

			while (!_0.exit()) {

				try {

					String line = _0.trim(in.readLine());
					if ("".equals(line)) {
						continue;
					}
					if ("exit".equals(line)) {
						break;
					}
					if ("!".equals(line)) {
						throw new Exception("cli");
					}

					// TODO: 汎用化

					if ("??".equals(line)) {
						stacktrace(true);
						continue;
					}
					if ("?".equals(line)) {
						stacktrace(false);
						continue;
					}
					if ("flush".equals(line)) {
						_0.flush(kvs);
						continue;
					}

					if (line.startsWith("load ")) {

						String idx_key = line.substring("load ".length()).trim();

						load_table(idx_key);

						continue;

					}

					log.info(line);

				} catch (IOException e) {
					log.trace("", e);

				} catch (SQLException e) {
					log.trace("", e);
				}

			}

		}

	}

	/**
	 * <pre>
	 * ファイルをインデックス化します。
	 * </pre>
	 *
	 * @param start
	 * @throws IOException
	 */
	private static void idx_file(final Path start)
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

						kvs.set("file", key, val);

					} catch (SQLException e) {
						log.trace("{}", path, e);
					}

					String lower = key.toLowerCase();
					if (lower.endsWith(".mdb") || lower.endsWith(".accdb")) {

						try {

							idx_table_mdb(path);

						} catch (IOException e) {
							log.trace("{}", path, e);

						} catch (SQLException e) {
							log.trace("{}", path, e);
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

	private static void idx_host()
			throws IOException {

		List<InterfaceAddress> ifaddrs = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
				.parallel()
				.filter(e -> e.getName().matches("^(en|sl|wl|ww)p[0-9]+s[0-9a-f]+$"))
				.map(NetworkInterface::getInterfaceAddresses)
				.flatMap(Collection::stream)
				.filter(e -> e.getAddress() instanceof Inet4Address)
				.filter(e -> !e.getAddress().isLoopbackAddress())
				.filter(e -> !e.getAddress().getCanonicalHostName().equals(e.getAddress().getHostAddress()))
				.toList();

		for (InterfaceAddress ifaddr : ifaddrs) {

			InetAddress addr = ifaddr.getAddress();
			int len = ifaddr.getNetworkPrefixLength();

			log.debug("{} {}", addr, len);

		}

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
	private static void idx_table_rdb(final Jdbc jdbc)
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

						kvs.set("table", tbl_key, val);

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

							kvs.set("column", col_key, val);

						}

					}

				}

			}

		}

	}

	private static void idx_table_mdb(final Path path)
			throws IOException, SQLException {

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

				kvs.set("table", tbl_key, val);

				List<? extends Column> columns = table.getColumns();
				for (Column column : columns) {

					String col_key = tbl_key + "/" + column.getName();

					val = new HashMap<>();
					val.put("type", column.getType().getSQLType());

					kvs.set("column", col_key, val);

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

	private static void load_table(final String key)
			throws IOException, SQLException {

		Map<String, Object> rec = kvs.get("table", key);

		if (null == rec) {
			throw new IOException(key);
		}

		String path = _0.select(rec, "val", "path");

		StopWatch sw = new StopWatch();

		if (null != path && (path.endsWith(".mdb") || path.endsWith(".accdb"))) {
			load_table_mdb(rec);

		} else {
			load_table_rdb(rec);
		}

		log.debug("load time={} {}", sw.stop(), key);

	}

	private static void load_table_mdb(final Map<String, Object> rec)
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

			Jdbc.drop_table(  kvs.con, key);
			Jdbc.create_table(kvs.con, key, columns_);
			// TODO: transfer

		}

	}

	private static void load_table_rdb(final Map<String, Object> rec)
			throws IOException, SQLException {

		String              key         = _0.select(rec, "key");
		Map<String, Object> val         = _0.select(rec, "val");
		String              val_type    = _0.select(val, "type");
		String              val_host    = _0.select(val, "host");
		String              val_catalog = _0.select(val, "catalog");
		String              val_schema  = _0.select(val, "schema");
		String              val_table   = _0.select(val, "table");

		Map<String, Object> auth = auth(val_host, val_type);

		try (Connection in_con = new Jdbc(auth).connect()) {

			val_catalog = Jdbc.esc(in_con, val_catalog);
			val_schema  = Jdbc.esc(in_con, val_schema);
			val_table   = Jdbc.esc(in_con, val_table);

			String in_table = Arrays.asList(val_catalog, val_schema, val_table).stream()
					.filter(Objects::nonNull)
					.collect(Collectors.joining("."));

			String out_table = Jdbc.esc(kvs.con, key);

			Jdbc.transfer(in_con, in_table, kvs.con, out_table);

		}

	}

	private static void stacktrace(boolean all) {

		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();

		List<Thread> keys = new LinkedList<>(map.keySet());
		Collections.sort(keys, (o1, o2) -> (int)(o1.threadId() - o2.threadId()));

		log.info("threads:");
		for (Thread thread : keys) {

			List<StackTraceElement> stacks = new LinkedList<>();
			{

				StackTraceElement[] items = map.get(thread);
				for (StackTraceElement item : items) {

					if (!all && !item.toString().startsWith("app//")) {
						continue;
					}

					stacks.add(item);

				}

				Collections.reverse(stacks);

			}

			if (_0.empty(stacks)) {
				continue;
			}

			log.info("  - id: {}", thread.threadId());
			log.info("    name: {}", thread.getName());
			log.info("    stacktrace:");

			for (StackTraceElement stack : stacks) {
				log.info("      - {}", stack);
			}

		}

	}

	private static void submit(final String name, final Callable<Void> impl) {
		submit(name, impl, 0);
	}

	private static void submit(final String name, final Callable<Void> impl, final long millis) {
		submit(name, impl, millis, TimeUnit.MILLISECONDS);
	}

	private static void submit(final String name, final Callable<Void> impl, final long delay, final TimeUnit unit) {

		worker.schedule(() -> {

			String origin = null;
			if (null != name) {
				origin = Thread.currentThread().getName();
				Thread.currentThread().setName(origin + "/" + name);
			}

			Object ret = null;
			try {
				ret = impl.call();
			} catch (RuntimeException e) {
				log.trace("", e);
			} catch (Exception e) {
				log.trace("", e);
			}

			if (null != origin) {
				Thread.currentThread().setName(origin);
			}

			return ret;

		}, delay, unit);

	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> map(final String file)
			throws IOException {

		Map<String, Object> map = null;

		try (InputStream in = new FileInputStream(Path.of(file).toFile())) {
			map = (Map<String, Object>)new Yaml().loadAs(in, Map.class);
		}

		return map;

	}

	private static Map<String, Object> auth(final String host, final String type)
			throws SQLException {

		Map<String, Object> auth = new HashMap<>();

		for (String key : kvs.keys("auth")) {

			Map<String, Object> val = _0.select(kvs.get("auth", key), "val");

			String host_ = _0.select(val, "host");
			String type_ = _0.select(val, "type");

			if (!host_.equals(host)) {
				continue;
			}
			if (!type_.equals(type)) {
				continue;
			}

			auth = val;
			break;

		}

		return auth;

	}

}
