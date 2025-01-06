import java.lang.reflect.Method;

public final class Entrypoint {

	private Entrypoint() {
	}

	public static void main(final String... args)
			throws Throwable {

		String arg1 = args[0];
		Class<?> clazz = Class.forName("_0.playground.cli." + arg1 + ".Main");

		String[] new_args = new String[args.length - 1];
		System.arraycopy(args, 1, new_args, 0, new_args.length);

		Method method = clazz.getMethod("main", args.getClass());
		method.invoke(null, (Object)new_args);

	}

}
