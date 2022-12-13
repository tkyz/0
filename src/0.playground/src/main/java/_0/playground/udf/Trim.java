package _0.playground.udf;

import java.sql.SQLException;

import _0._0;

public final class Trim extends XFuncPlugin {

	@Override
	protected void impl()
			throws SQLException {

		String val = value_text(0);

		if (val != null) {
			val = _0.trim(val);
		}

		result(val);

	}

}
