package _0.playground.xfunc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import _0._0;

public final class Hash extends XFuncPlugin {

	@Override
	protected void impl()
			throws SQLException, NoSuchAlgorithmException {

		String val = value_text(0);
		String alg = value_text(1);

		if (val != null) {

			MessageDigest md = MessageDigest.getInstance(alg);

			md.update(val.getBytes());

			byte[] digest = md.digest();

			val = _0.hex(digest);

		}

		result(val);

	}

}
