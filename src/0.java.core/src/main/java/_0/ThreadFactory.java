package _0;

public final class ThreadFactory implements java.util.concurrent.ThreadFactory {

	private String prefix = null;

	private int cnt = 0;

	public ThreadFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Thread newThread(Runnable runnable) {

		Thread thread = new Thread(runnable, prefix + cnt++);
		thread.setDaemon(true);

		return thread;

	}

}
