package _0.kvs.udf;

import java.sql.SQLException;

import org.sqlite.Function;

import _0.playground.core._0;

public final class Eq extends Function {

	@Override
	protected final void xFunc()
			throws SQLException {
		result(0 == _0.compare(value_text(0), value_text(1)) ? 1 : 0);
	}

}
