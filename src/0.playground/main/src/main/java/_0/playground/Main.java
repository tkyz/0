package _0.playground;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileStore;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import _0.kvs.Entry;
import _0.kvs.Kvs;
import _0.playground.core._0;
import _0.playground.debug.Debug;
import _0.playground.debug.StopWatch;
import _0.playground.func.Func;
import _0.sshd.Sshd;

public final class Main implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static final int proc_block_size = 0x1000;

	public boolean proc = false;
	public boolean exit = false;

	public ExecutorService worker_task = null;
	public ExecutorService worker_file = null; // TODO: worker_host
	public ExecutorService worker_net  = null;
	public List<Future<?>> futures     = null;

	public HttpContext http_context = null;

	public static void main(final String... args)
			throws Throwable {

		StopWatch sw = new StopWatch();

		log.trace("start");

		try (Main main = new Main()) {}

		log.trace("end time={}", sw.stop());

	}

	public Main()
			throws ReflectiveOperationException, IOException, SQLException {
		init();
		task(() -> _cli(System.in));
		task(() -> _flush());
		task(() -> _seed());
		task(() -> _proc());
		wait_futures();
	}

	private void init()
			throws ReflectiveOperationException, IOException, SQLException {

		// worker
		worker_task = Executors.newFixedThreadPool(8);
		worker_file = Executors.newFixedThreadPool(1);
		worker_net  = Executors.newFixedThreadPool(UserConfig.tcp_sock);
		futures     = Collections.synchronizedList(new LinkedList<>());

		// design.yml
		Map<String, Object> design = null;
		{

			Yaml yaml = new Yaml();

			try (Reader reader = new FileReader(UserConfig.design.toFile())) {
				design = (Map<String, Object>)yaml.load(reader);
			}

			log.debug(yaml.dumpAsMap(design));

			design = _0.get(design, "design");

		}

		// define
		{

			// FIXME: replace
			Global.instance.run_dir  = Path.of(_0.get(design, "define.run_dir" ).toString().replace("${USER}", _0.username));
			Global.instance.blob_dir = Path.of(_0.get(design, "define.blob_dir").toString().replace("${USER}", _0.username));
			Global.instance.ref_dir  = Path.of(_0.get(design, "define.ref_dir" ).toString().replace("${USER}", _0.username));
//			Global.instance.text_dir = null;

			FileStore blobfs = Files.getFileStore(Global.instance.blob_dir);
			FileStore reffs  = Files.getFileStore(Global.instance.ref_dir);
			if (!blobfs.equals(reffs)) {
				throw new IllegalStateException();
			}

		}

		Global.instance.main = this;
		Global.instance.kvs  = new Kvs(Global.instance.run_dir.resolve("sqlite.db"));
//		Global.instance.sshd = new Sshd();

		// https?
		{

			Global.instance.cookie_dir  = Global.instance.run_dir.resolve("cookie");
			Global.instance.cookie_file = Global.instance.cookie_dir.resolve("dat");

			BasicCookieStore cookies = Files.exists(Global.instance.cookie_file) ? _0.read(Global.instance.cookie_file) : new BasicCookieStore();
			if (Files.exists(Global.instance.cookie_dir)) {
				Files.walkFileTree(Global.instance.cookie_dir, new FileVisitor<Path>() {

					@Override
					public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
							throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
							throws IOException {

						if (file.equals(Global.instance.cookie_file)) {
							return FileVisitResult.CONTINUE;
						}

						Map<String, Object> json = Entry.json(Files.readAllBytes(file));
						if (_0.empty(json)) {
							return FileVisitResult.CONTINUE;
						}

						Path p = Global.instance.cookie_dir.relativize(file);
						String ns     = p.getName(0).toString();
						String domain = _0.reverse(".", ns);
						String path   = "/";
						String name   = file.getFileName().toString();
						String value  = (String)json.get("value");
						String expire = (String)json.get("expire");

						for (int i = 1; i < p.getNameCount() - 1; i++) {
							path += p.getName(i) + "/";
						}

						BasicClientCookie cookie = new BasicClientCookie(name, value);
						cookie.setDomain(domain);
						cookie.setPath(path);
						cookie.setExpiryDate(null == expire ? null : Instant.parse(expire));

//						cookies.addCookie(cookie);

						return FileVisitResult.CONTINUE;

					}

					@Override
					public FileVisitResult visitFileFailed(final Path file, final IOException e)
							throws IOException {

						if (null != e) {
							log.warn("{}", e.getMessage());
						}

						return FileVisitResult.CONTINUE;

					}

					@Override
					public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
							throws IOException {

						if (null != e) {
							log.warn("{}", e.getMessage());
						}

						return FileVisitResult.CONTINUE;

					}

				});
			}

			http_context = HttpClientContext.create();
			http_context.setAttribute(HttpClientContext.COOKIE_STORE, cookies);

		}

	}

	private void wait_futures() {

		while (!_0.empty(futures)) {

			try {

				Future<?> future = futures.remove(0);

				if (!future.isDone()) {
					futures.add(future);
					_0.yield();
					continue;
				}

				@SuppressWarnings("unused")
				Object ret = future.get();

			} catch (CancellationException e) {
				log.trace("cancel {}", e.getMessage());

			} catch (ExecutionException | InterruptedException e) {
				log.warn("", e);
				worker_file.shutdownNow();
				worker_net.shutdownNow();
				worker_task.shutdownNow();
			}

		}

	}

	@Override
	public void close() {

		exit = true;

		if (null != worker_file) {
			worker_file.shutdown();
		}
		if (null != worker_net) {
			worker_net.shutdown();
		}
		if (null != worker_task) {
			worker_task.shutdown();
		}

		_0.close(Global.instance.sshd);
		_0.close(Global.instance.kvs);

		if (null != http_context) {

			BasicCookieStore cookies = (BasicCookieStore)http_context.getAttribute(HttpClientContext.COOKIE_STORE);
			for (Cookie cookie : cookies.getCookies()) {

				String  domain = cookie.getDomain();
				String  ns     = _0.reverse(".", domain);
				String  path   = cookie.getPath();
				String  name   = cookie.getName();
				String  value  = cookie.getValue();
				Instant expire = cookie.getExpiryInstant();

				Map<String, Object> json = new HashMap<>();
				json.put("value",  value);
				json.put("expire", expire);

				Path cookie_dir_ = Global.instance.cookie_dir.resolve(ns).resolve(path.replaceAll("^/", ""));
				Path cookie_file = cookie_dir_.resolve(name);

				try {
					Files.createDirectories(cookie_dir_);
					Files.writeString(cookie_file, Entry.json(json, 2));
				} catch (IOException e) {
					log.warn("", e);
				}

			}
			try {
				_0.write(Global.instance.cookie_file, cookies);
			} catch (IOException e) {
				log.warn("", e);
			}

		}

	}

	private Void _cli(final InputStream in)
			throws IOException {

		boolean system = System.in == in;

		Thread.currentThread().setName("task/cli");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			while (true) {

				String line = _0.trim(reader.readLine());
				if (_0.empty(line)) {
					proc = !proc;
					continue;
				}

				// TODO: 入力解析
				// TODO: 処理ハンドリング

				Callable<Void> task = null;

				if (system && "exit".equalsIgnoreCase(line)) {
					exit = true;
					break;
				}
				if ("?".equalsIgnoreCase(line)) {
					debug(false);
					continue;
				}
				if ("??".equalsIgnoreCase(line)) {
					debug(true);
					continue;
				}

				int idx = line.indexOf(" ");
				String line1 = -1 == idx ? line : line.substring(0, idx);
				String line2 = -1 == idx ? null : line.substring(idx + 1);

				if ("size".equalsIgnoreCase(line1)) {
					task = () -> _size();
				}
				if ("vacuum".equalsIgnoreCase(line1)) {
					task = () -> _vacuum();
				}
				if ("seed".equalsIgnoreCase(line1)) {
					task = () -> _seed();
				}
				if ("imp".equalsIgnoreCase(line1)) {
					Path path = null == line2 ? null : Path.of(line2);
					task = () -> _imp(path);
				}
				if ("exp".equalsIgnoreCase(line1)) {
					task = () -> _exp();
				}
				if ("walk".equalsIgnoreCase(line1)) {
					Path path = null == line2 ? Global.instance.ref_dir : Path.of(line2);
					task = () -> _walk(path);
				}
				if (null == task) {
					// TODO: null val
					Global.instance.kvs.set(line);
					task = () -> _proc(line, null, true);
				}

				task(task);

			}
		}

		return null;

	}

	private Void _size()
			throws SQLException {

		Thread.currentThread().setName("task/size");

		log.trace("{}", Global.instance.kvs.size());

		return null;

	}

	private Void _vacuum()
			throws SQLException {

		Thread.currentThread().setName("task/vacuum");

		Global.instance.kvs.vacuum();

		return null;

	}

	private Void _seed()
			throws IOException {

		task(() -> _imp(Global.instance.run_dir.resolve("seed.txt")));
		task(() -> _walk(Global.instance.ref_dir));

		for (Path i1 : Files.list(Global.instance.ref_dir.resolve(".uri")).toList()) {
			for (Path i2 : Files.list(i1).toList()) {

				String scheme = i1.getFileName().toString();
				String ns     = i2.getFileName().toString();
				String domain = _0.reverse(".", ns);
				String uri    = scheme + "://" + domain + "/";

				if ("http".equals(scheme)) {
					continue;
				}

				Global.instance.kvs.set(uri);

			}
		}

		return null;

	}

	private Void _imp(final Path file)
			throws IOException {

		Thread.currentThread().setName("task/imp");

		if (null == file) {
			return null;
		}
		if (!Files.exists(file)) {
			return null;
		}
		if (Files.isDirectory(file)) {
			return null;
		}

		Files.readAllLines(file).parallelStream()
				.forEach(Global.instance.kvs::set);

		return null;

	}

	private Void _exp() {

		Thread.currentThread().setName("task/exp");

		// TODO: exp

		return null;

	}

	private Void _walk(final Path target)
			throws IOException {

		Path path = target.toAbsolutePath().normalize();
		if (!Files.exists(path)) {
			return null;
		}

		Files.walkFileTree(path, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
					throws IOException {

				Thread.currentThread().setName("task/walk" + Main.toString(dir) + "/");

				if (exit) {
					return FileVisitResult.TERMINATE;
				}
				if (!UserConfig.walk_target.test(dir)) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
					throws IOException {

				Thread.currentThread().setName("task/walk" + Main.toString(file));

				if (exit) {
					return FileVisitResult.TERMINATE;
				}

				Global.instance.kvs.set("file://" + Main.toString(file));

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult visitFileFailed(final Path file, final IOException e)
					throws IOException {

				if (null != e) {
					log.warn("{}", e.getMessage());
				}

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
					throws IOException {

				if (null != e) {
					log.warn("{}", e.getMessage());
				}

				return FileVisitResult.CONTINUE;

			}

		});

		return null;

	}

	private Void _flush() {

		Thread.currentThread().setName("task/kvs/flush");
		while (!exit) {
			_0.yield();
			_0.flush(Global.instance.kvs);
		}

		return null;

	}

	private Void _proc()
			throws SQLException {

		Thread.currentThread().setName("task/kvs/proc");
		while (!exit) {

			_0.yield();
			if (!proc) {
				continue;
			}

			Set<String> proced = new HashSet<>();

			List<Entry> entries = Global.instance.kvs.rand(proc_block_size);
			while (!_0.empty(entries)) {

				Entry entry = entries.remove(0);
				String key = entry.getKey();
				String val = entry.getValue();

				String rep = UserConfig.normalize_key.apply(key);
				if (0 != _0.compare(key, rep)) {
					Global.instance.kvs.del(key);
					if (null == rep) {
						continue;
					}
					key = rep;
					Global.instance.kvs.set(key, val);
				}
				if (!proced.add(key)) {
					continue;
				}
				while (UserConfig.tcp_sock <= remain(worker_file) + remain(worker_net)) {
					_0.yield();
				}
				if (exit) {
					break;
				}
				if (!proc) {
					continue;
				}

				_proc(key, val, false);

			}

		}

		return null;

	}

	private <T> Void _proc(final String key, final String val, final boolean cli) {

		int idx = key.indexOf("://");
		if (-1 < idx) {

			String base   = getClass().getPackageName() + ".func.uri";
			String scheme = key.substring(0, idx);
			String host   = key.substring(idx + 3, key.length()).replaceAll("/.*", "").replace("-", "_");

			if ("http".equals(scheme)) {
				scheme = "https";
			}
			if (!_0.empty(host)) {
				host = _0.reverse(".", host);
			}

			List<String> cps = new LinkedList<>();
			if (!_0.empty(scheme) && !_0.empty(host)) {
				cps.add(base + "." + scheme + "." + host + ".Impl");
			}
			if (!_0.empty(scheme)) {
				cps.add(base + "." + scheme + ".Impl");
			}

			Func<?> func = null;
			Map<String, Object> map = null;
			while (null == func && !_0.empty(cps)) {

				if (null == map) {
					map = Entry.json(val);
				}
				if (null == map) {
					map = new HashMap<>();
				}

				try {
					func = (Func<?>)Class.forName(cps.remove(0)).getConstructor(new Class[] {String.class, Map.class}).newInstance(key, map);
				} catch (ReflectiveOperationException e) {
				} catch (ClassCastException e) {
				}

			}
			if (null == func) {
				log.warn("{}", key);
				return null;
			}

			Func<?> func_ = func;
			futures.add(worker_file.submit(() -> {
				Thread.currentThread().setName("task/kvs/proc/" + key.replaceAll(":.*", ""));
				return func_.call();
			}));

		}

		return null;

	}

	private void task(final Callable<Void> task) {
		futures.add(worker_task.submit(task));
	}

	private int remain() {
		return futures.size();
	}

	private static int remain(final ExecutorService worker) {

		ThreadPoolExecutor worker_ = (ThreadPoolExecutor)worker;

		return (int)(worker_.getTaskCount() - worker_.getCompletedTaskCount());

	}

	private void debug(final boolean all) {

		Debug.println(all);
		if (all) {

			System.out.println("cookie:");

			((CookieStore)http_context.getAttribute(HttpClientContext.COOKIE_STORE)).getCookies().stream()
					.sorted((o1, o2) -> _0.compare(o1.getName(),   o2.getName()))
					.sorted((o1, o2) -> _0.compare(o1.getPath(),   o2.getPath()))
					.sorted((o1, o2) -> _0.compare(o1.getDomain(), o2.getDomain()))
					.forEach(e -> System.out.println("  - " + e));

		}
		System.out.println("remain:");
		System.out.println("  task: " + remain(worker_task));
		System.out.println("  file: " + remain(worker_file));
		System.out.println("  net:  " + remain(worker_net));
		System.out.println("futures: " + remain());

	}

	public static String toString(final Path path) {
		return ("/" + path.toAbsolutePath().normalize())
				.replace('\\', '/')
				.replaceAll("/+", "/");
	}

	public static String date(final long millis) {
		return date(new Date(millis));
	}

	public static String date(final Date date) {

		String ret = null;

		if (null != date) {
			synchronized (format_date) {
				ret = format_date.format(date);
			}
		}

		return ret;

	}

	public static final class Global {

		public static final Global instance = of();

		public Path run_dir  = null;
		public Path blob_dir = null;
		public Path ref_dir  = null;
//		public Path text_dir = null;

		public Path cookie_dir  = null;
		public Path cookie_file = null;

		public Main main = null;
		public Kvs  kvs  = null;
		public Sshd sshd = null;

		private Global() {
		}

		private static final Global of() {
			return new Global();
		}

	}

}
