package _0.playground.udf;

import java.sql.SQLException;

public final class Nvl extends XFuncPlugin {

	@Override
	protected void impl()
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
