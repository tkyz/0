package _0.playground.udf;

import java.sql.SQLException;

public final class Replace extends XFuncPlugin {

	@Override
	protected void impl()
			throws SQLException {

		String val         = value_text(0);
		String regex       = value_text(1);
		String replacement = value_text(2);

		if (val != null) {
			val = val.replaceAll(regex, replacement);
		}

		result(val);

	}

}
