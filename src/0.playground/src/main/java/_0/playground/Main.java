package _0.playground;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
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

	private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	private static InetAddress ip = null;

	private static Kvs kvs = null;

	private static Set<Closeable> closeables = new HashSet<>();

	private static boolean exit = false;

	private Main() {
	}

	public static void main(final String... args)
			throws Throwable {

		StopWatch sw = new StopWatch();
		try {

			log.trace("start");

			ip  = _0.ip();
			kvs = new Kvs();

			Map<String, Object> root_yml = _0.select(map("playground.yml"), "playground");

			debug(args);
			sshd(root_yml);
			idx(root_yml);
			clipboard();

			cli();
//			Thread.sleep(Long.MAX_VALUE);

			log.trace("end time={}", sw.stop());

		} catch (Throwable e) {
			log.trace("err time={}", sw.stop(), e);
			throw e;

		} finally {

			worker.shutdown();

			closeables.stream()
					.parallel()
					.forEach(_0::close);

			if (null != kvs) {
				kvs.vacuum();
				_0.close(kvs);
			}

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

	private static void sshd(final Map<String, Object> root_yml)
			throws IOException {

		Map<String, Object> curr_yml = _0.select(root_yml, _0.current().getMethodName());

		Integer port = _0.select(curr_yml, "port");
		int port_ = null == port ? 0 : Math.max(0, port.intValue());

		closeables.add(new Sshd(port_));

	}

	// TODO: Inconsistent stackmap frames at branch target 149
	private static void idx(final Map<String, Object> root_yml)
			throws IOException, SQLException {

		Map<String, Object> curr_yml = _0.select(root_yml, _0.current().getMethodName());

		Collection<Map<String, Object>> auths   = _0.select(root_yml, "auth");
		Collection<Map<String, Object>> targets = _0.select(curr_yml, "target");

		List<String> exts = _0.select(curr_yml, "exts");
		FileExtFilter exts_filter = _0.empty(exts) ? new FileExtFilter() : new FileExtFilter(exts);

		// localhost
		{

			// auth
			idx_table_rdb(kvs.jdbc);
			idx_table_rdb(new Jdbc("mariadb").host("mariadb.0").username("root").password("mariadb"));
			idx_table_rdb(new Jdbc("postgres").host("pgsql.0").username("postgres").password("pgsql"));

			List<Path> paths = new LinkedList<>();

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

			while (!paths.isEmpty()) {
				idx_file(ip, paths.remove(0), exts_filter);
			}

		}

		// ip毎に集約
		Map<String, List<Map<String, Object>>> ip_hosts = new HashMap<>();
		for (Map<String, Object> host_map : targets) {

			String ip = null;
			try {

				String host = _0.select(host_map, "host");

				ip = InetAddress.getByName(host).getHostAddress();

			} catch (UnknownHostException e) {
				log.trace("{}", e.toString());
				continue;
			}

			if (!ip_hosts.containsKey(ip)) {
				ip_hosts.put(ip, new LinkedList<>());
			}

			ip_hosts.get(ip).add(host_map);

		}

		// ip毎にスレッド化
		for (Entry<String, List<Map<String, Object>>> entry : ip_hosts.entrySet()) {

			String host = entry.getKey();

			submit(host, () -> {

				for (Map<String, Object> target : entry.getValue()) {

					String type = (String)target.get("type");
					String path = (String)target.get("path");

					// TODO: auths.isEmpty()
					for (Map<String, Object> auth : auths) {

						// TODO: 事前にip変換
						if (!host.equals(InetAddress.getByName((String)auth.get("host")).getHostAddress())) {
							continue;
						}

						// TODO: unmatched
						boolean unmatched = false;
						unmatched |= "file".equals(type)  && !"cifs".equals(auth.get("type"));
						unmatched |= "table".equals(type) && "cifs".equals(auth.get("type"));
						if (unmatched) {
							continue;
						}

						Map<String, Object> merged = new HashMap<>();
						merged.putAll(target);
						merged.putAll(auth);
						merged.put("host", host);

						log.debug("{}", merged);

						if ("file".equals(type)) {
							idx_file(InetAddress.getByName(host), Path.of(path), exts_filter);

						} else if ("table".equals(type)) {
							idx_table_rdb(new Jdbc(merged));

						} else {
							throw new UnsupportedOperationException(type);
						}

					}

				}

				return null;

			});

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

		Set<DataFlavor> flavors = new HashSet<>();
		flavors.add(DataFlavor.stringFlavor);
//		flavors.add(DataFlavor.imageFlavor);
//		flavors.add(DataFlavor.javaFileListFlavor);
//		flavors.add(DataFlavor.selectionHtmlFlavor);
//		flavors.add(DataFlavor.fragmentHtmlFlavor);
//		flavors.add(DataFlavor.allHtmlFlavor);
//		flavors.addAll(List.of(transferable.getTransferDataFlavors()));

		submit("clipboard", () -> {

			Object prev = null;
			while (!exit) {

				for (DataFlavor flavor : flavors) {

					Object data = null;
					try {
						data = clipboard.getContents(null).getTransferData(flavor);
					} catch (IOException e) {
						throw new IllegalStateException(e);
					} catch (UnsupportedFlavorException e) {
						continue;
					}

					if (data.equals(prev)) {
						break;
					}
					prev = data;

					// TODO: event
					Object data_ = data;
					submit("clipboard/" + data_.hashCode(), () -> {

						kvs.set("clipboard", data_.toString());

						log.debug("{} {}", flavor, data_);

						return null;

					});

				}

				// 100ms間隔
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}

			}

		});

	}

	private static void cli()
			throws IOException, SQLException {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
			while (true) {

				String line = _0.trim(in.readLine());

				exit = "exit".equals(line);
				if (exit) {
					break;
				}
				if ("".equals(line)) {
					continue;
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

				if (line.startsWith("load ")) {

					String idx_key = line.substring("load ".length());

					load_table(idx_key);

					continue;

				}

				log.info(line);

			}
		}

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
	private static void idx_file(final InetAddress host, final Path path, final FileFilter filter)
			throws IOException {

		boolean local = false;
		local |= ip == host;
		local |= host.isLoopbackAddress();

		InetAddress host_ = local ? ip : host;

		Path uncpath = local ? path : _0.uncpath(host, path);

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
						val.put("host",   host_.getHostAddress());
						val.put("path",   hostpath);
						val.put("latest", _0.ymdhmss(_0.latest(attrs)));

						kvs.set("file", key, val);

					} catch (SQLException e) {
						log.trace("{}", uncpath, e);
					}

					String lower = key.toLowerCase();
					if (lower.endsWith(".mdb") || lower.endsWith(".accdb")) {

						try {

							idx_table_mdb(host_, Path.of(hostpath));

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
	private static void idx_table_rdb(final Jdbc jdbc)
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
							tbl_key = "//" + ip.getHostAddress() + "/" + jdbc.file().toAbsolutePath() + "/" + table_;
//						} else if (ip) {
//							tbl_key = "//" + InetAddress.getByName(jdbc.host()).getHostAddress() + "/" + String.join("/", Arrays.asList(catalog_, schema_, table_).stream().filter(Objects::nonNull).toList());
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

						kvs.set("table", tbl_key, val);

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

	private static void idx_table_mdb(final InetAddress host, final Path path)
			throws IOException, SQLException {

		boolean local = false;
		local |= ip == host;
		local |= host.isLoopbackAddress();

		InetAddress host_ = local ? ip : host;

		Path uncpath = local ? path : _0.uncpath(host, path);

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
				val.put("path",  uncpath);
				val.put("table", name);

				kvs.set("table", key, val);

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
			log.trace("{} {} {}", host_.getHostAddress(), uncpath, e.toString());

		} catch (IOException e) {
			log.trace("{} {} {}", host_.getHostAddress(), uncpath, e.toString());

		} catch (UnsupportedCodecException e) {
			log.trace("{} {} {}", host_.getHostAddress(), uncpath, e.toString());
		}

	}

	private static void load_table(final String key)
			throws IOException, SQLException {

		Map<String, Object> rec = kvs.get("table", key);

		if (null != rec) {

			String path = _0.select(rec, "val", "path");
			String uri  = _0.select(rec, "val", "uri");

			StopWatch sw = new StopWatch();

			if (null != path && (path.endsWith(".mdb") || path.endsWith(".accdb"))) {
				load_table_mdb(rec);

			} else if (null != uri) {
				load_table_rdb(rec);

			} else {
				throw new IllegalArgumentException(key);
			}

			log.debug("load time={} {}", sw.stop(), key);

		}

	}

	private static void load_table_mdb(final Map<String, Object> rec)
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

	private static void load_table_rdb(final Map<String, Object> rec)
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

			log.info("  - id: {}", thread.threadId());
			log.info("    name: {}", thread.getName());
			log.info("    stacktrace:");

			StackTraceElement[] stacks = map.get(thread);
			for (int i = stacks.length - 1; 0 <= i; i--) {

				String s = stacks[i].toString();

				if (!all && !s.startsWith("app//")) {
					continue;
				}

				log.info("      - {}", s);

			}

		}

	}

	private static void submit(final String name, final Runnable impl) {
		new Thread(impl, name).start();
	}

	private static void submit(final String name, final Callable<Void> impl) {
		submit(name, impl, 0);
	}

	private static void submit(final String name, final Callable<Void> impl, final long millis) {
		submit(name, impl, millis, TimeUnit.MILLISECONDS);
	}

	private static void submit(final String name, final Callable<Void> impl, final long delay, final TimeUnit unit) {

		worker.schedule(() -> {

			String origin = Thread.currentThread().getName();
			Thread.currentThread().setName(origin + "/" + name);

			Object ret = impl.call();

			Thread.currentThread().setName(origin);

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

}
