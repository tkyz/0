package _0.kvs.udf;

import java.sql.SQLException;

import org.sqlite.Function;

public final class Getenv extends Function {

	@Override
	protected final void xFunc()
			throws SQLException {

		String val = value_text(0);
		String ret = null;

		if (val != null) {
			ret = System.getenv(val);
		}

		result(ret);

	}

}
