package _0.kvs.udf;

import java.sql.SQLException;

@SuppressWarnings("serial")
public final class UdfException extends SQLException {

	@Deprecated
	public UdfException() {
	}

	@Deprecated
	public UdfException(String reason) {
		super(reason);
	}

	public UdfException(Throwable cause) {
		super(cause);
	}

	public UdfException(String reason, Throwable cause) {
		super(reason, cause);
	}

}
