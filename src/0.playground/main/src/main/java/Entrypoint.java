import java.lang.reflect.Method;

import _0.playground.core._0;

public final class Entrypoint {

	private Entrypoint() {
	}

	public static void main(final String... args)
			throws Throwable {

		String   arg1     = args[0];
		String[] new_args = new String[args.length - 1];
		System.arraycopy(args, 1, new_args, 0, new_args.length);

		Class<?> clazz = null;
		if (null != _0.user.fpr) {
			try {
				clazz = Class.forName("_0.playground.cli." + arg1 + "_" + _0.user.fpr + ".Main");
			} catch (ClassNotFoundException e) {
			}
		}
		if (null == clazz) {
			clazz = Class.forName("_0.playground.cli." + arg1 + ".Main");
		}

		Method method = clazz.getMethod("main", args.getClass());
		method.invoke(null, (Object)new_args);

	}

}
