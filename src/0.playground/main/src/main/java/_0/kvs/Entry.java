package _0.kvs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entry implements java.util.Map.Entry<String, String> {

	private static final Logger log = LoggerFactory.getLogger(Entry.class);

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

	public Map<String, Object> json() {
		return json(val);
	}

	public static Map<String, Object> json(final byte[] bytes) {
		return json(new String(bytes));
	}

	public static Map<String, Object> json(final String val) {

		Map<String, Object> ret = null;

		if (null != val) {
			try {
				// TODO: conv nest map
				ret = new JSONObject(val).toMap();
			} catch (JSONException e) {
				log.warn("{}", val, e);
			}
		}

		return ret;

	}

	public static String json(final Map<String, Object> val) {
		return json(val, 0);
	}

	public static String json(final Map<String, Object> val, final int indent) {

		String ret = null;

		if (null != val) {
			try {
				// TODO: conv nest map
				ret = new JSONObject(val).toString(indent);
			} catch (JSONException e) {
				log.warn("{}", val, e);
			}
		}

		return ret;

	}

}
