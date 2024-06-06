package _0.playground.debug;

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
		return stop(false);
	}

	public String stop(final boolean restart) {

		String stop = df.format(new Date(System.currentTimeMillis() - start));

		if (restart) {
			start();
		}

		return stop;

	}

}
