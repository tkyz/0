package _0.playground;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.Closeable;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
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

import _0.Ansi;
import _0.FileExtFilter;
import _0.Jdbc;
import _0.ThreadFactory;
import _0._0;
import _0.debug.StopWatch;
import _0.playground.idx.Idx;
import _0.playground.sshd.Sshd;

public final class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static ScheduledExecutorService worker = Executors.newScheduledThreadPool(Math.max(4, _0.availableProcessors >> 1), new ThreadFactory("worker/"));

	private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	private static InetAddress ip = null;

	private static Idx idx = null;

	private static Set<Closeable> closeables = new HashSet<>();

	private static boolean exit = false;

	private Main() {
	}

	public static void main(final String... args)
			throws Throwable {

		StopWatch sw = new StopWatch();
		try {

			log.trace("start");

			ip = _0.ip();

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

			idx.vacuum();

			closeables.stream()
					.parallel()
					.forEach(_0::close);

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
		if (null != port) {
			Sshd sshd = new Sshd(port);
			closeables.add(sshd);
		}

	}

	private static void idx(final Map<String, Object> root_yml)
			throws IOException, SQLException {

		Map<String, Object> curr_yml = _0.select(root_yml, _0.current().getMethodName());

		idx = new Idx();
		closeables.add(idx);

		Collection<Map<String, Object>> auths   = _0.select(root_yml, "auth");
		Collection<Map<String, Object>> targets = _0.select(curr_yml, "target");

		List<String> exts = _0.select(curr_yml, "exts");
		FileExtFilter exts_filter = _0.empty(exts) ? null : new FileExtFilter(exts);

		// ip毎に集約
		Map<String, List<Map<String, Object>>> ip_hosts = new HashMap<>();
		for (Map<String, Object> host_map : targets) {

			String host = (String)host_map.get("host");
			if (null == host) {
				host = ip.getHostAddress();
			}

			String ip = null;
			try {
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
							idx.idx_file(InetAddress.getByName(host), Path.of(path), exts_filter);

						} else if ("table".equals(type)) {
							idx.idx_table(new Jdbc(merged));

						} else {
							throw new UnsupportedOperationException(type);
						}

					}

				}

				return null;

			});

		}

		// queries
		{

			Map<String, Object> queries_yml = _0.select(curr_yml, "queries");
			Path dir   = Path.of((String)_0.select(queries_yml, "dir"));
			int  delay = _0.select(queries_yml, "delay");

			Callable<Void> impl = new Callable<>() {

				@Override
				public Void call()
						throws Exception {

					List<Path> files = Files.list(dir)
							.filter(new FileExtFilter("sql"))
							.sorted((o1, o2) -> o1.compareTo(o2))
							.toList();

					for (Path file : files) {

						log.debug("file: {}", file);

						String[] queries = new String(Files.readAllBytes(file)).split(";");
						for (String query : queries) {

							query = _0.trim(query);
							if ("".equals(query)) {
								continue;
							}

							// TODO: クエリ内の文字列に--を含む場合
							query = query.replaceAll("--[^\\r\\n]*[^\\r\\n]", "");

							log.debug("  query: {}", query.replaceAll(_0.regex_spaces, " ").replace(" ,", ", "));

							idx.execute(query);

						}

					}

					// TODO: cancel
					if (-1 < delay) {
						submit("queries", this, delay);
					}

					return null;

				}

			};
			submit("queries", impl, 0);

		}

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

					Object data_ = data;
					submit("clipboard/" + data_.hashCode(), () -> {

						// TODO: event
						idx.set("", data_.toString());

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
					idx.load(idx_key);
					continue;
				}

				log.info(line);

			}
		}

	}

	private static void stacktrace(boolean all) {

		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();

		List<Thread> keys = new LinkedList<>(map.keySet());
		Collections.sort(keys, (o1, o2) -> (int)(o1.getId() - o2.getId()));

		log.info("threads:");
		for (Thread thread : keys) {

			log.info("  - id: {}", thread.getId());
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
