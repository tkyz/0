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
import java.nio.charset.Charset;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public final class _0 {

	public static final String cr   = "\r";
	public static final String lf   = "\n";
	public static final String crlf = cr + lf;

	public static final Charset utf8 = Charset.forName("utf8");

	public static final String username = System.getProperty("user.name");
	public static final Path   userhome = Path.of(System.getProperty("user.home"));

	@Deprecated
	public static final byte[] openpgp4fpr = null; // TODO: openpgp4fpr

	@SuppressWarnings("serial")
	public static final Map<String, String> normalize = Collections.unmodifiableMap(new HashMap<>() {

		{

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
			put("｛", "{"); put("｝", "}");

			// 記号: 全角
			put("ﾞ", "゛"); put("ﾟ", "゜");
			put("､", "、"); put("｡", "。");
//			put("～", "〜"); put("ｰ", "ー");
			put("｢", "「"); put("｣", "」");
			put("『", "「"); put("』", "」");

			// 単位記号 U+3300~3357
			put("㌀", "アパート");   put("㌁", "アルファ"); put("㌂", "アンペア");   put("㌃", "アール");   put("㌄", "イニング");   put("㌅", "インチ");     put("㌆", "ウォン");       put("㌇", "エスクード"); put("㌈", "エーカー"); put("㌉", "オンス");     put("㌊", "オーム");       put("㌋", "カイリ");     put("㌌", "カラット"); put("㌍", "カロリー"); put("㌎", "ガロン");     put("㌏", "ガンマ");
			put("㌐", "ギガ");       put("㌑", "ギニー");   put("㌒", "キュリー");   put("㌓", "ギルダー"); put("㌔", "キロ");       put("㌕", "キログラム"); put("㌖", "キロメートル"); put("㌗", "キロワット"); put("㌘", "グラム");   put("㌙", "グラムトン"); put("㌚", "クルセイロ");   put("㌛", "クローネ");   put("㌜", "ケース");   put("㌝", "コルチ");   put("㌞", "コーポ");     put("㌟", "サイクル");
			put("㌠", "サンチーム"); put("㌡", "シリング"); put("㌢", "センチ");     put("㌣", "セント");   put("㌤", "ダース");     put("㌥", "デシ");       put("㌦", "ドル");         put("㌧", "トン");       put("㌨", "ナノ");     put("㌩", "ノット");     put("㌪", "ハイツ");       put("㌫", "パーセント"); put("㌬", "パーツ");   put("㌭", "バーレル"); put("㌮", "ピアストル"); put("㌯", "ピクル");
			put("㌰", "ピコ");       put("㌱", "ビル");     put("㌲", "ファラッド"); put("㌳", "フィート"); put("㌴", "ブッシェル"); put("㌵", "フラン");     put("㌶", "ヘクタール");   put("㌷", "ペソ");       put("㌸", "ペニヒ");   put("㌹", "ヘルツ");     put("㌺", "ペンス");       put("㌻", "ページ");     put("㌼", "ベータ");   put("㌽", "ポイント"); put("㌾", "ボルト");     put("㌿", "ホン");
			put("㍀", "ポンド");     put("㍁", "ホール");   put("㍂", "ホーン");     put("㍃", "マイクロ"); put("㍄", "マイル");     put("㍅", "マッハ");     put("㍆", "マルク");       put("㍇", "マンション"); put("㍈", "ミクロン"); put("㍉", "ミリ");       put("㍊", "ミリメートル"); put("㍋", "メガ");       put("㍌", "メガトン"); put("㍍", "メートル"); put("㍎", "ヤード");     put("㍏", "ヤール");
			put("㍐", "ユアン");     put("㍑", "リットル"); put("㍒", "リラ");       put("㍓", "ルピー");   put("㍔", "ルーブル");   put("㍕", "レム");       put("㍖", "レントゲン");   put("㍗", "ワット");

			// 英字: 半角
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

			// 数字: 半角
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

			// カナ: 全角
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
			put("ｳﾞ", "ヴ");
			put("ｶﾞ", "ガ"); put("ｷﾞ", "ギ"); put("ｸﾞ", "グ"); put("ｹﾞ", "ゲ"); put("ｺﾞ", "ゴ");
			put("ｻﾞ", "ザ"); put("ｼﾞ", "ジ"); put("ｽﾞ", "ズ"); put("ｾﾞ", "ゼ"); put("ｿﾞ", "ゾ");
			put("ﾀﾞ", "ダ"); put("ﾁﾞ", "ヂ"); put("ﾂﾞ", "ヅ"); put("ﾃﾞ", "デ"); put("ﾄﾞ", "ド");
			put("ﾊﾞ", "バ"); put("ﾋﾞ", "ビ"); put("ﾌﾞ", "ブ"); put("ﾍﾞ", "ベ"); put("ﾎﾞ", "ボ");
			put("ﾊﾟ", "パ"); put("ﾋﾟ", "ピ"); put("ﾌﾟ", "プ"); put("ﾍﾟ", "ペ"); put("ﾎﾟ", "ポ");

			// ２文字→１文字
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

			// 3括弧
			put("{", "}");
			put("｛", "｝");

		}

	});

	public static final void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	public static final long ino(final Path file)
			throws IOException {
		return Files.exists(file) ? ino(Files.getFileAttributeView(file, BasicFileAttributeView.class).readAttributes()) : -1;
	}

	public static final long ino(final BasicFileAttributes attrs) {

		String key = attrs.fileKey().toString();

		return Long.parseLong(key.substring(key.indexOf("ino=") + 4, key.indexOf(")")));

	}

	/**
	 * 現在のスレッドがメインスレッドかどうかを返します。
	 *
	 * @return true: メインスレッドの場合、false:それ以外の場合
	 */
	public static final boolean main() {
		return 1 == Thread.currentThread().threadId();
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

	public static String trim(final CharSequence val) {
		return val.toString().replaceAll("^" + Regex.spaces + "|" + Regex.spaces + "$", "");
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

		} else if (obj instanceof Number) {
			empty = 0.0d == ((Number)obj).doubleValue();

		} else if (obj instanceof CharSequence) {
			empty = "".equals(obj.toString());

		} else {
			throw new UnsupportedOperationException(obj.getClass().getName());
		}

		return empty;

	}

	public static <T extends Comparable<T>> int compare(final Object v1, final Object v2) {

		int compare = 0;

		if (null == v1 && null == v2) {
			compare = 0;

		} else if (null != v1 && null == v2) {
			compare = 1;

		} else if (null == v1 && null != v2) {
			compare = -1;

		} else if (v1 instanceof Number && v2 instanceof Number) {

			Number n1 = (Number)v1;
			Number n2 = (Number)v2;

			// TODO: 厳密な比較
			Long l1 = n1.longValue();
			Long l2 = n2.longValue();
			compare = l1.compareTo(l2);

			if (0 == compare) {
				Double d1 = n1.doubleValue();
				Double d2 = n2.doubleValue();
				compare = d1.compareTo(d2);
			}

		} else if (v1 instanceof Comparable && v2 instanceof Comparable) {

			@SuppressWarnings("unchecked") T t1 = (T)v1;
			@SuppressWarnings("unchecked") T t2 = (T)v2;

			compare = t1.compareTo(t2);

		} else if (v1 instanceof Map && v2 instanceof Map) {

			Map<?, ?> m1 = (Map<?, ?>)v1;
			Map<?, ?> m2 = (Map<?, ?>)v2;

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
	public static void set(final Map<String, Object> map, final String selector, final Object val) {

		Map<String, Object> obj = map;

		String[] tree = selector.split("[/\\.]");
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
	public static <T> T get(final Map<String, Object> map, final String selector) {

		Object obj = map;

		String[] tree = selector.split("[/\\.]");
		for (int i = 0; i < tree.length; i++) {

			String item = tree[i];

			if (obj instanceof Map) {
				obj = ((Map<?, ?>)obj).get(item);
			}

			if (null == obj) {
				break;
			}

		}

		return (T)obj;

	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> merge(final Map<String, Object> origin, final Map<String, Object> merge) {
		return (Map<String, Object>)merge((Object)origin, (Object)merge);
	}

	private static Object merge(final Object origin, final Object merge) {

		Object ret = null;

		if (null == origin) {
			ret = merge;

		} else if (null == merge) {
			ret = origin;

		} else if (origin instanceof Map && merge instanceof Map) {

			Map<?, ?> map1 = (Map<?, ?>)origin;
			Map<?, ?> map2 = (Map<?, ?>)merge;

			Set<Object> keys = new HashSet<>();
			keys.addAll(map1.keySet());
			keys.addAll(map2.keySet());

			Map<String, Object> map = new HashMap<>();
			for (Object key : keys) {

				Object v1  = map1.get(key);
				Object v2  = map2.get(key);
				Object val = merge(v1, v2);

				map.put((String)key, val);

			}
			ret = map;

		} else if (origin instanceof Collection && merge instanceof Collection) {

			Collection<?> list1 = (Collection<?>)origin;
			Collection<?> list2 = (Collection<?>)merge;

			Set<Object> set = new HashSet<>();
			set.addAll(list1);
			set.addAll(list2);

			ret = set;

		} else if (origin.getClass() == merge.getClass()) {
			ret = merge;

		} else {
			throw new UnsupportedOperationException(origin.getClass().getName() + ":" + merge.getClass().getName());
		}

		return ret;

	}

	public static final <T> T nvl(final Object... objs) {
		return nvl(false, objs);
	}

	@SuppressWarnings("unchecked")
	public static final <T> T nvl(final boolean empty, final Object... objs) {

		Object ret = null;

		for (Object obj : objs) {

			if (null == obj) {
				continue;
			}
			if (empty && empty(obj)) {
				continue;
			}

			ret = obj;
			break;

		}

		return (T)ret;

	}

	public static Map<String, Long> count(final String str) {

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

	public static Map<String, Long> count(final String str, final Collection<String> words) {

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

	public static final String normalize(final String val) {

		String ret = val;

		if (null != ret) {

			for (Entry<String, String> entry : normalize.entrySet()) {

				String k = entry.getKey();
				String v = entry.getValue();

				if (-1 == ret.indexOf(k)) {
					continue;
				}

				ret = ret.replace(k, v);

			}

			ret = ret.replaceAll(Regex.spaces, " ");
			ret = trim(ret);

		}

		return ret;

	}

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

	public static final byte[] sha256(final Path path)
			throws IOException {
		return sha256(path.toFile());
	}

	public static final byte[] sha256(final File file)
			throws IOException {

		byte[] ret = null;

		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			ret = sha256(in);
		}

		return ret;

	}

	public static final byte[] sha256(final String str)
			throws IOException {

		byte[] ret = null;

		try (InputStream in = new ByteArrayInputStream(str.getBytes())) {
			ret = sha256(in);
		}

		return ret;

	}

	public static final byte[] sha256(final InputStream in)
			throws IOException {

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
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

	@SuppressWarnings("unchecked")
	public static final <T> T read(final Path file)
			throws IOException, ClassNotFoundException {

		T obj = null;

		if (Files.exists(file) && !Files.isDirectory(file)) {
			try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.toFile()))) {
				obj = (T)in.readObject();
			}
		}

		return obj;

	}

	public static final void write(final Path file, final Object obj)
			throws IOException {

		Files.createDirectories(file.getParent());

		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
			out.writeObject(obj);
		}

	}

	public static byte[] classpath(final Object clazz, final String name)
			throws IOException {

		Class<?> clazz_ = clazz instanceof Class ? (Class<?>)clazz : clazz.getClass();

		// TODO: 内部クラス('$' -> '/')対応
		String classpath = clazz_.getPackageName().replace('.', '/').replace('$', '/') + "/" + name;

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

	@Deprecated
	public static void yield() {
		try {
			Thread.yield();
			Thread.sleep(1);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static String ext(final Path path) {

		String ext = null;

		String name = path.getFileName().toString();

		int idx = name.lastIndexOf(".");
		if (-1 < idx) {
			ext = name.substring(idx + 1).toLowerCase();
		}

		return ext;

	}

}
