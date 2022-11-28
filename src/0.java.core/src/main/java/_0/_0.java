package _0;

import java.io.BufferedReader;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	/** 論理プロセッサ数 */
	public static final int availableProcessors = Runtime.getRuntime().availableProcessors();

	private static final SimpleDateFormat fmt_ymdhmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static final DecimalFormat fmt_size = new DecimalFormat("#,##0.00");

	public static final String regex_spaces = "[\\r\\n\\t\\s　 ]+";

	public static final Map<String, String> char_conv = Collections.unmodifiableMap(new HashMap<>() {

		private static final long serialVersionUID = 1L;

		{
			// 英字は半角
			put("Ａ", "A"); put("ａ", "a");
			put("Ｂ", "B"); put("ｂ", "b");
			put("Ｃ", "C"); put("ｃ", "c");
			put("Ｄ", "D"); put("ｄ", "d");
			put("Ｅ", "E"); put("ｅ", "e");
			put("Ｆ", "F"); put("ｆ", "f");
			put("Ｇ", "G"); put("ｇ", "g");
			put("Ｈ", "H"); put("ｈ", "h");
			put("Ｉ", "I"); put("ｉ", "i");
			put("Ｊ", "J"); put("ｊ", "j");
			put("Ｋ", "K"); put("ｋ", "k");
			put("Ｌ", "L"); put("ｌ", "l");
			put("Ｍ", "M"); put("ｍ", "m");
			put("Ｎ", "N"); put("ｎ", "n");
			put("Ｏ", "O"); put("ｏ", "o");
			put("Ｐ", "P"); put("ｐ", "p");
			put("Ｑ", "Q"); put("ｑ", "q");
			put("Ｒ", "R"); put("ｒ", "r");
			put("Ｓ", "S"); put("ｓ", "s");
			put("Ｔ", "T"); put("ｔ", "t");
			put("Ｕ", "U"); put("ｕ", "u");
			put("Ｖ", "V"); put("ｖ", "v");
			put("Ｗ", "W"); put("ｗ", "w");
			put("Ｘ", "X"); put("ｘ", "x");
			put("Ｙ", "Y"); put("ｙ", "y");
			put("Ｚ", "Z"); put("ｚ", "z");
			// 数字は半角
			put("０", "0");
			put("１", "1");
			put("２", "2");
			put("３", "3");
			put("４", "4");
			put("５", "5");
			put("６", "6");
			put("７", "7");
			put("８", "8");
			put("９", "9");
			// 記号は半角
			put("＋", "+"); put("－", "-"); put("＊", "*"); put("／", "/"); put("＝", "=");
			put("％", "%"); put("＄", "$"); put("￥", "\\");
			put("，", ","); put("．", "."); put("；", ";"); put("：", ":");
			put("＆", "&"); put("｜", "|"); put("！", "!"); put("？", "?");
			put("＃", "#"); put("＠", "@"); put("＿", "_");
			put("’", "'"); put("”", "\"");
			put("（", "("); put("）", ")");
			put("＜", "<"); put("＞", ">");
			put("〈", "<"); put("〉", ">");
			put("［", "["); put("］", "]");
			// 日本語は全角
			put("ｱ", "ア"); put("ｲ", "イ"); put("ｳ", "ウ"); put("ｴ", "エ"); put("ｵ", "オ");
			put("ｶ", "カ"); put("ｷ", "キ"); put("ｸ", "ク"); put("ｹ", "ケ"); put("ｺ", "コ");
			put("ｻ", "サ"); put("ｼ", "シ"); put("ｽ", "ス"); put("ｾ", "セ"); put("ｿ", "ソ");
			put("ﾀ", "ダ"); put("ﾁ", "チ"); put("ﾂ", "ツ"); put("ﾃ", "テ"); put("ﾄ", "ト");
			put("ﾅ", "ナ"); put("ﾆ", "ニ"); put("ﾇ", "ヌ"); put("ﾈ", "ネ"); put("ﾉ", "ノ");
			put("ﾊ", "ハ"); put("ﾋ", "ヒ"); put("ﾌ", "フ"); put("ﾍ", "ヘ"); put("ﾎ", "ホ");
			put("ﾏ", "マ"); put("ﾐ", "ミ"); put("ﾑ", "ム"); put("ﾒ", "メ"); put("ﾓ", "モ");
			put("ﾔ", "ヤ"); put("ﾕ", "ユ"); put("ﾖ", "ヨ");
			put("ﾗ", "ラ"); put("ﾘ", "リ"); put("ﾙ", "ル"); put("ﾚ", "レ"); put("ﾛ", "ロ");
			put("ﾜ", "ワ"); put("ｦ", "ヲ"); put("ﾝ", "ン");
			put("ｧ", "ァ"); put("ｨ", "ィ"); put("ｩ", "ゥ"); put("ｪ", "ェ"); put("ｫ", "ォ");
			put("ｬ", "ャ"); put("ｭ", "ュ"); put("ｮ", "ョ");
			put("ｶﾞ", "ガ"); put("ｷﾞ", "ギ"); put("ｸﾞ", "グ"); put("ｹﾞ", "ゲ"); put("ｺﾞ", "ゴ");
			put("ﾀﾞ", "ダ"); put("ﾁﾞ", "ヂ"); put("ﾂﾞ", "ヅ"); put("ﾃﾞ", "デ"); put("ﾄﾞ", "ド");
			put("ﾊﾞ", "バ"); put("ﾋﾞ", "ビ"); put("ﾌﾞ", "ブ"); put("ﾍﾞ", "ベ"); put("ﾎﾞ", "ボ");
			put("ﾊﾟ", "パ"); put("ﾋﾟ", "ピ"); put("ﾌﾟ", "プ"); put("ﾍﾟ", "ペ"); put("ﾎﾟ", "ポ");
			put("～", "〜"); put("ｰ", "ー");
			put("､", "、"); put("｡", "。");
			put("｢", "「"); put("｣", "」");
		}

	});

	private static InetAddress gw = null;

	private static InetAddress ip = null;

	private _0() {
	}

	/**
	 * 現在のスレッドがメインスレッドかどうかを返します。
	 *
	 * @return true: メインスレッドの場合、false:それ以外の場合
	 */
	public static final boolean main() {
		return 1 == Thread.currentThread().getId();
	}

	/**
	 * このメソッドを呼び出したメソッドの名前を返します。
	 *
	 * @return このメソッドを呼び出したメソッドの名前
	 */
	public static final String methodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

	/**
	 * 自身のスタックを返します。
	 *
	 * @return
	 */
	public static final StackTraceElement current() {
		return Thread.currentThread().getStackTrace()[2];
	}

	/**
	 * 呼び出し元のスタックを返します。
	 *
	 * @return
	 */
	public static final StackTraceElement caller() {
		return Thread.currentThread().getStackTrace()[3];
	}

	/**
	 * スタックトレースを文字列として返します。
	 *
	 * @param t スロー
	 * @return スタックトレースの文字列
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
	 * フラッシュ可能オブジェクトをフラッシュします。
	 *
	 * @param flushable フラッシュ可能オブジェクト
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
	 * クローズ可能オブジェクトをクローズします。
	 *
	 * @param closeable クローズ可能オブジェクト
	 */
	public static final void close(final AutoCloseable closeable) {

		if (null != closeable) {

			// コネクションの場合はロールバックも実行
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

	/**
	 * 最初に現れるnullではないオブジェクトを返します。
	 *
	 * @param T
	 * @param v 値
	 * @return 最初に現れるnullでないオブジェクト
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
	 * 最初に現れる空ではないオブジェクトを返します。
	 * 空かどうかは{@link #empty}で判定されます。
	 * </pre>
	 *
	 * @param T
	 * @param v 値
	 * @return 最初に現れる空でないオブジェクト
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

	public static final String hex(final byte[] bytes) {

		StringBuilder hex = new StringBuilder();

		for (byte b : bytes) {
			hex.append(String.format("%02x", b & 0xff) );
		}

		return hex.toString();

	}

	public static final String size(final long size) {
		return size(size, true);
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

	public static final InetAddress gw()
			throws IOException {

		if (null == gw) {
			gw = gw_();
		}

		return gw;

	}

	private static synchronized final InetAddress gw_()
			throws IOException {

		if (null == gw) {

			InetAddress gw_ = null;

			String[] commands = null;
			if (windows) {

				// TODO: gw
				throw new UnsupportedOperationException();

			} else if (linux) {

				commands = new String[] {"ip", "r"};

				ProcessBuilder pb = new ProcessBuilder(commands);
				pb.redirectErrorStream(true);

				Process process = pb.start();

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), encoding))) {

					String line = null;
					while (null != (line = reader.readLine())) {

						line = line.replaceAll("^" + regex_spaces, "");
						line = line.replaceAll(regex_spaces + "$", "");
						line = line.toLowerCase();

						String[] items = line.split(regex_spaces );

						if (!"default".equals(items[0])) {
							continue;
						}

						gw_ = InetAddress.getByName(items[2]);
						break;

					}

				}

				try {
					int code = process.waitFor();
					if (0 != code) {
						log.trace("{} {}", code, String.join(" ", commands));
					}
				} catch (InterruptedException e) {
					log.trace("", e);
				}

			} else {
				throw new UnsupportedOperationException();
			}

			gw = gw_;

		}

		return gw;

	}

	public static final InetAddress ip()
			throws IOException {

		if (null == ip) {
			ip = ip_();
		}

		return ip;

	}

	private static synchronized final InetAddress ip_()
			throws IOException {

		if (null == ip) {

			InetAddress ip_ = null;

			InetAddress gw = gw();
			if (null == gw) {
//				ip_ = InetAddress.getLocalHost();
				throw new IllegalArgumentException();

			} else {

				List<InterfaceAddress> ifaddrs = new LinkedList<>();
				Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
						.parallel()
						.map(NetworkInterface::getInterfaceAddresses)
						.forEach(ifaddrs::addAll);

				ip_ = ifaddrs.stream()
						.parallel()
						.map(InterfaceAddress::getAddress)
						.filter(e -> gw.getHostAddress().substring(0, 3).equals(e.getHostAddress().substring(0, 3))) // TODO: subnetmask
						.findAny()
						.get();

			}

			ip = ip_;

		}

		return ip;

	}

	public static final boolean icmp(final InetAddress addr)
			throws IOException {

		boolean ping = false;

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

		try {
			ping = 0 == process.waitFor();
		} catch (InterruptedException e) {
			log.trace("", e);
		}

		if (!ping) {
			ping = addr.isReachable(1000);
		}

		return ping;

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

	public static String hostpath(Path unc) {

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

}
