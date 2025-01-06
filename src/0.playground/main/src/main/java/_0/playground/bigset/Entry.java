package _0.playground.bigset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class Entry implements Map.Entry<byte[], byte[]> {

	private byte[] key = null;
	private byte[] val = null;

	protected Entry(final ResultSet rs)
			throws SQLException {
		key = rs.getBytes("key");
		val = rs.getBytes("val");
	}

	@Override
	public final byte[] getKey() {
		return key;
	}

	@Override
	public final byte[] getValue() {
		return val;
	}

	@Override
	@Deprecated
	public final byte[] setValue(final byte[] val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {

		byte[] k1 = getKey();
		byte[] k2 = ((Entry)obj).getKey();

		return Arrays.equals(k1, k2);

	}

	@Override
	public final String toString() {
		return new String(getKey());
	}

}
