package _0;

public final class Ansi {

	private static final String prefix = "\u001B[";
	private static final String suffix = "m";

	// style
	public static final String reset   = prefix +  0 + suffix;
	public static final String bold    = prefix +  1 + suffix;
	public static final String dim     = prefix +  2 + suffix;
	public static final String italic  = prefix +  3 + suffix;
	public static final String uline   = prefix +  4 + suffix;
	public static final String blink   = prefix +  5 + suffix;
	public static final String reverse = prefix +  7 + suffix;
	public static final String hidden  = prefix +  8 + suffix;

	// foreground
	public static final String black   = prefix + 30 + suffix;
	public static final String gray    = prefix + 90 + suffix;
	public static final String red     = prefix + 91 + suffix;
	public static final String green   = prefix + 92 + suffix;
	public static final String yellow  = prefix + 93 + suffix;
	public static final String blue    = prefix + 94 + suffix;
	public static final String magenta = prefix + 95 + suffix;
	public static final String cyan    = prefix + 96 + suffix;
	public static final String white   = prefix + 97 + suffix;

}
