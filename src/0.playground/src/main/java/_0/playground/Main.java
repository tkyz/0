package _0.playground;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0.Ansi;
import _0.ThreadFactory;
import _0._0;
import _0.debug.StopWatch;
import _0.playground.kvs.Kvs;
import _0.playground.sshd.Sshd;

public final class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static ScheduledExecutorService worker = null;
	protected static Config config = null;
	protected static Kvs kvs = null;
	protected static Idx idx = null;

	private Main() {
	}

	public static void main(final String... args)
			throws Throwable {

		log.trace("start");

		StopWatch sw = new StopWatch();
		try {

			debug(args);

			config();
			worker();
			kvs();
			clipboard();
			sshd();
			idx();

			cli();

		} finally {
			log.trace("end time={}", sw.stop());
		}

	}

	private static void debug(final String... args)
			throws IOException {

		if (log.isDebugEnabled()) {

			Comparator<Entry<?, ?>> sort = (o1, o2) -> o1.toString().compareTo(o2.toString());

			String path_separator = System.getProperty("path.separator");

			log.debug("{}---", Ansi.gray);
			log.debug("debug:");

			log.debug("  args:");
			if (null != args) {
				for (String arg : args) {
					log.debug("    - " + arg);
				}
			}

			log.debug("  env:");
			if (true) {

				Set<String> set = new HashSet<>();
				set.add("classpath");
				set.add("path");
				set.add("pathext");
				set.add("psmodulepath");
				set.add("session_manager");

				for (Entry<String, String> entry : System.getenv().entrySet().stream().sorted(sort).collect(Collectors.toList())) {

					if (set.contains(entry.getKey().toLowerCase())) {

						String[] paths = entry.getValue().toString().split(Pattern.quote(path_separator));
						log.debug("    " + entry.getKey() + ":");
						for (String path : paths) {
							log.debug("      - \"" + path + "\"");
						}

					} else {
						log.debug("    " + entry.getKey() + ": \"" + entry.getValue() + "\"");
					}

				}

			}

			log.debug("  prop:");
			if (true) {

				Set<String> set = new HashSet<>();
				set.add("java.class.path");
				set.add("java.library.path");

				for (Entry<Object, Object> entry : System.getProperties().entrySet().stream().sorted(sort).collect(Collectors.toList())) {

					if (set.contains(entry.getKey().toString().toLowerCase())) {

						String[] paths = entry.getValue().toString().split(Pattern.quote(path_separator));
						log.debug("    " + entry.getKey() + ":");
						for (String path : paths) {
							log.debug("      - \"" + path + "\"");
						}

					} else {
						log.debug("    " + entry.getKey() + ": \"" + entry.getValue() + "\"");
					}

				}

			}

			log.debug("{}", Ansi.reset);

		}

	}

	private static void config()
			throws IOException {
		config = new Config();
	}

	private static void worker() {

		worker = Executors.newScheduledThreadPool(Math.max(4, _0.availableProcessors >> 1), new ThreadFactory("worker/"));

		_0.shutdown(_0.methodName(), () -> worker.shutdown());

	}

	private static void kvs()
			throws SQLException {

		kvs = new Kvs();

		_0.shutdown(_0.methodName(), () -> {

			try {
				kvs.vacuum();
			} catch (IOException e) {
				log.trace("", e);
			} catch (SQLException e) {
				log.trace("", e);
			}

			_0.close(kvs);

		});

	}

	private static void clipboard() {

		_0.clipboard(val -> worker.submit(() -> {

			String[] items = val.toString().split("[\\r\\n]+");
			for (String item : items) {

				item = _0.trim(item);
				if ("".equals(item)) {
					continue;
				}

				try {
					kvs.set("clipboard", item.toString());
				} catch (IOException e) {
					log.trace("", e);
				} catch (SQLException e) {
					log.trace("", e);
				}

			}

		}));

	}

	private static void sshd()
			throws IOException {

		Sshd sshd = new Sshd();

		_0.shutdown(_0.methodName(), () -> {
			_0.close(sshd);
		});

	}

	private static void idx()
			throws IOException, SQLException {

		idx = new Idx();

		submit("idx", () -> {
			idx.run();
			return null;
		});

	}

	private static void cli()
			throws Exception {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {

			while (!_0.exit()) {

				try {

					String line = _0.trim(in.readLine());

					// TODO: 汎用化

					if ("".equals(line)) {
//						continue;
						break;
					}
					if ("exit".equals(line)) {
						break;
					}

					if ("??".equals(line)) {
						stacktrace(true);
						continue;
					}
					if ("?".equals(line)) {
						stacktrace(false);
						continue;
					}

					if ("!".equals(line)) {
						throw new Exception("cli");
					}

					if ("flush".equals(line)) {
						_0.flush(kvs);
						continue;
					}

					if (line.startsWith("load ")) {

						String idx_key = line.substring("load ".length()).trim();

						idx.load(idx_key);

						continue;

					}

					log.info(line);

				} catch (IOException e) {
					log.trace("", e);

				} catch (SQLException e) {
					log.trace("", e);
				}

			}

		}

	}

	private static void stacktrace(boolean all) {

		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();

		List<Thread> keys = new LinkedList<>(map.keySet());
		Collections.sort(keys, (o1, o2) -> (int)(o1.threadId() - o2.threadId()));

		log.info("threads:");
		for (Thread thread : keys) {

			List<StackTraceElement> stacks = new LinkedList<>();
			{

				StackTraceElement[] items = map.get(thread);
				for (StackTraceElement item : items) {

					if (!all && !item.toString().startsWith("app//")) {
						continue;
					}

					stacks.add(item);

				}

				Collections.reverse(stacks);

			}

			if (_0.empty(stacks)) {
				continue;
			}

			log.info("  - id: {}", thread.threadId());
			log.info("    name: {}", thread.getName());
			log.info("    stacktrace:");

			for (StackTraceElement stack : stacks) {
				log.info("      - {}", stack);
			}

		}

	}

	protected static <T> void submit(final String name, final Callable<T> impl) {
		submit(name, impl, 0);
	}

	protected static <T> void submit(final String name, final Callable<T> impl, final long millis) {
		submit(name, impl, millis, TimeUnit.MILLISECONDS);
	}

	protected static <T> void submit(final String name, final Callable<T> impl, final long delay, final TimeUnit unit) {

		@SuppressWarnings("unused")
		ScheduledFuture<T> future = worker.schedule(() -> {

			String origin = null;
			if (null != name) {
				origin = Thread.currentThread().getName();
				Thread.currentThread().setName(origin + "/" + name);
			}

			T ret = null;
			try {
				ret = impl.call();
			} catch (RuntimeException e) {
				log.trace("", e);
			} catch (Exception e) {
				log.trace("", e);
			}

			if (null != origin) {
				Thread.currentThread().setName(origin);
			}

			return ret;

		}, delay, unit);

	}

//	private static Map<String, Object> auth(final String host, final String type)
//			throws SQLException {
//
//		Map<String, Object> auth = new HashMap<>();
//
//		for (String key : kvs.keys("auth")) {
//
//			Map<String, Object> val = _0.select(kvs.get("auth", key), "val");
//
//			String host_ = _0.select(val, "host");
//			String type_ = _0.select(val, "type");
//
//			if (!host_.equals(host)) {
//				continue;
//			}
//			if (!type_.equals(type)) {
//				continue;
//			}
//
//			auth = val;
//			break;
//
//		}
//
//		return auth;
//
//	}

}
