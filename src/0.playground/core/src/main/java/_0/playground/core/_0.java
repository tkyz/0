package _0.playground.core;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class _0 {

	public static final long    start = sys.now();
	public static final Charset utf8  = StandardCharsets.UTF_8;

	public static final class debug {

		private static final Path src_dir = user.home.resolve("src/0.playground/main/src/main/java");

		public static final Path mksrcdir(final String subdir) {

			Path dir = null;

			if (Files.exists(src_dir)) {

				dir = src_dir.resolve(subdir);

				try {
					Files.createDirectories(dir);
				} catch (IOException e) {
					dir = null;
				}

			}

			return dir;

		}

		public static final void println() {
			println(false);
		}

		public static final void println(final boolean all) {

			Comparator<Entry<?, ?>> sort = (o1, o2) -> compare(o1.getKey().toString(), o2.getKey().toString());

			String path_separator = System.getProperty("path.separator");

			System.out.println("---");

			// env
			if (all) {

				Set<String> set = new HashSet<>();
				set.add("classpath");
				set.add("path");
				set.add("pathext");
				set.add("psmodulepath");
				set.add("session_manager");

				System.out.println("  env:");
				for (Entry<String, String> entry : System.getenv().entrySet().stream().sorted(sort).collect(Collectors.toList())) {

					if (set.contains(entry.getKey().toLowerCase())) {

						String[] paths = entry.getValue().toString().split(Pattern.quote(path_separator));
						System.out.println("    " + entry.getKey() + ":");
						for (String path : paths) {
							System.out.println("      - \"" + path + "\"");
						}

					} else {
						System.out.println("    " + entry.getKey() + ": \"" + entry.getValue() + "\"");
					}

				}

			}

			// prop
			if (all) {

				Set<String> set = new HashSet<>();
				set.add("java.class.path");
				set.add("java.library.path");

				System.out.println("  prop:");
				for (Entry<Object, Object> entry : System.getProperties().entrySet().stream().sorted(sort).collect(Collectors.toList())) {

					if (set.contains(entry.getKey().toString().toLowerCase())) {

						String[] paths = entry.getValue().toString().split(Pattern.quote(path_separator));
						System.out.println("    " + entry.getKey() + ":");
						for (String path : paths) {
							System.out.println("      - \"" + path + "\"");
						}

					} else {
						System.out.println("    " + entry.getKey() + ": \"" + entry.getValue() + "\"");
					}

				}

			}

			if (true) {

				Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();

				List<Thread> keys = new LinkedList<>(map.keySet());
				Collections.sort(keys, (o1, o2) -> (int)(o1.threadId() - o2.threadId()));

				System.out.println("  threads:");
				for (Thread thread : keys) {

					List<StackTraceElement> stacks = new LinkedList<>();
					{

						StackTraceElement[] items = map.get(thread);
						for (StackTraceElement item : items) {

							boolean skip = false;
							skip |= item.toString().startsWith("java.");
							skip |= item.toString().endsWith("(Unknown Source)");
							skip &= !all;

							if (skip) {
								continue;
							}

							stacks.add(item);

						}

						Collections.reverse(stacks);

					}

					if (empty(stacks)) {
						continue;
					}

					System.out.println("    - id: "   + thread.threadId());
					System.out.println("      name: " + thread.getName());
					System.out.println("      stacktrace:");
					for (StackTraceElement stack : stacks) {
						System.out.println("        - " + stack);
					}

				}

			}

		}

		public static final String age() {

			long time = sys.now() - start;
			int  unit = 0;

			unit = 1000; long SSS  = time % unit; time /= unit;
			unit =   60; long ss   = time % unit; time /= unit;
			unit =   60; long mm   = time % unit; time /= unit;
			unit =   24; long HH   = time % unit; time /= unit;

			return String.format("%02d:%02d:%02d.%03d", HH, mm, ss, SSS);

		}

	}

	public static final class sys {

		public static final int cpu_core = Runtime.getRuntime().availableProcessors();

		public static final void beep() {
			Toolkit.getDefaultToolkit().beep();
		}

		public static final long now() {
			return System.currentTimeMillis();
		}

	}

	public static final class user {

		public static final String name = System.getProperty("user.name");
		public static final Path   home = Path.of(System.getProperty("user.home"));
		public static final String fpr  = System.getenv().get("openpgp4fpr");
		public static final byte[] fpr_ = null == fpr ? null : HexFormat.of().parseHex(fpr);

	}

	public static final class fs {

		/** 外部コマンド依存 */
		@Deprecated
		public static final String mime_type(final Path path)
				throws InterruptedException, IOException {

			Process proc = null;
			int     ret  = -1;
			synchronized (path.getFileSystem()) {
				proc = Runtime.getRuntime().exec(new String[] {"file", "--brief", "--mime-type", path.toString()});
				ret = proc.waitFor();
			}

			String s = null;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

				(0 == ret ? proc.getInputStream() : proc.getErrorStream()).transferTo(out);

				s = new String(out.toByteArray());

			}

			if (0 != ret) {
				throw new IOException(s);
			}

			return trim(s).toLowerCase();
//			return Files.probeContentType(path);

		}

		/** ファイルシステム依存 */
		@Deprecated
		public static final long ino(final Path file)
				throws IOException {
			return Files.exists(file) ? ino(Files.getFileAttributeView(file, BasicFileAttributeView.class).readAttributes()) : -1;
		}

		/** ファイルシステム依存 */
		@Deprecated
		public static final long ino(final BasicFileAttributes attrs) {

			String key = attrs.fileKey().toString();
			key = key.substring(key.indexOf("ino=") + 4, key.indexOf(")"));

			return Long.parseLong(key);

		}

		/** ファイルシステム依存 */
		@Deprecated
		public static final Integer nlink(final Path file) {

			Integer nlink = null;

			if (Files.exists(file)) {
				try {
					nlink = (Integer)Files.getAttribute(file, "unix:nlink");
				} catch (IOException e) {
					// pass
				}
			}

			return nlink;

		}

	}

	public static final class ansi {

		/** 未判定 */
		@Deprecated
		private static final boolean support = true;

		private static final String prefix = "\u001B[";
		private static final String suffix = "m";

		public static final String reset = esc( 0);

		public static final class style {
			public static final String bold       = esc( 1);
			public static final String faint      = esc( 2);
			public static final String italic     = esc( 3);
			public static final String underline  = esc( 4);
			public static final String blink      = esc( 5);
			public static final String blink2     = esc( 6);
			public static final String invert     = esc( 7);
			public static final String hide       = esc( 8);
			public static final String strike     = esc( 9);
		}

		public static final class fg {

			public static final String black      = esc(30);
			public static final String red        = esc(31);
			public static final String green      = esc(32);
			public static final String yellow     = esc(33);
			public static final String blue       = esc(34);
			public static final String magenta    = esc(35);
			public static final String cyan       = esc(36);
			public static final String white      = esc(37);

			public static final String red_3f     = esc(38, 2, 0xff, 0x3f, 0x3f);
			public static final String green_3f   = esc(38, 2, 0x3f, 0xff, 0x3f);
			public static final String yellow_3f  = esc(38, 2, 0xff, 0xff, 0x3f);
			public static final String blue_3f    = esc(38, 2, 0x3f, 0x3f, 0xff);
			public static final String magenta_3f = esc(38, 2, 0xff, 0x3f, 0xff);
			public static final String cyan_3f    = esc(38, 2, 0x3f, 0xff, 0xff);
			public static final String gray_3f    = esc(38, 2, 0x3f, 0x3f, 0x3f);

			public static final String red_7f     = esc(38, 2, 0xff, 0x7f, 0x7f);
			public static final String green_7f   = esc(38, 2, 0x7f, 0xff, 0x7f);
			public static final String yellow_7f  = esc(38, 2, 0xff, 0xff, 0x7f);
			public static final String blue_7f    = esc(38, 2, 0x7f, 0x7f, 0xff);
			public static final String magenta_7f = esc(38, 2, 0xff, 0x7f, 0xff);
			public static final String cyan_7f    = esc(38, 2, 0x7f, 0xff, 0xff);
			public static final String gray_7f    = esc(38, 2, 0x7f, 0x7f, 0x7f);

			public static final String red_bf     = esc(38, 2, 0xff, 0xbf, 0xbf);
			public static final String green_bf   = esc(38, 2, 0xbf, 0xff, 0xbf);
			public static final String yellow_bf  = esc(38, 2, 0xff, 0xff, 0xbf);
			public static final String blue_bf    = esc(38, 2, 0xbf, 0xbf, 0xff);
			public static final String magenta_bf = esc(38, 2, 0xff, 0xbf, 0xff);
			public static final String cyan_bf    = esc(38, 2, 0xbf, 0xff, 0xff);
			public static final String gray_bf    = esc(38, 2, 0xbf, 0xbf, 0xbf);

		}

		public static final class bg {

			public static final String black      = esc(40);
			public static final String red        = esc(41);
			public static final String green      = esc(42);
			public static final String yellow     = esc(43);
			public static final String blue       = esc(44);
			public static final String magenta    = esc(45);
			public static final String cyan       = esc(46);
			public static final String white      = esc(47);

			public static final String red_3f     = esc(48, 2, 0xff, 0x3f, 0x3f);
			public static final String green_3f   = esc(48, 2, 0x3f, 0xff, 0x3f);
			public static final String yellow_3f  = esc(48, 2, 0xff, 0xff, 0x3f);
			public static final String blue_3f    = esc(48, 2, 0x3f, 0x3f, 0xff);
			public static final String magenta_3f = esc(48, 2, 0xff, 0x3f, 0xff);
			public static final String cyan_3f    = esc(48, 2, 0x3f, 0xff, 0xff);
			public static final String gray_3f    = esc(48, 2, 0x3f, 0x3f, 0x3f);

			public static final String red_7f     = esc(48, 2, 0xff, 0x7f, 0x7f);
			public static final String green_7f   = esc(48, 2, 0x7f, 0xff, 0x7f);
			public static final String yellow_7f  = esc(48, 2, 0xff, 0xff, 0x7f);
			public static final String blue_7f    = esc(48, 2, 0x7f, 0x7f, 0xff);
			public static final String magenta_7f = esc(48, 2, 0xff, 0x7f, 0xff);
			public static final String cyan_7f    = esc(48, 2, 0x7f, 0xff, 0xff);
			public static final String gray_7f    = esc(48, 2, 0x7f, 0x7f, 0x7f);

			public static final String red_bf     = esc(48, 2, 0xff, 0xbf, 0xbf);
			public static final String green_bf   = esc(48, 2, 0xbf, 0xff, 0xbf);
			public static final String yellow_bf  = esc(48, 2, 0xff, 0xff, 0xbf);
			public static final String blue_bf    = esc(48, 2, 0xbf, 0xbf, 0xff);
			public static final String magenta_bf = esc(48, 2, 0xff, 0xbf, 0xff);
			public static final String cyan_bf    = esc(48, 2, 0xbf, 0xff, 0xff);
			public static final String gray_bf    = esc(48, 2, 0xbf, 0xbf, 0xbf);

		}

		private static final String esc(final int... esc) {

			StringBuilder s = new StringBuilder();
			if (support) {
				s.append(prefix);
				for (int i = 0; i < esc.length; i++) {
					s.append(0 == i ? "" : ";");
					s.append(esc[i]);
				}
				s.append(suffix);
			}

			return s.toString();

		}

	}

	public static final class math {

		private static final Random rand = new Random();

		public static final double rand() {
			return rand.nextDouble();
		}

		public static final int min(final int... vals) {

			int val = Integer.MAX_VALUE;

			for (int i = 0; i < vals.length; i++) {
				val = Math.min(val, vals[i]);
			}

			return val;

		}

		public static final int max(final int... vals) {

			int val = Integer.MIN_VALUE;

			for (int i = 0; i < vals.length; i++) {
				val = Math.max(val, vals[i]);
			}

			return val;

		}

		public static final long min(final long... vals) {

			long val = Long.MAX_VALUE;

			for (int i = 0; i < vals.length; i++) {
				val = Math.min(val, vals[i]);
			}

			return val;

		}

		public static final long max(final long... vals) {

			long val = Long.MIN_VALUE;

			for (int i = 0; i < vals.length; i++) {
				val = Math.max(val, vals[i]);
			}

			return val;

		}

	}

	public static final class hash {

		public static final byte[] sha256(final Path path)
				throws IOException {
			return sha256(path.toFile());
		}

		public static final byte[] sha256(final File file)
				throws IOException {

			byte[] ret = null;

			synchronized (file.toPath().getFileSystem()) {
				if (!file.canRead()) {
					throw new AccessDeniedException(file.toString());
				}
				try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
					ret = digest(in, "SHA-256");
				}
			}

			return ret;

		}

		public static final byte[] sha512(final Path path)
				throws IOException {
			return sha512(path.toFile());
		}

		public static final byte[] sha512(final File file)
				throws IOException {

			byte[] ret = null;

			synchronized (file.toPath().getFileSystem()) {
				if (!file.canRead()) {
					throw new AccessDeniedException(file.toString());
				}
				try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
					ret = digest(in, "SHA-512");
				}
			}

			return ret;

		}

		private static final byte[] digest(final InputStream in, final String algorithm)
				throws IOException {

			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance(algorithm);
			} catch (NoSuchAlgorithmException e) {
				throw new IOException(e);
			}

			byte[] buffer = new byte[1 << 16];
			int size = -1;

			while (-1 < (size = in.read(buffer))) {
				md.update(buffer, 0, size);
			}

			return md.digest();

		}

		public static class HashCollisionException extends Exception {

			private static final long serialVersionUID = 1L;

			private Object o1 = null;
			private Object o2 = null;

			public HashCollisionException(final Object o1, final Object o2) {
				this.o1 = o1;
				this.o2 = o2;
			}

			public Object o1() {
				return o1;
			}

			public Object o2() {
				return o2;
			}

			@Override
			public String toString() {
				return o1 + " " + o2;
			}

		}

	}

	public static final class normalize {

		/**
		 * 数字: 全角→半角
		 */
		@SuppressWarnings("serial")
		public static final Map<String, String> number = Collections.unmodifiableMap(new LinkedHashMap<>() {
			{

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

			}
		});

		/**
		 * 英字: 全角→半角
		 */
		@SuppressWarnings("serial")
		public static final Map<String, String> alpha = Collections.unmodifiableMap(new LinkedHashMap<>() {
			{

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

			}
		});

		/**
		 * カナ: 半角→全角
		 */
		@SuppressWarnings("serial")
		public static final Map<String, String> kana = Collections.unmodifiableMap(new LinkedHashMap<>() {
			{

				put("ﾞ", "゛"); put("ﾟ", "゜");
				put("､", "、"); put("｡", "。");
				put("｢", "「"); put("｣", "」");

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

				// 合成
//				put("ｳﾞ", "ヴ");
//				put("ｶﾞ", "ガ"); put("ｷﾞ", "ギ"); put("ｸﾞ", "グ"); put("ｹﾞ", "ゲ"); put("ｺﾞ", "ゴ");
//				put("ｻﾞ", "ザ"); put("ｼﾞ", "ジ"); put("ｽﾞ", "ズ"); put("ｾﾞ", "ゼ"); put("ｿﾞ", "ゾ");
//				put("ﾀﾞ", "ダ"); put("ﾁﾞ", "ヂ"); put("ﾂﾞ", "ヅ"); put("ﾃﾞ", "デ"); put("ﾄﾞ", "ド");
//				put("ﾊﾞ", "バ"); put("ﾋﾞ", "ビ"); put("ﾌﾞ", "ブ"); put("ﾍﾞ", "ベ"); put("ﾎﾞ", "ボ");
//				put("ﾊﾟ", "パ"); put("ﾋﾟ", "ピ"); put("ﾌﾟ", "プ"); put("ﾍﾟ", "ペ"); put("ﾎﾟ", "ポ");
				put("う゛", "ゔ");
				put("か゛", "が"); put("き゛", "ぎ"); put("く゛", "ぐ"); put("け゛", "げ"); put("こ゛", "ご");
				put("さ゛", "ざ"); put("し゛", "じ"); put("す゛", "ず"); put("せ゛", "せ"); put("そ゛", "そ");
				put("た゛", "だ"); put("ち゛", "ぢ"); put("つ゛", "づ"); put("て゛", "で"); put("と゛", "ど");
				put("は゛", "ば"); put("ひ゛", "び"); put("ふ゛", "ぶ"); put("へ゛", "べ"); put("ほ゛", "ぼ");
				put("は゜", "ぱ"); put("ひ゜", "ぴ"); put("ふ゜", "ぷ"); put("へ゜", "ぺ"); put("ほ゜", "ぽ");
				put("ゔ",   "ゔ");
				put("が",   "が"); put("ぎ",   "ぎ"); put("ぐ",   "ぐ"); put("げ",   "げ"); put("ご",   "ご");
				put("ざ",   "ざ"); put("じ",   "じ"); put("ず",   "ず"); put("ぜ",   "ぜ"); put("ぞ",   "ぞ");
				put("だ",   "だ"); put("ぢ",   "ぢ"); put("づ",   "づ"); put("で",   "で"); put("ど",   "ど");
				put("ば",   "ば"); put("び",   "び"); put("ぶ",   "ぶ"); put("べ",   "べ"); put("ぼ",   "ぼ");
				put("ぱ",   "ぱ"); put("ぴ",   "ぴ"); put("ぷ",   "ぷ"); put("ぺ",   "ぺ"); put("ぽ",   "ぽ");
				put("ウ゛", "ヴ");
				put("カ゛", "ガ"); put("キ゛", "ギ"); put("ク゛", "グ"); put("ケ゛", "ゲ"); put("コ゛", "ゴ");
				put("サ゛", "ザ"); put("シ゛", "ジ"); put("ス゛", "ズ"); put("セ゛", "ゼ"); put("ソ゛", "ゾ");
				put("タ゛", "ダ"); put("チ゛", "ヂ"); put("ツ゛", "ヅ"); put("テ゛", "デ"); put("ト゛", "ド");
				put("ハ゛", "バ"); put("ヒ゛", "ビ"); put("フ゛", "ブ"); put("ヘ゛", "ベ"); put("ホ゛", "ボ");
				put("ハ゜", "パ"); put("ヒ゜", "ピ"); put("フ゜", "プ"); put("ヘ゜", "ペ"); put("ホ゜", "ポ");
				put("ヴ",   "ヴ");
				put("ガ",   "ガ"); put("ギ",   "ギ"); put("グ",   "グ"); put("ゲ",   "ゲ"); put("ゴ",   "ゴ");
				put("ザ",   "ザ"); put("ジ",   "ジ"); put("ズ",   "ズ"); put("ゼ",   "ゼ"); put("ゾ",   "ゾ");
				put("ダ",   "ダ"); put("ヂ",   "ヂ"); put("ヅ",   "ヅ"); put("デ",   "デ"); put("ド",   "ド");
				put("バ",   "バ"); put("ビ",   "ビ"); put("ブ",   "ブ"); put("ベ",   "ベ"); put("ボ",   "ボ");
				put("パ",   "パ"); put("ピ",   "ピ"); put("プ",   "プ"); put("ペ",   "ペ"); put("ポ",   "ポ");

			}
		});

		@SuppressWarnings("serial")
		public static final Map<String, String> all = Collections.unmodifiableMap(new LinkedHashMap<>() {
			{

				putAll(normalize.number);
				putAll(normalize.alpha);
				putAll(normalize.kana);

				// 記号: 半角
				put("＋", "+"); put("－", "-"); put("＊", "*"); put("／", "/"); put("＝", "=");
				put("％", "%"); put("＄", "$"); put("￥", "\\");
				put("，", ","); put("．", "."); put("；", ";"); put("：", ":");
				put("＆", "&"); put("｜", "|"); put("！", "!"); put("？", "?");
				put("＃", "#"); put("＠", "@"); put("＿", "_"); put("｀", "`");
				put("’", "'"); put("”", "\"");
				put("（", "("); put("）", ")");
				put("＜", "<"); put("＞", ">");
				put("〈", "<"); put("〉", ">");
				put("《", "<"); put("》", ">");
				put("［", "["); put("］", "]");
				put("【", "["); put("】", "]");
				put("〔", "["); put("〕", "]");
				put("〖", "["); put("〗", "]");
				put("｛", "{"); put("｝", "}");

				// 記号: 全角
//				put("～", "〜"); put("ｰ", "ー");
				put("『", "「"); put("』", "」");

				// 単位記号 U+3300~3357
				put("㌀", "アパート");   put("㌁", "アルファ"); put("㌂", "アンペア");   put("㌃", "アール");   put("㌄", "イニング");   put("㌅", "インチ");     put("㌆", "ウォン");       put("㌇", "エスクード"); put("㌈", "エーカー"); put("㌉", "オンス");     put("㌊", "オーム");       put("㌋", "カイリ");     put("㌌", "カラット"); put("㌍", "カロリー"); put("㌎", "ガロン");     put("㌏", "ガンマ");
				put("㌐", "ギガ");       put("㌑", "ギニー");   put("㌒", "キュリー");   put("㌓", "ギルダー"); put("㌔", "キロ");       put("㌕", "キログラム"); put("㌖", "キロメートル"); put("㌗", "キロワット"); put("㌘", "グラム");   put("㌙", "グラムトン"); put("㌚", "クルセイロ");   put("㌛", "クローネ");   put("㌜", "ケース");   put("㌝", "コルチ");   put("㌞", "コーポ");     put("㌟", "サイクル");
				put("㌠", "サンチーム"); put("㌡", "シリング"); put("㌢", "センチ");     put("㌣", "セント");   put("㌤", "ダース");     put("㌥", "デシ");       put("㌦", "ドル");         put("㌧", "トン");       put("㌨", "ナノ");     put("㌩", "ノット");     put("㌪", "ハイツ");       put("㌫", "パーセント"); put("㌬", "パーツ");   put("㌭", "バーレル"); put("㌮", "ピアストル"); put("㌯", "ピクル");
				put("㌰", "ピコ");       put("㌱", "ビル");     put("㌲", "ファラッド"); put("㌳", "フィート"); put("㌴", "ブッシェル"); put("㌵", "フラン");     put("㌶", "ヘクタール");   put("㌷", "ペソ");       put("㌸", "ペニヒ");   put("㌹", "ヘルツ");     put("㌺", "ペンス");       put("㌻", "ページ");     put("㌼", "ベータ");   put("㌽", "ポイント"); put("㌾", "ボルト");     put("㌿", "ホン");
				put("㍀", "ポンド");     put("㍁", "ホール");   put("㍂", "ホーン");     put("㍃", "マイクロ"); put("㍄", "マイル");     put("㍅", "マッハ");     put("㍆", "マルク");       put("㍇", "マンション"); put("㍈", "ミクロン"); put("㍉", "ミリ");       put("㍊", "ミリメートル"); put("㍋", "メガ");       put("㍌", "メガトン"); put("㍍", "メートル"); put("㍎", "ヤード");     put("㍏", "ヤール");
				put("㍐", "ユアン");     put("㍑", "リットル"); put("㍒", "リラ");       put("㍓", "ルピー");   put("㍔", "ルーブル");   put("㍕", "レム");       put("㍖", "レントゲン");   put("㍗", "ワット");

			}
		});

	}

	public static final class regex {

		public static final String spaces = "[\\r\\n\\t\\s　 ]+";

	}

	public static final class concurrent {

		public static final long id() {
			return Thread.currentThread().threadId();
		}

		/**
		 * 現在のスレッドがメインスレッドかどうかを返します。
		 *
		 * @return true: メインスレッドの場合、false:それ以外の場合
		 */
		public static final boolean main() {
			return 1 == id();
		}

		/**
		 * このメソッドを呼び出したメソッドの名前を返します。
		 *
		 * @return このメソッドを呼び出したメソッドの名前
		 */
		public static final String methodName() {
			return Thread.currentThread().getStackTrace()[2].getMethodName();
		}

	}

	public static final class reflect {

		@SuppressWarnings("unchecked")
		public static final <T> Class<T> load(final Object obj)
				throws ClassNotFoundException {
			return switch (obj) {
				case Class<?>     e -> (Class<T>)e;
				case CharSequence e -> (Class<T>)Class.forName(e.toString());
				default             -> (Class<T>)obj.getClass();
			};
		}

//		@Deprecated
//		@SuppressWarnings("unchecked")
//		public static final <T> T invoke(final Object obj, final String name, final Object... args)
//				throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//
//			Method method = null;
//			{
//
//				Class<?>   clazz = load(obj);
//				Class<?>[] types = null == args ? new Class[0] : Stream.of(args).map(Object::getClass).toList().toArray(new Class[0]);
//UnixPath a = null;
//				try {
//					method = clazz.getDeclaredMethod(name, types);
//				} catch (NoSuchMethodException e) {
//					method = clazz.getMethod(name, types);
//				}
//				method.setAccessible(true);
//
//			}
//
//			Object instance = switch (obj) {
//				case Class<?>     e -> null;
//				case CharSequence e -> null;
//				default             -> obj;
//			};
//
//			return (T)method.invoke(instance, args);
//
//		}

		public static final byte[] classpath(final Object obj, final String name)
				throws ClassNotFoundException, IOException {

			Class<?> clazz = load(obj);

			// TODO: 内部クラス対応
			String classpath = clazz.getPackageName().replace('.', '/').replace('$', '/') + "/" + name;

			byte[] bytes = null;
			try (InputStream in = new BufferedInputStream(ClassLoader.getSystemResourceAsStream(classpath))) {

				boolean exists = false;
				try {
					exists = -1 < in.available();
				} catch (IOException e) {
				}
				if (!exists) {
					throw new NoSuchFileException("classpath: " + classpath);
				}

				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					in.transferTo(out);
					bytes = out.toByteArray();
				}

			}

			return bytes;

		}

	}

	public static final class seri {

		@SuppressWarnings("unchecked")
		public static final <T extends Serializable> T read(final Path file)
				throws IOException, ClassNotFoundException {

			T obj = null;

			if (Files.exists(file) && Files.isRegularFile(file)) {
				try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.toFile()))) {
					obj = (T)in.readObject();
				}
			}

			return obj;

		}

		public static final void write(final Path file, final Serializable obj)
				throws IOException {

			Files.createDirectories(file.getParent());

			try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
				out.writeObject(obj);
			}

		}

	}

	public static final class format {

		/**
		 * 2進数の文字列表現を返します。
		 */
		public static final String bin(final byte v) {

			StringBuilder s = new StringBuilder();
			for (int i = 0; i < 8; i++) {
				s.append(0 < (v & (1 << (7 - i))) ? '1' : '0');
			}

			return s.toString();

		}

		/**
		 * 2進数の文字列表現を返します。
		 */
		public static final String bin(final byte[] v) {

			StringBuilder s = new StringBuilder();
			for (byte b : v) {
				s.append(bin(b));
			}

			return s.toString();

		}

		/**
		 * 2進数の文字列表現を返します。
		 */
		public static final String bin(final short v) {

			StringBuilder s = new StringBuilder();
			s.append(bin((byte)(0xff & (v >> 8))));
			s.append(bin((byte)(0xff & (v >> 0))));

			return s.toString();

		}

		/**
		 * 2進数の文字列表現を返します。
		 */
		public static final String bin(final int v) {

			StringBuilder s = new StringBuilder();
			s.append(bin((short)(0xffff & (v >> 16))));
			s.append(bin((short)(0xffff & (v >>  0))));

			return s.toString();

		}

		/**
		 * 2進数の文字列表現を返します。
		 */
		public static final String bin(final long v) {

			StringBuilder s = new StringBuilder();
			s.append(bin((int)(0xffffffff & (v >> 32))));
			s.append(bin((int)(0xffffffff & (v >>  0))));

			return s.toString();

		}

		/**
		 * 16進数の文字列表現を返します。
		 */
		public static final String hex(final byte v) {
			return String.format("%02x", v);
		}

		/**
		 * 16進数の文字列表現を返します。
		 */
		public static final String hex(final byte[] v) {

			String ret = null;

			if (null != v) {

				StringBuilder s = new StringBuilder();
				for (byte b : v) {
					s.append(hex(b));
				}

				ret = s.toString();

			}

			return ret;

		}

		/**
		 * 16進数の文字列表現を返します。
		 */
		public static final String hex(final short v) {

			StringBuilder s = new StringBuilder();
			s.append(hex((byte)(0xff & (v >> 8))));
			s.append(hex((byte)(0xff & (v >> 0))));

			return s.toString();

		}

		/**
		 * 16進数の文字列表現を返します。
		 */
		public static final String hex(final int v) {

			StringBuilder s = new StringBuilder();
			s.append(hex((short)(0xffff & (v >> 16))));
			s.append(hex((short)(0xffff & (v >>  0))));

			return s.toString();

		}

		/**
		 * 16進数の文字列表現を返します。
		 */
		public static final String hex(final long v) {

			StringBuilder s = new StringBuilder();
			s.append(hex((int)(0xffffffff & (v >> 32))));
			s.append(hex((int)(0xffffffff & (v >>  0))));

			return s.toString();

		}

	}

	public static final class codec {

		public static final String base64(final byte[] val) {
			return Base64.getEncoder().encodeToString(val);
		}

		public static final byte[] base64(final String val) {
			return Base64.getDecoder().decode(val);
		}

		public static final <T> T bencode(final Path file)
				throws IOException {
			return bencode(Files.readAllBytes(file));
		}

		public static final <T> T bencode(final byte[] val)
				throws IOException {
			return bencode(new ByteArrayInputStream(val));
		}

		@SuppressWarnings("unchecked")
		private static final <T> T bencode(final ByteArrayInputStream in)
				throws IOException {

			T ret = null;

			int token = in.read();

			if (token == 'i') {

				StringBuilder sb = new StringBuilder();

				int b = -1;
				while ((b = in.read()) != 'e') {

					if (-1 == b) {
						throw new IllegalArgumentException();
					}

					sb.append((char)b);

				}

				ret = (T)new BigDecimal(sb.toString());

			} else if (Character.isDigit(token)) {

				StringBuilder sb = new StringBuilder();
				sb.append((char)token);

				int b = -1;
				while ((b = in.read()) != ':') {

					if (-1 == b) {
						throw new IllegalArgumentException();
					}

					sb.append((char)b);

				}

				int len = Integer.parseInt(sb.toString());

				byte[] buf = new byte[len];
				in.read(buf, 0, len);

				String utf8 = new String(buf, _0.utf8);

				if (0 == compare(buf, utf8.getBytes())) {
					ret = (T)utf8;

				} else {
					ret = (T)format.hex(buf);
				}

			} else if (token == 'l') {

				List<Object> list = new ArrayList<>();

				while (true) {

					in.mark(1);
					if (in.read() == 'e') {
						break;
					}
					in.reset();

					Object rec = bencode(in);

					list.add(rec);

				}

				ret = (T)list;

			} else if (token == 'd') {

				Map<String, Object> map = new HashMap<>();

				while (true) {

					in.mark(1);
					if (in.read() == 'e') {
						break;
					}
					in.reset();

					String key = bencode(in);
					Object val = bencode(in);

					map.put(key, val);

				}

				ret = (T)map;

			} else {
				throw new IOException(Character.toString((char)token));
			}

			return ret;

		}

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
				e.printStackTrace();
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
				Connection con = (Connection)closeable;
				try {
					if (!con.getAutoCommit()) {
						con.rollback();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			try {
				closeable.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public static final String trim(final CharSequence val) {
		return val.toString().replaceAll("\\u00a0", " ").replaceAll("^" + regex.spaces + "|" + regex.spaces + "$", "");
	}

	public static final boolean empty(final Object obj) {
		return switch (obj) {
			case null            -> true;
			case Boolean       e -> !e.booleanValue();
			case Number        e -> 0.0d == e.doubleValue();
			case CharSequence  e -> "".equals(e.toString());
			case Iterator<?>   e -> e.hasNext();
			case Collection<?> e -> e.isEmpty();
			case Map<?, ?>     e -> e.isEmpty();
			default              -> throw new UnsupportedOperationException(obj.getClass().getName());
		};
	}

	@SuppressWarnings("unchecked")
	public static final int compare(final Object v1, final Object v2) {

		int compare = 0;

		if (null == v1 && null == v2) {
			compare = 0;

		} else if (null != v1 && null == v2) {
			compare = 1;

		} else if (null == v1 && null != v2) {
			compare = -1;

		} else if (v1 instanceof byte[] b1 && v2 instanceof byte[] b2) {

			compare = compare(b1.length, b2.length);

			for (int i = 0; i < b1.length && 0 == compare; i++) {
				compare = compare(b1[i], b2[i]);
			}

		} else if (v1 instanceof Number n1 && v2 instanceof Number n2) {

			Long l1 = n1.longValue();
			Long l2 = n2.longValue();
			compare = l1.compareTo(l2);

			if (0 == compare) {
				Double d1 = n1.doubleValue();
				Double d2 = n2.doubleValue();
				compare = d1.compareTo(d2);
			}

		} else if (v1 instanceof Comparable c1 && v2 instanceof Comparable c2) {
			compare = c1.compareTo(c2);

		} else if (v1 instanceof Map m1 && v2 instanceof Map m2) {

			Set<Object> keys = new HashSet<>();
			keys.addAll(m1.keySet());
			keys.addAll(m2.keySet());

			for (Object key : keys) {

				Object o1 = m1.get(key);
				Object o2 = m2.get(key);
				compare = compare(o1, o2);

				if (0 != compare) {
					break;
				}

			}

		} else {
			throw new UnsupportedOperationException();
		}

		return compare;

	}

	@SuppressWarnings("unchecked")
	public static final void set(final Map<String, Object> map, final String selector, final Object val) {

		Map<String, Object> obj = map;

		String[] tree = selector.split(Pattern.quote("/"));
		for (int i = 0; i < tree.length; i++) {

			String item = tree[i];

			boolean last = i == tree.length - 1;
			if (last) {
				obj.put(item, val);
				break;
			}

			if (!obj.containsKey(item)) {
				obj.put(item, new HashMap<>());
			}

			obj = (Map<String, Object>)obj.get(item);

		}

	}

	@SuppressWarnings("unchecked")
	public static final <T> T get(final Map<String, Object> map, final String selector) {

		Object obj = map;

		String[] tree = selector.split(Pattern.quote("/"));
		for (int i = 0; i < tree.length; i++) {

			String item = tree[i];

			if (obj instanceof Map m) {
				obj = m.get(item);
			}

			if (null == obj) {
				break;
			}

		}

		return (T)obj;

	}

	@SuppressWarnings("unchecked")
	public static final Map<String, Object> merge(final Map<String, Object> origin, final Map<String, Object> merge) {
		return (Map<String, Object>)merge((Object)origin, (Object)merge);
	}

	@SuppressWarnings("unchecked")
	private static final Object merge(final Object origin, final Object merge) {

		Object ret = null;

		if (null == origin) {
			ret = merge;

		} else if (null == merge) {
			ret = null;

		} else if (origin instanceof Map m1 && merge instanceof Map m2) {

			Set<Object> keys = new HashSet<>();
			keys.addAll(m1.keySet());
			keys.addAll(m2.keySet());

			Map<String, Object> map = new HashMap<>();
			for (Object key : keys) {

				boolean c1 = m1.containsKey(key);
				boolean c2 = m2.containsKey(key);
				Object  v1 = m1.get(key);
				Object  v2 = m2.get(key);
				Object  v  = null;

				if (c1 && c2) {
					v = merge(v1, v2);
				} else if (c1 && !c2) {
					v = v1;
				} else if (!c1 && c2) {
					v = v2;
				}

				map.put((String)key, v);

			}

			ret = map;

		} else if (origin instanceof Collection c1 && merge instanceof Collection c2) {

			Set<Object> set = new HashSet<>();
			set.addAll(c1);
			set.addAll(c2);

			ret = set;

		} else {
			ret = merge;
		}

		return ret;

	}

	@SuppressWarnings("unchecked")
	public static final <T> T nvl(final Object... objs) {

		Object ret = null;

		for (Object obj : objs) {

			if (null == obj) {
				continue;
			}

			ret = obj;
			break;

		}

		return (T)ret;

	}

	public static final Map<String, Long> count(final String str) {

		Map<String, Long> wc = new HashMap<>();

		for (int i = 0; i < str.length(); i++) {

			char   c = str.charAt(i);
			String s = null;

			if (Character.isHighSurrogate(c)) {

				char[] chars = new char[2];
				chars[0] = c;
				chars[1] = str.charAt(i + 1);

				int cp = Character.codePointAt(chars, 0);

				s = Character.toString(cp);

			} else if (Character.isLowSurrogate(c)) {
				continue;

			} else {
				s = Character.toString(c);
			}

			long cnt = nvl(wc.get(s), 0L);
			wc.put(s, cnt + 1);

		}

		return wc;

	}

	public static final Map<String, Long> count(final String str, final Collection<String> words) {

		Map<String, Long> wc = new HashMap<>();

		for (String word : words) {

			long cnt = nvl(wc.get(word), 0L);

			int offset = 0;
			int index  = -1;
			while (-1 < (index = str.indexOf(word, offset))) {
				cnt++;
				offset = index + 1;
			}
			if (cnt < 1) {
				continue;
			}

			wc.put(word, cnt);

		}

		return wc;

	}

	public static final String reverse(final String delim, final String org) {

		List<String> items = new ArrayList<>();
		items.addAll(List.of(org.split(Pattern.quote(delim))));

		Collections.reverse(items);

		return String.join(delim, items);

	}

//	public static final String normalize(final String val) {
//
//		String ret = val;
//
//		if (null != ret) {
//
//			for (Entry<String, String> entry : normalize.entrySet()) {
//
//				String k = entry.getKey();
//				String v = entry.getValue();
//
//				if (-1 == ret.indexOf(k)) {
//					continue;
//				}
//
//				ret = ret.replace(k, v);
//
//			}
//
//			ret = ret.replaceAll(Regex.spaces, " ");
//			ret = trim(ret);
//
//		}
//
//		return ret;
//
//	}

	@SuppressWarnings("serial")
	public static final Map<String, String> brackets = Collections.unmodifiableMap(new HashMap<>() {

		{

			// 0括弧
			put("(", ")");
			put("（", "）");

			// 1括弧
			put("<", ">");
			put("＜", "＞");
			put("≪", "≫");
			put("〈", "〉");
			put("《", "》");
			put("｢", "｣");
			put("「", "」");

			// 2括弧
			put("[", "]");
			put("［", "］");
			put("〔", "〕");
			put("【", "】");
			put("〖", "〗");

			// 3括弧
			put("{", "}");
			put("｛", "｝");

		}

	});

	public static final Collection<String> brackets(final CharSequence val) {
		return brackets(val, false);
	}

	public static final Collection<String> brackets(final CharSequence val, final boolean remain) {

		Set<String> items = new HashSet<>();

		StringBuilder buf = new StringBuilder(val);
		List<Map<String, Object>> stack = new ArrayList<>();

		for (int i = 0; i < buf.length(); i++) {

			String s = String.valueOf(buf.charAt(i));

			String close = brackets.get(s);
			if (null != close) {

				Map<String, Object> map = new HashMap<>();
				map.put("open",  s);
				map.put("close", close);
				map.put("start", Integer.valueOf(i));

				stack.addLast(map);

			}

			for (int j = stack.size(); -1 < i && 0 < j; j--) {

				Map<String, Object> map = stack.get(j - 1);
				if (!s.equals(map.get("close"))) {
					continue;
				}

				int start = ((Integer)map.get("start")).intValue();
				int end   = i;

				String substr = trim(buf.substring(start + 1, end));
				items.add(substr);

				buf.delete(start, end + 1);
				buf.insert(start, " ");
				stack.clear();
				i = -1;

			}

		}

		if (remain) {
			items.add(trim(buf));
		}

		return items;

	}

	public static final String scheme(final String uri) {
		return scheme(uri, false);
	}

	public static final String scheme(final String uri, final boolean trim) {

		String scheme = null;

		int idx = uri.indexOf(":");
		if (-1 < idx) {

			scheme = uri.substring(0, idx).toLowerCase();
			if (trim) {
				scheme = trim(scheme);
			}
			if (!scheme.matches("[a-z][a-z0-9\\+\\-\\.]*")) {
				scheme = null;
			}

		}

		return scheme;

	}

	public static final Throwable cause(final Throwable e) {
		return cause(e, false);
	}

	public static final Throwable cause(final Throwable e, final boolean auto) {

		Throwable curr = e;
		while (true) {

			if (auto) {
				if (curr instanceof Error t) {
					throw t;
				}
				if (curr instanceof RuntimeException t) {
					throw t;
				}
			}

			Throwable cause = curr.getCause();
			if (null == cause) {
				break;
			}

			curr = cause;

		}

		return curr;

	}

}
