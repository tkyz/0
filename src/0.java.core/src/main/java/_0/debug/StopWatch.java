package _0.debug;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class StopWatch {

	private SimpleDateFormat df = null;

	private long start = 0;

	// TODO: days
	public StopWatch() {
		this("HH:mm:ss.SSS");
	}

	public StopWatch(final String pattern) {

		df = new SimpleDateFormat(pattern);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		start();

	}

	public void start() {
		start = System.currentTimeMillis();
	}

	public String stop() {
		return df.format(new Date(System.currentTimeMillis() - start));
	}

}
