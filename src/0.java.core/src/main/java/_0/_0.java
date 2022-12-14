package _0;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Flushable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class _0 {

	private static final Logger log = LoggerFactory.getLogger(_0.class);

	public static final boolean windows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	public static final boolean linux   = System.getProperty("os.name").toLowerCase().startsWith("linux");
//	public static final boolean macos   = System.getProperty("os.name").toLowerCase().startsWith("mac");
//	public static final boolean solaris = System.getProperty("os.name").toLowerCase().startsWith("sun");
//	public static final boolean freebsd = System.getProperty("os.name").toLowerCase().startsWith("freebsd");

	public static final Path userhome = Path.of(System.getProperty("user.home"));

	public static final String encoding = nvl(System.getProperty("native.encoding"), System.getProperty("file.encoding"));

	public static final int pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", ""));

	/** ???????????????????????? */
	public static final int availableProcessors = Runtime.getRuntime().availableProcessors();

	private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	private static final SimpleDateFormat fmt_ymdhms  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat fmt_ymdhmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static final DecimalFormat fmt_size = new DecimalFormat("#,##0.00");

	public static final String regex_spaces = "[\\r\\n\\t\\s??? ]+";

	@SuppressWarnings("serial")
	public static final Map<String, String> char_conv = Collections.unmodifiableMap(new HashMap<>() {

		{
			// ???????????????
			put("???", "A"); put("???", "a");
			put("???", "B"); put("???", "b");
			put("???", "C"); put("???", "c");
			put("???", "D"); put("???", "d");
			put("???", "E"); put("???", "e");
			put("???", "F"); put("???", "f");
			put("???", "G"); put("???", "g");
			put("???", "H"); put("???", "h");
			put("???", "I"); put("???", "i");
			put("???", "J"); put("???", "j");
			put("???", "K"); put("???", "k");
			put("???", "L"); put("???", "l");
			put("???", "M"); put("???", "m");
			put("???", "N"); put("???", "n");
			put("???", "O"); put("???", "o");
			put("???", "P"); put("???", "p");
			put("???", "Q"); put("???", "q");
			put("???", "R"); put("???", "r");
			put("???", "S"); put("???", "s");
			put("???", "T"); put("???", "t");
			put("???", "U"); put("???", "u");
			put("???", "V"); put("???", "v");
			put("???", "W"); put("???", "w");
			put("???", "X"); put("???", "x");
			put("???", "Y"); put("???", "y");
			put("???", "Z"); put("???", "z");
			// ???????????????
			put("???", "0");
			put("???", "1");
			put("???", "2");
			put("???", "3");
			put("???", "4");
			put("???", "5");
			put("???", "6");
			put("???", "7");
			put("???", "8");
			put("???", "9");
			// ???????????????
			put("???", "+"); put("???", "-"); put("???", "*"); put("???", "/"); put("???", "=");
			put("???", "%"); put("???", "$"); put("???", "\\");
			put("???", ","); put("???", "."); put("???", ";"); put("???", ":");
			put("???", "&"); put("???", "|"); put("???", "!"); put("???", "?");
			put("???", "#"); put("???", "@"); put("???", "_");
			put("???", "'"); put("???", "\"");
			put("???", "("); put("???", ")");
			put("???", "<"); put("???", ">");
			put("???", "<"); put("???", ">");
			put("???", "["); put("???", "]");
			// ??????????????????
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???"); put("???", "???");
			put("??????", "???"); put("??????", "???"); put("??????", "???"); put("??????", "???"); put("??????", "???");
			put("??????", "???"); put("??????", "???"); put("??????", "???"); put("??????", "???"); put("??????", "???");
			put("??????", "???"); put("??????", "???"); put("??????", "???"); put("??????", "???"); put("??????", "???");
			put("??????", "???"); put("??????", "???"); put("??????", "???"); put("??????", "???"); put("??????", "???");
			put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???");
			put("???", "???"); put("???", "???");
		}

	});

	private static InetAddress ip = null;

	// TODO: private
	public static boolean exit = false;

	static {

		shutdown("exit", () -> _0.exit = true);

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			log.trace("", e);
		});

	}

	private _0() {
	}

	public static final boolean exit() {
		return exit;
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????
	 *
	 * @return true: ?????????????????????????????????false:?????????????????????
	 */
	public static final boolean main() {
		return 1 == Thread.currentThread().threadId();
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????
	 *
	 * @return ?????????????????????????????????????????????????????????
	 */
	public static final String methodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @return
	 */
	public static final StackTraceElement current() {
		return Thread.currentThread().getStackTrace()[2];
	}

	/**
	 * ????????????????????????????????????????????????
	 *
	 * @return
	 */
	public static final StackTraceElement caller() {
		return Thread.currentThread().getStackTrace()[3];
	}

	/**
	 * ????????????????????????????????????????????????????????????
	 *
	 * @param t ?????????
	 * @return ????????????????????????????????????
	 */
	public static final String stackTrace(final Throwable t) {

		String val = null;

		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {

			t.printStackTrace(pw);
			flush(pw);

			val = sw.toString();

		} catch (IOException e) {
			log.trace("", e);
		}

		return val;

	}

	/**
	 * ?????????????????????????????????????????????????????????????????????
	 *
	 * @param flushable ???????????????????????????????????????
	 */
	public static final void flush(final Flushable flushable) {

		if (null != flushable) {

			try {

				flushable.flush();

			} catch (IOException e) {
				log.trace("flush failed.", e);
			}

		}

	}

	/**
	 * ???????????????????????????????????????????????????????????????
	 *
	 * @param closeable ????????????????????????????????????
	 */
	public static final void close(final AutoCloseable closeable) {

		if (null != closeable) {

			// ?????????????????????????????????????????????????????????
			if (closeable instanceof Connection) {

				try {

					if (!((Connection)closeable).getAutoCommit()) {
						((Connection)closeable).rollback();
					}

				} catch (SQLException e) {
					log.trace("rollback failed.", e);
				}

			}

			try {

				closeable.close();

			} catch (Exception e) {
				log.trace("close failed.", e);
			}

		}

	}

	public static boolean empty(final Object obj) {

		boolean empty = false;

		if (null == obj) {
			empty = true;

		} else if (obj instanceof Collection) {
			empty = ((Collection<?>)obj).isEmpty();

		} else if (obj instanceof Map) {
			empty = ((Map<?, ?>)obj).isEmpty();

		} else if (obj instanceof Iterator) {
			empty = !((Iterator<?>)obj).hasNext();

		} else {
			throw new UnsupportedOperationException(obj.getClass().getName());
		}

		return empty;

	}

	/**
	 * ??????????????????null????????????????????????????????????????????????
	 *
	 * @param T
	 * @param v ???
	 * @return ??????????????????null???????????????????????????
	 */
	@SafeVarargs
	public static final <T> T nvl(final T... v) {

		T ret = null;

		for (T obj : v) {

			if (null == obj) {
				continue;
			}

			ret = (T)obj;
			break;

		}

		return ret;

	}

	/**
	 * <pre>
	 * ?????????????????????????????????????????????????????????????????????
	 * </pre>
	 *
	 * @param T
	 * @param v ???
	 * @return ????????????????????????????????????????????????
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T nvl(final Collection<T> v) {
		return (T)nvl(v.toArray());
	}

	@SuppressWarnings("rawtypes")
	public static final boolean eq(final Comparable o1, final Comparable o2) {
		return 0 == compare(o1, o2);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final int compare(final Comparable o1, final Comparable o2) {

		int compare = 0;

		if (null == o1 && null != o2) {
			compare = -1;

		} else if (null == o1 && null == o2) {
			compare = 0;

		} else if (null != o1 && null == o2) {
			compare = 1;

		} else if (o1 instanceof Number && o2 instanceof Number) {
			// TODO:

		} else {
			compare = o1.compareTo(o2);
		}

		return compare;

	}

	public static final int min(final int... vals) {

		int min = Integer.MAX_VALUE;

		for (int val : vals) {
			min = Math.min(min, val);
		}

		return min;

	}

	public static final int max(final int... vals) {

		int max = Integer.MIN_VALUE;

		for (int val : vals) {
			max = Math.max(max, val);
		}

		return max;

	}

	public static final long min(final long... vals) {

		long min = Long.MAX_VALUE;

		for (long val : vals) {
			min = Math.min(min, val);
		}

		return min;

	}

	public static final long max(final long... vals) {

		long max = Long.MIN_VALUE;

		for (long val : vals) {
			max = Math.max(max, val);
		}

		return max;

	}

	public static final String size(final long size) {
		return size(size, true);
	}

	@SuppressWarnings("unchecked")
	public static final <T> T select(final Object obj, final Object... keys) {

		Object select = obj;

		for (Object key : keys) {

			if (null == select) {
				break;
			}

			if (select instanceof Map) {
				select = ((Map<?, ?>)select).get(key);

			} else if (select instanceof List) {
				select = ((List<?>)select).get(((Integer)key).intValue());

			} else {
				throw new IllegalArgumentException(String.valueOf(key));
			}

		}

		return (T)select;

	}

	public static final String size(final long size, final boolean iec) {

		double d = iec ? 1024.0d : 1000.0d;

		double v = size;
		double k = v / d;
		double m = k / d;
		double g = m / d;
		double t = g / d;

		String s = null;
		synchronized (fmt_size) {
			if (1.0d <= t) {
				s = fmt_size.format(t) + (iec ? " TiB" : " TB");
			} else if (1.0d <= g) {
				s = fmt_size.format(g) + (iec ? " GiB" : " GB");
			} else if (1.0d <= m) {
				s = fmt_size.format(m) + (iec ? " MiB" : " MB");
			} else if (1.0d <= k) {
				s = fmt_size.format(k) + (iec ? " KiB" : " KB");
			} else {
				s = fmt_size.format(v) + (iec ? "   B" : "  B");
			}
		}

		return s;

	}

	@SuppressWarnings("unchecked")
	public static final <T, R> R cast(final Class<R> type, final T val) {

		Object ret = null;

		// TODO: cast
		if (null != val) {
			if      ((byte.class  == type || Byte.class    == type) && (val instanceof Byte                                                                         )) ret = (byte)val;
			else if ((short.class == type || Short.class   == type) && (val instanceof Byte || val instanceof Short                                                 )) ret = (short)val;
			else if ((int.class   == type || Integer.class == type) && (val instanceof Byte || val instanceof Short || val instanceof Integer                       )) ret = (int)val;
			else if ((long.class  == type || Long.class    == type) && (val instanceof Byte || val instanceof Short || val instanceof Integer || val instanceof Long)) ret = (long)val;
			else if ((                       String.class  == type) && (val instanceof CharSequence                                                                 )) ret = val.toString();
			else throw new IllegalArgumentException(type.toString());
		}

		// null -> 0
		if (null == ret && type.isPrimitive()) {
			if      (boolean.class == type) ret = Boolean.FALSE;
			else if (byte.class    == type) ret = Byte.valueOf((byte)0);
			else if (char.class    == type) ret = Character.valueOf((char)0);
			else if (short.class   == type) ret = Short.valueOf((short)0);
			else if (int.class     == type) ret = Integer.valueOf(0);
			else if (long.class    == type) ret = Long.valueOf(0L);
			else if (float.class   == type) ret = Float.valueOf(0.0f);
			else if (double.class  == type) ret = Double.valueOf(0.0d);
			else throw new IllegalArgumentException(type.toString());
		}

		return (R)ret;

	}

	public static String trim(final String val) {
		return null == val ? null : val.replaceAll("^" + regex_spaces + "|" + regex_spaces + "$", "");
	}

	public static String normalize(final String val) {

		String ret = val;

		if (null != ret) {
			for (Entry<String, String> entry : char_conv.entrySet()) {
				ret = ret.replace(entry.getKey(), entry.getValue());
			}
		}

		return ret;

	}

	public static final byte[] sha256(final byte[] data) {

		byte[] hash = null;
		try {

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(data);

			hash = md.digest();

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}

		return hash;

	}

	public static final byte[] sha512(final byte[] data) {

		byte[] hash = null;
		try {

			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(data);

			hash = md.digest();

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}

		return hash;

	}

	public static void daemon(final Runnable impl) {
		Thread thread = new Thread(impl);
		thread.setDaemon(true);
		thread.start();
	}

	public static void shutdown(final String name, final Runnable impl) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.trace("shotdown hook. {}", name);
			impl.run();
		}));
	}

	public static <T, R> void clipboard(final Consumer<T> listener) {

		Set<DataFlavor> flavors = new HashSet<>();
		flavors.add(DataFlavor.stringFlavor);
//		flavors.add(DataFlavor.imageFlavor);
//		flavors.add(DataFlavor.javaFileListFlavor);
//		flavors.add(DataFlavor.selectionHtmlFlavor);
//		flavors.add(DataFlavor.fragmentHtmlFlavor);
//		flavors.add(DataFlavor.allHtmlFlavor);
//		flavors.addAll(List.of(transferable.getTransferDataFlavors()));

		daemon(() -> {

			Object prev = null;

			while (!exit()) {

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

					@SuppressWarnings("unchecked")
					T t = (T)data;

					listener.accept(t);

				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}

			}

		});

	}

	public static final InetAddress ip()
			throws IOException {

		if (null == ip) {

			List<InetAddress> ips = NetworkInterface.networkInterfaces()
					.parallel()
					.filter(new NetworkInterfaceFilter())
					.map(NetworkInterface::getInterfaceAddresses)
					.flatMap(Collection::stream)
					.map(InterfaceAddress::getAddress)
					.filter(e -> e instanceof Inet4Address)
					.filter(e -> !e.isLoopbackAddress())
					.toList();

			if (0 == ips.size()) {
				ip = InetAddress.getLocalHost();

			} else if (1 == ips.size()) {
				ip = ips.get(0);

			} else {
				ips.stream().forEach(ip -> log.debug("{} {}", ip, ip.getCanonicalHostName()));
				throw new UnsupportedOperationException();
			}

		}

		return ip;

	}

	public static int ip(InetAddress addr) {

		if (!(addr instanceof Inet4Address)) {
			throw new UnsupportedOperationException(addr.toString());
		}

		return ip(addr.getAddress());

	}

	public static int ip(byte[] bytes) {

		int ip = 0;

		for (int i = 0; i < bytes.length; i++) {
			ip = ip | ((bytes[i] & 0xff) << ((bytes.length * 8) - ((i + 1) * 8)));
		}

		return ip;

	}

	public static boolean tcp(InetAddress addr, int port) {

		int available = -1;

		try (Socket sock = new Socket(addr, port)) {

			available = sock.getInputStream().available();

		} catch (ConnectException e) {
//			log.trace("", e); // ??????????????????????????????

		} catch (IOException e) {
			log.trace("", e);
		}

		return -1 < available;

	}

	public static int subnetmask(InterfaceAddress ifaddr) {
		return subnetmask(ifaddr.getNetworkPrefixLength());
	}

	public static int subnetmask(short networkPrefixLength) {

		int subnetmask = 0;

		for (int i = 0; i < 32; i++) {
			subnetmask  = subnetmask << 1;
			subnetmask |= (i < networkPrefixLength) ? 1 : 0;
		}

		return subnetmask;

	}

	public static final boolean icmp(final InetAddress addr)
			throws IOException {

		boolean ping = false;
		try {

			String[] commands = null;
			if (windows) {
				commands = new String[] {"ping", "-n", "1", "-w", "1000", addr.getHostAddress()};
			} else if (linux) {
				commands = new String[] {"ping", "-c", "1", "-W",    "1", addr.getHostAddress()};
			} else {
				throw new UnsupportedOperationException();
			}

			ProcessBuilder pb = new ProcessBuilder(commands);
			pb.redirectErrorStream(true);

			Process process = pb.start();

			ping = 0 == process.waitFor();

		} catch (IOException e) {
			log.trace("", e);

		} catch (InterruptedException e) {
			log.trace("", e);
		}

		if (!ping) {
			ping = addr.isReachable(1000);
		}

		return ping;

	}

	public static final String ymdhms() {
		return ymdhms(System.currentTimeMillis());
	}

	public static final synchronized String ymdhms(final long millis) {

		String val = null;

		synchronized (fmt_ymdhms) {
			val = fmt_ymdhms.format(new Date(millis));
		}

		return val;

	}

	public static final String ymdhmss() {
		return ymdhmss(System.currentTimeMillis());
	}

	public static final synchronized String ymdhmss(final long millis) {

		String val = null;

		synchronized (fmt_ymdhmss) {
			val = fmt_ymdhmss.format(new Date(millis));
		}

		return val;

	}

	public static final long latest(final BasicFileAttributes attrs) {

		long[] times = new long[3];
		times[0] = attrs.creationTime().toMillis();
		times[1] = attrs.lastAccessTime().toMillis();
		times[2] = attrs.lastModifiedTime().toMillis();

		return max(times);

	}

	public static Path uncpath(final InetAddress host, final Path path)
			throws IOException {

		String unc = null;

		String str = path.toString().replace("\\", "/");
		if (str.startsWith("//")) {
			unc = str;

		} else if (str.startsWith("/")) {
			unc = "\\\\" + host.getHostAddress() + str;

		} else if (str.matches("^[A-Za-z]:.*$")) {

			StringBuilder sb = new StringBuilder();
			sb.append(str.replaceAll("(?<letter>[A-Za-z]):(?<path>.*)", "${letter}").toLowerCase());
			sb.append("$");
			sb.append(str.replaceAll("(?<letter>[A-Za-z]):(?<path>.*)", "${path}"));

			unc = "\\\\" + host.getHostAddress() + "/" + sb.toString();

		} else {
			throw new UnsupportedOperationException(str);
		}

		return Path.of(unc);

	}

	public static String hostpath(final Path unc) {

		String path = unc.toString().replace('\\', '/');

		if (path.startsWith("//")) {
			path = path.replaceAll("^//[^/]+", "");
		}

		if (path.matches("/[A-Za-z]\\$")) {

			StringBuilder sb = new StringBuilder();
			sb.append(path.replaceAll("/(?<letter>[A-Za-z])\\$(?<path>.*)$", "${letter}").toUpperCase());
			sb.append(":");
			sb.append(path.replaceAll("/(?<letter>[A-Za-z])\\$(?<path>.*)$", "${path}"));

			path = sb.toString();

		}

		return path;

	}

	public static long delay(final String syntax) {

		long delay = -1;

		// split
		String[] date = null;
		String[] time = null;
		{

			String syntax_ = syntax.replace('/', '-');

			if (-1 < syntax_.indexOf(" ")) {
				String[] items = syntax_.split(" ");
				date = items[0].split("-");
				time = items[1].split(":");

			} else if (-1 < syntax_.indexOf("-")) {
				date = syntax_.split("-");

			} else if (-1 < syntax_.indexOf(":")) {
				time = syntax_.split(":");

			} else {
				throw new IllegalArgumentException(syntax);
			}

			if (null != date && 3 != date.length) {
				throw new IllegalArgumentException(syntax);
			}
			if (null != time && 3 != time.length) {
				throw new IllegalArgumentException(syntax);
			}

		}

		long now = System.currentTimeMillis();

		if (null != date && null != time) {
			throw new UnsupportedOperationException(syntax);

		} else if (null != date) {
			throw new UnsupportedOperationException(syntax);

		} else if (null != time) {

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(now);

			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
			cal.set(Calendar.MINUTE,      Integer.parseInt(time[1]));
			cal.set(Calendar.SECOND,      Integer.parseInt(time[2]));
			cal.set(Calendar.MILLISECOND, 0);

			long next = cal.getTimeInMillis();
			while (next - now < 0) {

				cal.add(Calendar.DATE, 1);

				next = cal.getTimeInMillis();

			}

			delay = next - now;

		} else {
			throw new IllegalArgumentException(syntax);
		}

		return delay;

	}

	public static String bin(final byte v) {

		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			s.append(0 < (v & (1 << (7 - i))) ? '1' : '0');
		}

		return s.toString();

	}

	public static String bin(final byte[] v) {

		StringBuilder s = new StringBuilder();
		for (byte b : v) {
			s.append(bin(b));
		}

		return s.toString();

	}

	public static String bin(final short v) {

		StringBuilder s = new StringBuilder();
		s.append(bin((byte)(0xff & (v >> 8))));
		s.append(bin((byte)(0xff & (v >> 0))));

		return s.toString();

	}

	public static String bin(final int v) {

		StringBuilder s = new StringBuilder();
		s.append(bin((short)(0xffff & (v >> 16))));
		s.append(bin((short)(0xffff & (v >>  0))));

		return s.toString();

	}

	public static String bin(final long v) {

		StringBuilder s = new StringBuilder();
		s.append(bin((int)(0xffffffff & (v >> 32))));
		s.append(bin((int)(0xffffffff & (v >>  0))));

		return s.toString();

	}

	public static String hex(final byte v) {
		return String.format("%02x", v);
	}

	public static String hex(final byte[] v) {

		StringBuilder s = new StringBuilder();
		for (byte b : v) {
			s.append(hex(b));
		}

		return s.toString();

	}

	public static String hex(final short v) {

		StringBuilder s = new StringBuilder();
		s.append(hex((byte)(0xff & (v >> 8))));
		s.append(hex((byte)(0xff & (v >> 0))));

		return s.toString();

	}

	public static String hex(final int v) {

		StringBuilder s = new StringBuilder();
		s.append(hex((short)(0xffff & (v >> 16))));
		s.append(hex((short)(0xffff & (v >>  0))));

		return s.toString();

	}

	public static String hex(final long v) {

		StringBuilder s = new StringBuilder();
		s.append(hex((int)(0xffffffff & (v >> 32))));
		s.append(hex((int)(0xffffffff & (v >>  0))));

		return s.toString();

	}

}
