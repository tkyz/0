package _0.playground.udf;

import java.sql.SQLException;
import java.util.regex.Pattern;

public final class Matches extends XFuncPlugin {

	@Override
	protected void impl()
			throws SQLException {

		String val   = value_text(0);
		String regex = value_text(1);

		boolean match = false;
		if (val != null) {
			match = Pattern.compile(regex).matcher(val).find();
		}

		result(match);

	}

}
