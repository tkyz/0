package _0.playground.xfunc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.Function;

import _0.Plugin;

public abstract class XFuncPlugin extends Function implements Plugin {

	private static final Logger log = LoggerFactory.getLogger(XFuncPlugin.class);

	public String name() {
		return getClass().getSimpleName().toLowerCase();
	}

	protected final boolean value_boolean(int arg)
			throws SQLException {
		return 0 != value_int(arg);
	}

	protected final void result(boolean value)
			throws SQLException {
		result(value ? 1 : 0);
	}

	@Override
	protected final void xFunc()
			throws SQLException {

		try {

			impl();

		} catch (RuntimeException e) {
			log.warn("", e);
			throw e;

		} catch (SQLException e) {
			log.warn("", e);
			throw e;

		} catch (Exception e) {
			log.warn("", e);
			throw new SQLException(e);
		}

	}

	protected abstract void impl()
			throws Exception;

	public static void load(Connection con)
			throws SQLException {

		// TODO: auto load
		List<Class<? extends XFuncPlugin>> funcs = new LinkedList<>();
		funcs.add(Hash.class);
		funcs.add(Matches.class);
		funcs.add(Normalize.class);
		funcs.add(Nvl.class);
		funcs.add(Replace.class);
		funcs.add(Split.class);
		funcs.add(Trim.class);

		for (Class<? extends XFuncPlugin> func : funcs) {

			XFuncPlugin impl = null;

			try {
				impl = func.getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}

			Function.create(con, impl.name(), impl);

		}

	}

}
