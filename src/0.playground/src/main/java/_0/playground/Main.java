package _0.playground;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import _0.Jdbc;
import _0.ValSelector;
import _0._0;
import _0.playground.sshd.Sshd;

public final class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static ExecutorService worker = Executors.newFixedThreadPool(Math.min(1, _0.availableProcessors >> 1));

	private static InetAddress ip = null;

	private Main() {
	}

	public static final void main(final String... args)
			throws Exception {

		Sshd sshd = null;
		Idx  idx  = null;
		try {

			ip = _0.ip();

			ValSelector selector = ValSelector.of(yml("playground.yml")).get("playground").of();
			for (Object key : selector.keys()) {

				ValSelector yml = selector.get(key).of();

				Object schedule = yml.get("schedule").val();
				if (null == schedule) {
					continue;
				}

				// TODO: args
				// TODO: schedule
				// TODO: closeable

				Method method = null;
				try {
					method = Main.class.getDeclaredMethod(String.valueOf(key), ValSelector.class);
				} catch (NoSuchMethodException e) {
					log.trace("{}", e.toString());
					continue;
				}

				Object retval = null;
				if ("start".equals(schedule)) {
					retval = method.invoke(null, yml);
				}

				if ("sshd".equals(key)) {
					sshd = (Sshd)retval;
				} else if ("idx".equals(key)) {
					idx  = (Idx)retval;
				}

			}

			cli();
//			Thread.sleep(Long.MAX_VALUE);

		} finally {
			_0.close(sshd);
			_0.close(idx);
		}

	}

	protected static Sshd sshd(ValSelector yml)
			throws IOException {

		int port = yml.get("port").val();

		return new Sshd(port);

	}

	protected static Idx idx(ValSelector yml)
			throws IOException, SQLException {

		Idx idx = null;

		Collection<Map<String, Object>> hosts = yml.get("hosts").val();
		Set<String> exts = new HashSet<>(yml.get("exts").val());

		// TODO: hostsに追加
//		for (int i = 0; i <= 'Z' - 'A'; i++) {
//			idx.file(ip, Path.of((char)('A' + i) + ":/"), exts_filter);
//		}
//		idx.file(ip, _0.userhome, exts_filter);
//		idx.table(Idx.jdbc());

		FileFilter exts_filter = f -> {

			String ext = f.getName().toString().toLowerCase();

			int lastidx = ext.lastIndexOf("/");
			if (-1 < lastidx) {
				ext = ext.substring(lastidx + 1);
			}

			lastidx = ext.lastIndexOf(".");
			if (-1 < lastidx) {
				ext = ext.substring(lastidx + 1);
			}

			return exts.contains(ext.toLowerCase());

		};

		// ip毎に集約
		Map<String, List<Map<String, Object>>> ip_hosts = new HashMap<>();
		for (Map<String, Object> host_map : hosts) {

			String host = (String)host_map.get("host");
			if (null == host) {
				host = ip.getHostAddress();
			}

			String ip = null;
			try {
				ip = InetAddress.getByName(host).getHostAddress();
			} catch (UnknownHostException e) {
				continue;
			}

			if (!ip_hosts.containsKey(ip)) {
				ip_hosts.put(ip, new LinkedList<>());
			}

			ip_hosts.get(ip).add(host_map);

		}

		idx = new Idx();

		// ip毎にスレッド化
		for (Entry<String, List<Map<String, Object>>> entry : ip_hosts.entrySet()) {

			Idx    idx_ = idx;
			String host = entry.getKey();

			worker.submit(() -> {

				for (Map<String, Object> map : entry.getValue()) {

					if ("file".equals(map.get("type"))) {

						String path = (String)map.get("path");
						if (null == path) {
							path = _0.userhome.toString();
						}

						idx_.file(InetAddress.getByName(host), Path.of(path), exts_filter);

					} else {
						idx_.table(new Jdbc(map));
					}

				}

				return null;

			});

		}

		idx.vacuum();

		return idx;

	}

	protected static void debug(ValSelector yml)
			throws IOException {
		debug(new String[0]);
	}

	private static void debug(final String... args)
			throws IOException {

		if (log.isDebugEnabled()) {

			Comparator<Entry<?, ?>> sort = (o1, o2) -> o1.toString().compareTo(o2.toString());

			String path_separator = System.getProperty("path.separator");

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

		}

	}

	private static void cli()
			throws IOException {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
			while (true) {

				String line = in.readLine();

				if ("exit".equals(line)) {
					break;
				}

				System.out.println(line);

			}
		}

	}

	// TODO: 拾えないことが多い
	private static final void clip() {

		Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new FlavorListener() {

			@Override
			public void flavorsChanged(FlavorEvent event) {

				Clipboard clipboard = (Clipboard)event.getSource();

				try {

					Set<DataFlavor> flavors = new HashSet<>();
					flavors.add(DataFlavor.stringFlavor);
					flavors.add(DataFlavor.imageFlavor);
					flavors.add(DataFlavor.javaFileListFlavor);
					flavors.add(DataFlavor.selectionHtmlFlavor);
					flavors.add(DataFlavor.fragmentHtmlFlavor);
					flavors.add(DataFlavor.allHtmlFlavor);
					flavors.addAll(List.of(clipboard.getAvailableDataFlavors()));

					for (DataFlavor flavor : flavors) {

						if (!clipboard.isDataFlavorAvailable(flavor)) {
							continue;
						}

						Object data = clipboard.getData(flavor);

						boolean ignore = false;
						ignore |= data instanceof InputStream;
						ignore |= data instanceof Reader;
						ignore |= data instanceof ByteBuffer;
						ignore |= data instanceof CharBuffer;
						ignore |= data instanceof byte[];
						ignore |= data instanceof char[];
						if (ignore) {
							continue;
						}

						System.out.println(data);

					}

				} catch (UnsupportedFlavorException e) {
					log.trace("", e);

				} catch (IOException e) {
					log.trace("", e);
				}

			}

		});

	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> yml(String file)
			throws IOException {

		Map<String, Object> map = null;

		try (InputStream in = new FileInputStream(Path.of(file).toFile())) {
			map = (Map<String, Object>)new Yaml().loadAs(in, Map.class);
		}

		return map;

	}

}
