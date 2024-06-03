package _0.kvs;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import _0.playground.core._0;

public final class Entry {

	private String              key = null;
	private Map<String, Object> val = null;

	private Entry() {
	}

	public static Entry of(final ResultSet rs)
			throws IOException, SQLException {

		String key = (String)rs.getObject("key");
		String val = (String)rs.getObject("val");

		return of(key, val);

	}

	public static Entry of(final String key)
			throws IOException {
		return of(key, (String)null);
	}

	public static Entry of(final String key, final String val)
			throws IOException {
		return of(key, _0.json(val));
	}

	public static Entry of(final String key, final Map<String, Object> val)
			throws IOException {

		Entry entry = new Entry();
		entry.key(key);
		entry.val(val);

		return entry;

	}

	private void key(final String key) {
		this.key = key;
	}

	public String key() {
		return key;
	}

	private void val(final Map<String, Object> val) {
		this.val = val;
	}

	public Map<String, Object> val() {
		val = null == val ? new HashMap<>() : val;
		return val;
	}

	public void val(final String valkey, final Object val) {
		_0.set(val(), valkey, val);
	}

	public <T> T val(final String valkey) {
		return _0.get(val(), valkey);
	}

	@Override
	public String toString() {
		return key();
	}

}
