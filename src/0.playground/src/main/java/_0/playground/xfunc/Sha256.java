package _0.playground.xfunc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import _0._0;

public class Sha256 extends XFuncPlugin {

	@Override
	protected void impl()
			throws SQLException, NoSuchAlgorithmException {

		String val = value_text(0);

		if (val != null) {

			MessageDigest md = MessageDigest.getInstance("SHA-256");

			md.update(val.getBytes());

			byte[] digest = md.digest();

			val = _0.hex(digest);

		}

		result(val);

	}

}
