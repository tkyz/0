package _0.kvs;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Entry implements java.util.Map.Entry<String, String> {

	private String key = null;
	private String val = null;

	public Entry(final String key) {
		this(key, (String)null);
	}

	public Entry(final String key, final String val) {
		this.key = key;
		this.val = val;
	}

	public Entry(final ResultSet rs)
			throws SQLException {
		this((String)rs.getObject("key"), (String)rs.getObject("val"));
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return val;
	}

	@Override
	public String setValue(final String val) {
		String old = this.val;
		this.val = val;
		return old;
	}

}
