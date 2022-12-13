package _0.playground.udf;

import java.sql.SQLException;

public final class Split extends XFuncPlugin {

	@Override
	protected void impl()
			throws SQLException {

		String val   = value_text(0);
		String delim = value_text(1);
		int    index = value_int(2);

		if (val != null) {
			val = val.split(delim)[index];
		}

		result(val);

	}

}
