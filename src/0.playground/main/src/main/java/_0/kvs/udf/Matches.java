package _0.kvs.udf;

import java.sql.SQLException;
import java.util.regex.Pattern;

import org.sqlite.Function;

public final class Matches extends Function {

	@Override
	protected final void xFunc()
			throws SQLException {

		String val   = value_text(0);
		String regex = value_text(1);

		result(null == val || null == regex || Pattern.compile(regex).matcher(val).find() ? 0 : 1);

	}

}
