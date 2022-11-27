package _0.playground;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0.Jdbc;
import _0._0;

public final class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static InetAddress ip = null;

	private Main() {
	}

	public static final void main(final String... args)
			throws Exception {

		Sshd sshd = null;
		Idx  idx  = null;
		try {

			ip = _0.ip();

			debug();

			sshd = new Sshd(0);
			idx  = new Idx();

			idx.table(Idx.jdbc());
			idx.table(new Jdbc("mariadb").host("mariadb.0").username("root").password("mariadb"));
			idx.table(new Jdbc("postgres").host("pgsql.0").username("postgres").password("pgsql"));

			if (_0.windows) {
				for (int i = 0; i <= 'Z' - 'A'; i++) {
					idx.file(ip, Path.of((char)('A' + i) + ":/"), e-> true);
				}
			} else {
				idx.file(ip, _0.userhome, e -> true);
			}

			idx.vacuum();

			idx();

//			clip();

//			Thread.sleep(Long.MAX_VALUE);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
				while (true) {

					String line = in.readLine();

					if ("exit".equals(line)) {
						break;
					}

					System.out.println(line);

				}
			}

		} finally {
			_0.close(sshd);
			_0.close(idx);
		}

	}

	private static final void debug(final String... args)
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

	private static final void idx()
			throws IOException, SQLException {

		Set<String> exts = new HashSet<>();
		if (true) {

			exts.add("c"); exts.add("cpp"); exts.add("cs"); exts.add("h");
			exts.add("rs");
			exts.add("go");
			exts.add("java");
			exts.add("php");
			exts.add("pl");
			exts.add("py");
			exts.add("js");
			exts.add("sh"); exts.add("bat");
			exts.add("bas"); exts.add("frm");
			exts.add("cob");
			exts.add("sql"); exts.add("ddl");

			exts.add("Makefile");
			exts.add("Dockerfile");
			exts.add("Vagrantfile");

		}
		if (true) {

			exts.add("htm"); exts.add("html");
			exts.add("css");

			exts.add("cnf"); exts.add("conf"); exts.add("config");
			exts.add("ini");
			exts.add("properties");
			exts.add("json");
			exts.add("yml"); exts.add("yaml");
			exts.add("xml");

		}
		if (true) {
			exts.add("tsv"); exts.add("csv");
			exts.add("avro"); exts.add("avsc");
		}
		if (true) {
			exts.add("key");
			exts.add("pub");
			exts.add("crt");
			exts.add("csr");
			exts.add("pem");
			exts.add("id_rsa");
		}
		if (true) {
			exts.add("jar"); exts.add("war"); exts.add("ear");
			exts.add("tar");
			exts.add("bz2");
			exts.add("gz");
			exts.add("zip");
			exts.add("lzh");
		}
		if (true) {
			exts.add("mdb"); exts.add("accdb");
			exts.add("xls"); exts.add("xlsx");
			exts.add("pdf");
			exts.add("txt");
		}
		if (true) {
			exts.add("png");
			exts.add("gif");
			exts.add("jpg"); exts.add("jpeg");
			exts.add("bmp");
		}
		if (true) {
			exts.add("mp4");
			exts.add("flv");
			exts.add("avi");
		}

	}

}
