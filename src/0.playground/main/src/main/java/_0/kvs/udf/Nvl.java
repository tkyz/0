package _0.kvs.udf;

import java.sql.SQLException;

import org.sqlite.Function;

public final class Nvl extends Function {

	@Override
	protected final void xFunc()
			throws SQLException {

		String val = null;

		for (int i = 0; i < args(); i++) {

			if (null != val) {
				break;
			}

			val = value_text(i);

		}

		result(val);

	}

}
