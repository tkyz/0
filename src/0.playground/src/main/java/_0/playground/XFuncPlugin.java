package _0.playground;

import java.sql.SQLException;

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
	protected void xFunc()
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

}
