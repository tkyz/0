package _0.kvs.udf;

import java.io.IOException;
import java.sql.SQLException;

import org.sqlite.Function;

import _0.playground.core._0;

public final class Sha256 extends Function {

	@Override
	protected final void xFunc()
			throws SQLException {

		String val = value_text(0);
		String ret = null;

		if (null != val) {
			try {
				ret = _0.hex(_0.sha256(val));
			} catch (IOException e) {
				throw new UdfException(e);
			}
		}

		result(ret);

	}

}
