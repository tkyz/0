package _0.kvs.udf;

import java.sql.SQLException;
import java.util.List;

import org.json.JSONArray;
import org.sqlite.Function;

public final class Split extends Function {

	@Override
	protected final void xFunc()
			throws SQLException {

		String val   = value_text(0);
		String regex = value_text(1);
		int    index = args() < 3 ? -1 : value_int(2);
		String ret   = null;

		if (val != null) {

			List<String> items = List.of(val.split(regex));

			ret = -1 < index ? items.get(index) : new JSONArray(items).toString();

		}

		result(ret);

	}

}
