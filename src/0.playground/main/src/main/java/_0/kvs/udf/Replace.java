package _0.kvs.udf;

import java.sql.SQLException;

import org.sqlite.Function;

public final class Replace extends Function {

	@Override
	protected final void xFunc()
			throws SQLException {

		String val         = value_text(0);
		String regex       = value_text(1);
		String replacement = value_text(2);
		String ret         = null;

		if (val != null) {
			ret = val.replaceAll(regex, replacement);
		}

		result(ret);

	}

}
