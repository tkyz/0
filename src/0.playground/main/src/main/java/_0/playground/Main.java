package _0.playground;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.FileStore;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0.kvs.Entry;
import _0.kvs.Kvs;
import _0.playground.core.Regex;
import _0.playground.core._0;
import _0.playground.debug.Debug;
import _0.playground.debug.StopWatch;
import _0.sshd.Sshd;

public final class Main implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final SimpleDateFormat format_date          = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat format_last_modified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

	private static boolean exit = false;

	private static final Path run_vol     = UserImpl.run_vol;
	private static final Path run_dir     = run_vol.resolve("." + _0.hex(_0.openpgp4fpr));
	private static final Path seed_file   = run_dir.resolve("seed.txt");
	private static final Path token_file  = run_dir.resolve("token.txt");
	private static final Path cookie_dir  = run_dir.resolve("cookie");
	private static final Path cookie_file = cookie_dir.resolve("dat");
	private static final Path kvs_file    = run_dir.resolve("sqlite.db");

	private static final Path blob_vol    = UserImpl.blob_vol;
	private static final Path blob_dir    = blob_vol.resolve("blob");
	private static final Path ref_dir     = blob_vol.resolve("ref");
	private static final Path ref_uri_dir = ref_dir.resolve("uri");

	private static final Path text_vol    = UserImpl.text_vol;
	private static final Path text_dir    = text_vol.resolve("text");

	private Set<String> token = new HashSet<>();

	private FileStore   file_store   = null;
	private HttpContext http_context = null;

	private ExecutorService worker_task = null;
	private ExecutorService worker_file = null; // TODO: worker_host
	private ExecutorService worker_net  = null;
	private List<Future<?>> futures     = null;

	private Kvs  kvs  = null;
	private Sshd sshd = null;

	private static       boolean proc            = false;
	private static final int     proc_block_size = 0x1000;

	private static final Function<String, String> normalize_key = key -> {

		String rep = key;

		if (rep.matches("^https?://.*$")) {

			try {
				rep = new URI(rep).normalize().toString();
				rep = rep.replaceAll("/[\\.]+/", "/");
			} catch (URISyntaxException e) {
			}
			rep = rep.replaceAll("#[^/]*$", "");
			rep = rep.replaceAll("\\?[0-9]+$", "");
			rep = rep.replaceAll("[\\?&]+$", "");

		}

		return rep;

	};

	public static void main(final String... args)
			throws Throwable {

		StopWatch sw = new StopWatch();

		log.trace("start");

		try (Main main = new Main()) {
			main.wait_futures();
		}

		log.trace("end time={}", sw.stop());

	}

	public Main()
			throws ReflectiveOperationException, IOException, SQLException {
		init();
	}

	private void init()
			throws ReflectiveOperationException, IOException, SQLException {

		log.trace("volume: {}", blob_vol);

		file_store   = Files.getFileStore(blob_vol);
		http_context = HttpClientContext.create();

		worker_task = Executors.newFixedThreadPool(8);
		worker_file = Executors.newFixedThreadPool(1);
		worker_net  = Executors.newFixedThreadPool(UserImpl.tcp_sock);
		futures     = Collections.synchronizedList(new LinkedList<>());

		kvs  = new Kvs(kvs_file);
//		sshd = new Sshd();

		_load();

		task(() -> _cli(System.in));
		task(() -> _flush());
		task(() -> _seed());
		task(() -> _proc());

	}

	private Void _load()
			throws IOException, ClassNotFoundException {

		if (Files.exists(token_file)) {
			Files.readAllLines(token_file).stream()
//					.map(e -> List.of(e.split("\\t")))
//					.flatMap(e -> e.stream())
					.filter(e -> !_0.empty(e))
					.filter(e -> !e.startsWith("#"))
					.forEach(token::add);
		}

		BasicCookieStore cookies = new BasicCookieStore();
		if (Files.exists(cookie_file)) {
			cookies = _0.read(cookie_file);
		}
//		if (Files.exists(cookie_dir)) {
//			Files.walkFileTree(cookie_dir, new FileVisitor<Path>() {
//
//				@Override
//				public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
//						throws IOException {
//					return FileVisitResult.CONTINUE;
//				}
//
//				@Override
//				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
//						throws IOException {
//
//					Path p = cookie_dir.relativize(file);
//
//					Map<String, Object> json = Kvs.json(Files.readAllBytes(file));
//					if (_0.empty(json)) {
//						return FileVisitResult.CONTINUE;
//					}
//
//					String ns     = p.getName(0).toString();
//					String domain = _0.reverse(".", ns);
//					String path   = "/";
//					String name   = file.getFileName().toString();
//					String value  = (String)json.get("value");
//					String expire = (String)json.get("expire");
//
//					for (int i = 1; i < p.getNameCount() - 1; i++) {
//						path += p.getName(i) + "/";
//					}
//
//					BasicClientCookie cookie = new BasicClientCookie(name, value);
//					cookie.setDomain(domain);
//					cookie.setPath(path);
//					cookie.setExpiryDate(null == expire ? null : Instant.parse(expire));
//
//					cookies.addCookie(cookie);
//
//					return FileVisitResult.CONTINUE;
//
//				}
//
//				@Override
//				public FileVisitResult visitFileFailed(final Path file, final IOException e)
//						throws IOException {
//
//					if (null != e) {
//						log.warn("{}", e.getMessage());
//					}
//
//					return FileVisitResult.CONTINUE;
//
//				}
//
//				@Override
//				public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
//						throws IOException {
//
//					if (null != e) {
//						log.warn("{}", e.getMessage());
//					}
//
//					return FileVisitResult.CONTINUE;
//
//				}
//
//			});
//		}
		http_context.setAttribute(HttpClientContext.COOKIE_STORE, cookies);

		return null;

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

		_0.close(sshd);
		_0.close(kvs);

		BasicCookieStore cookies = (BasicCookieStore)http_context.getAttribute(HttpClientContext.COOKIE_STORE);
//		for (Cookie cookie : cookies.getCookies()) {
//
//			String  domain = cookie.getDomain();
//			String  ns     = _0.reverse(".", domain);
//			String  path   = cookie.getPath();
//			String  name   = cookie.getName();
//			String  value  = cookie.getValue();
//			Instant expire = cookie.getExpiryInstant();
//
//			Map<String, Object> json = new HashMap<>();
//			json.put("value",  value);
//			json.put("expire", expire);
//
//			Path cookie_dir  = Main.cookie_dir.resolve(ns).resolve(path.replaceAll("^/", ""));
//			Path cookie_file = cookie_dir.resolve(name);
//
//			try {
//				Files.createDirectories(cookie_dir);
//				Files.writeString(cookie_file, Kvs.json(json, 2));
//			} catch (IOException e) {
//				log.warn("", e);
//			}
//
//		}
		try {
			_0.write(cookie_file, cookies);
		} catch (IOException e) {
			log.warn("", e);
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

				kvs.set(line);

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
					Path path = null == line2 ? ref_dir : Path.of(line2);
					task = () -> _walk(path);
				}
				if (null == task) {
					// TODO: null val
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

		log.trace("{}", kvs.size());

		return null;

	}

	private Void _vacuum()
			throws SQLException {

		Thread.currentThread().setName("task/vacuum");

		kvs.vacuum();

		return null;

	}

	private Void _seed()
			throws IOException {

		task(() -> _imp(seed_file));
		task(() -> _walk(ref_dir));

		for (Path i1 : Files.list(ref_uri_dir).toList()) {
			for (Path i2 : Files.list(i1).toList()) {

				String scheme = i1.getFileName().toString();
				String ns     = i2.getFileName().toString();
				String domain = _0.reverse(".", ns);
				String uri    = scheme + "://" + domain + "/";

				kvs.set(uri);

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
				.forEach(kvs::set);

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

				Thread.currentThread().setName("task/walk" + Main.toString(dir));

				String lower = dir.toString().toLowerCase();

				boolean skip = false;
				skip |= dir.startsWith(run_dir);
				skip |= dir.startsWith(cookie_dir);
				skip |= dir.startsWith(blob_dir);
				skip |= dir.startsWith(ref_uri_dir);
				skip |= -1 < lower.indexOf("/.#");
				skip |= -1 < lower.indexOf("/.trash-");
				skip |= -1 < lower.indexOf("/lost+found");
				skip |= -1 < lower.indexOf("/$recycle.bin");
				if (skip) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
					throws IOException {

				Thread.currentThread().setName("task/walk" + Main.toString(file));

				boolean skip = false;
				skip |= file.startsWith(seed_file);
				skip |= file.startsWith(token_file);
				skip |= file.startsWith(kvs_file);
				if (skip) {
					return FileVisitResult.CONTINUE;
				}

				kvs.set("file://" + Main.toString(file));

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
			_0.flush(kvs);
		}

		return null;

	}

	private Void _proc()
			throws SQLException {

		Thread.currentThread().setName("task/kvs/proc");
		while (!exit) {

			_0.yield();

			Set<String> proced = new HashSet<>();

			List<Entry> entries = kvs.rand(proc_block_size);
			while (!_0.empty(entries)) {

				Entry entry = entries.remove(0);
				String key = entry.getKey();
				String val = entry.getValue();

				String rep = normalize_key
						.andThen(UserImpl.normalize_key)
						.apply(key);

				if (0 != _0.compare(key, rep)) {
					kvs.del(key);
					if (null != rep) {
						kvs.set(rep, val);
					}
					key = rep;
				}
				if (null == key) {
					continue;
				}
				if (!proced.add(key)) {
					continue;
				}

				while (UserImpl.tcp_sock <= remain(worker_file) + remain(worker_net)) {
					_0.yield();
				}
				if (!proc) {
					continue;
				}
				if (exit) {
					break;
				}

				_proc(key, val, false);

			}

		}

		return null;

	}

	private <T> Void _proc(final String key, final String val, final boolean cli) {

		// TODO: key type handling
		List<KvsEntryProc<?>> procs = new LinkedList<>();
		procs.add(new KvsEntryProc_openpgp4fpr());
		procs.add(new KvsEntryProc_blob());
		procs.add(new KvsEntryProc_file());
		procs.add(new KvsEntryProc_http());

		boolean log = true;
		while (!_0.empty(procs)) {

			@SuppressWarnings("unchecked")
			KvsEntryProc<T> proc = (KvsEntryProc<T>)procs.remove(0);

			T target = proc.type(key);
			if (null == target) {
				continue;
			}
			log = false;

			futures.add(proc.worker().submit(() -> {

				Thread.currentThread().setName("task/kvs/proc/" + proc.getClass().getName().replaceAll(".*_", ""));

				return proc._call(key, target, _0.nvl(Kvs.json(val), new HashMap<>()), cli);

			}));

		}

		if (log) {
			String n = Thread.currentThread().getName();
			Thread.currentThread().setName("task/kvs/proc/skip");
			debug(null, " #", key);
			Thread.currentThread().setName(n);
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

	private static String date(final Date date) {

		String ret = null;

		if (null != date) {
			synchronized (format_date) {
				ret = format_date.format(date);
			}
		}

		return ret;

	}

	private static Date date(final HttpResponse res) {

		Date ret = null;

		Header header = res.getFirstHeader("last-modified");
		if (null != header) {

			String val = header.getValue();

			try {
				synchronized (format_last_modified) {
					ret = format_last_modified.parse(val);
				}
			} catch (ParseException e) {
				log.warn("{} {}", val, e.toString());
			}

		}

		return ret;

	}

	public interface KvsEntryProc<T> {

		public T type(final String key);

		public ExecutorService worker();

		public Void _call(final String orgkey, final T key, final Map<String, Object> val, final boolean cli)
				throws Exception;

	}

	public class KvsEntryProc_openpgp4fpr implements KvsEntryProc<String> {

		@Override
		public String type(final String key) {
			return key.startsWith("openpgp4fpr:") ? key : null;
		}

		@Override
		public ExecutorService worker() {
			return worker_file;
		}

		@Override
		public Void _call(final String orgkey, final String key, final Map<String, Object> val, final boolean cli)
				throws IOException {

//			key.matches("^openpgp4fpr:[0-9a-fA-F]{40}$");

			debug(null, "# ", orgkey);

			return null;

		}

	}

	public class KvsEntryProc_blob implements KvsEntryProc<String> {

		@Override
		public String type(final String key) {
			return key.startsWith("blob:") ? key : null;
		}

		@Override
		public ExecutorService worker() {
			return worker_file;
		}

		@Override
		public Void _call(final String orgkey, final String key, final Map<String, Object> val, final boolean cli)
				throws IOException {

//			key.matches("^blob:[0-9a-fA-F]{8}$");
//			key.matches("^blob:[0-9a-fA-F]{32}$");
//			key.matches("^blob:[0-9a-fA-F]{40}$");
//			key.matches("^blob:[0-9a-fA-F]{64}$");
//			key.matches("^blob:[0-9a-fA-F]{128}$");

			debug(null, "# ", orgkey);

			return null;

		}

	}

	public class KvsEntryProc_file implements KvsEntryProc<Path> {

		@Override
		public Path type(final String key) {
			return key.startsWith("file://") ? Path.of(key.substring("file://".length())) : null;
		}

		@Override
		public ExecutorService worker() {
			return worker_file;
		}

		@Override
		public Void _call(final String orgkey, final Path key, final Map<String, Object> val, final boolean cli)
				throws IOException {

			if (!Files.exists(key)) {
				kvs.del(orgkey);
				debug(null, " ?", orgkey);
				return null;
			}
			if (Files.isSymbolicLink(key)) {
				kvs.del(orgkey);
				debug(null, " s", orgkey);
				return null;
			}
			if (Files.isDirectory(key)) {
				kvs.del(orgkey);
				debug(null, " d", orgkey);
				return null;
			}

			boolean skip = false;
			skip |= key.startsWith(run_dir);
			skip |= key.startsWith(cookie_dir);
			skip |= key.startsWith(blob_dir);
			skip |= key.startsWith(ref_uri_dir);
			skip |= key.startsWith(text_dir);
			skip |= key.startsWith(seed_file);
			skip |= key.startsWith(token_file);
			skip |= key.startsWith(kvs_file);
			skip |= kvs_file.getFileName().toString().startsWith("$");
			skip |= kvs_file.getFileName().toString().startsWith(".#");
			skip |= kvs_file.getFileName().toString().startsWith(".Trash-");
			skip |= kvs_file.getFileName().toString().startsWith("lost+found");
			if (skip) {
				kvs.del(orgkey);
				debug(null, " #", orgkey);
				return null;
			}

			// upd val
			boolean upd = false;
			long   size   = 0;
			long   date   = 0;
			String ymdhms = null;
			String hash   = null;
			{

				BasicFileAttributes attrs = Files.readAttributes(key, BasicFileAttributes.class);
				size   = attrs.size();
				date   = _0.max(attrs.creationTime().toMillis(), attrs.lastModifiedTime().toMillis());
				ymdhms = date(new Date(date));

				Number prev_size   = _0.get(val, "meta/size");
				String prev_ymdhms = _0.get(val, "meta/date");
				String prev_hash   = _0.get(val, "meta/hash");

				upd |= null == prev_hash;
				upd |= 0 != _0.compare(prev_size,   size);
				upd |= 0 != _0.compare(prev_ymdhms, ymdhms);
				if (upd) {

					hash = _0.hex(_0.sha256(key));

					Map<String, Object> val_ = new HashMap<>();
					_0.set(val_, "meta/size", size);
					_0.set(val_, "meta/date", ymdhms);
					_0.set(val_, "meta/hash", hash);

					kvs.set(orgkey, val_);

				} else {
					hash = prev_hash;
				}

			}

			boolean is_vol = is_vol(key);
			Path blob_dir  = Main.blob_dir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4));
			Path blob_file = blob_dir.resolve(hash);

			// copy blob
			if (!Files.exists(blob_file) && !is_vol) {

				String lower = Main.toString(key).toLowerCase();

				// TODO: 残ストレージ

				boolean copy = false;
				copy |= lower.endsWith(".gif");
				copy |= lower.endsWith(".png");
				copy |= lower.endsWith(".jpg");
				copy |= lower.endsWith(".jpeg");
				copy |= lower.endsWith(".bmp");
				copy |= lower.endsWith(".pdf");
				copy |= lower.endsWith(".mp3");
				copy |= lower.endsWith(".wav");
				copy |= lower.endsWith(".avi");
				copy |= lower.endsWith(".flv");
				copy |= lower.endsWith(".mp4");
				copy |= lower.endsWith(".mpg");
				copy |= lower.endsWith(".mpeg");
				copy |= lower.endsWith(".wmv");

				if (copy) {

					Files.createDirectories(blob_dir);
					Files.copy(key, blob_file);
					Files.setLastModifiedTime(blob_file, FileTime.fromMillis(date));

					debug(hash, "<<", orgkey);

					// TODO: ref/uri/file/...
					Path copy_dir  = blob_vol.resolve("ref.copy");
					Path copy_file = copy_dir.resolve(hash + "_" + key.getFileName().toString().replace(hash, ""));

					Files.createDirectories(copy_dir);
					Files.createLink(copy_file, blob_file);

				}

			}

			// link blob
			if (!Files.exists(blob_file) && is_vol) {

				Files.createDirectories(blob_dir);
				Files.createLink(blob_file, key);

				debug(hash, "<-", orgkey);

				Files.createDirectories(blob_vol.resolve("ref.new"));
				Files.createLink(blob_vol.resolve("ref.new").resolve(hash), blob_file);

			}

			// link ref
			if (is_vol && _0.ino(key) != _0.ino(blob_file)) {

				long ms1 = Files.getLastModifiedTime(key).toMillis();
				long ms2 = Files.getLastModifiedTime(blob_file).toMillis();
				Files.setLastModifiedTime(blob_file, FileTime.fromMillis(Math.min(ms1, ms2)));

				// TODO: 移動
				Files.delete(key);
				Files.createLink(key, blob_file);

				debug(hash, "->", orgkey);

			} else if (is_vol && _0.ino(key) == _0.ino(blob_file) && upd) {
				debug(hash, "->", orgkey);
			}

			return null;

		}

//		private static List<String> tags(final String key) {
//
//			List<String> tags = new LinkedList<>(List.of(key.split("/")));
//			tags.remove(0);
//
//			String filename = tags.getLast();
//			if (-1 < tags.getLast().indexOf(".")) {
//
//				int index = filename.lastIndexOf(".");
//
//				String name = filename.substring(0, index);
//				String ext  = filename.substring(index + 1, filename.length()).toLowerCase();
//
//				tags.removeLast();
//				tags.add(name);
//				tags.add(ext);
//
//			}
//
//			return tags.parallelStream()
//					.map(_0::normalize)
//					.flatMap(e -> _0.brackets(e, true).stream())
//					.map(e -> e.replaceAll("_+", "_"))
//					.map(e -> e.replaceAll(Regex.spaces, " "))
//					.map(e -> e.replaceAll("^[_ ]+|[_ ]+$", ""))
//					.filter(e -> !e.equals(""))
//					.filter(e -> !e.equals("home"))
//					.filter(e -> !e.equals(_0.username))
//					.filter(e -> !e.equals(_0.hex(_0.openpgp4fpr)))
//					.filter(e -> !e.matches("^[0-9_\\-]+$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{8}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{32}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{40}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{64}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{128}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{8}([_\\-][0-9a-fA-F]{4}){3}[_\\-][0-9a-fA-F]{12}$"))
//					.sequential()
//					.distinct()
//					.toList();
//
//		}

	}

	public class KvsEntryProc_http implements KvsEntryProc<URI> {

		@Override
		public URI type(final String key) {

			URI target = null;

			if (key.startsWith("http://") || key.startsWith("https://")) {

				try {

					String enc = key;
					enc = enc.replaceAll(" ",   "%20");
					enc = enc.replaceAll("　",  "%E3%80%80");
					enc = enc.replaceAll("\\|", "%7C");
					enc = enc.replaceAll("\\{", "%7B");
					enc = enc.replaceAll("\\}", "%7D");

					target = new URI(enc);

				} catch (URISyntaxException e) {
					log.warn("{}", e.getMessage());
				}

			}

			return target;

		}

		@Override
		public ExecutorService worker() {
			return worker_net;
		}

		@Override
		public Void _call(final String orgkey, final URI key, final Map<String, Object> val, final boolean cli)
				throws IOException, NoSuchAlgorithmException, SQLException {

			String prev_hash = _0.get(val, "data/hash");
			if (null != prev_hash) {
				debug(prev_hash, ". ", orgkey);
				return null;
			}

			boolean target = false;
			target |= key.getHost().matches("^" + Regex.subdomain + "*\\.0$");
			target |= UserImpl.http_target.test(key);
			if (!target) {
				debug(null, "# ", orgkey);
				return null;
			}

			String   hash    = null;
			String   date    = null;
			Number   size    = null;
			String   mime    = null;
			Number   code    = null;
			Charset  charset = null;
			String   text    = null;
			Document html    = null;
			Number   width   = null;
			Number   height  = null;

			Path uri_file = ref_uri_dir
					.resolve(key.getScheme())
					.resolve(_0.reverse(".", key.getHost()))
					.resolve(key.getPath().replaceAll("^/", ""));

			RequestConfig config = RequestConfig.custom()
//					.setConnectTimeout(10 * 1000)
//					.setConnectionRequestTimeout(60 * 1000)
//					.setSocketTimeout(3 * 60 * 1000)
					.build();

			HttpUriRequestBase req = new HttpGet(key);
			req.addHeader("User-Agent",      "Mozilla/5.0");
			req.addHeader("Accept-Charaset", "UTF-8");
			req.addHeader("Accept-Language", "ja, en;");
			req.setConfig(config);

			try (CloseableHttpClient client = HttpClientBuilder.create().build(); CloseableHttpResponse res = client.execute(req, http_context)) {

				// TODO: redirect
				code = res.getCode();

				HttpEntity entity = res.getEntity();

				ContentType content_type  = ContentType.parse(entity.getContentType());
				Date        last_modified = date(res);

				mime = null == content_type ? null : content_type.getMimeType().toLowerCase();

				// TODO: mime handling
				if (null == mime) {
					debug(mime, "? ", orgkey);

				} else if ("text/plain".equals(mime)) {

					charset = content_type.getCharset();
					text    = new String(content(entity), null == charset ? _0.utf8 : charset);

					debug(mime, "<-", orgkey);

				} else if ("text/html".equals(mime)) {

					charset = content_type.getCharset();
					text    = new String(content(entity), null == charset ? _0.utf8 : charset);
					html    = Jsoup.parse(text);

					debug(mime, "<-", orgkey);

				} else if (mime.startsWith("image/") || mime.startsWith("video/") || mime.endsWith("octet-stream") || "application/pdf".equals(mime) || "jpg".equals(mime)) {

					Path tmp = Files.createTempFile(null, null);
					try (InputStream in = entity.getContent(); OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp.toFile()))) {

						MessageDigest md = MessageDigest.getInstance("SHA-256");

						byte[] buffer = new byte[1 << 16];
						int size_ = -1;

						while (-1 < (size_ = in.read(buffer))) {
							md.update(buffer, 0, size_);
							out.write(buffer, 0, size_);
						}

						_0.flush(out);

						size = size_;
						hash = _0.hex(md.digest());

					}
					if (null != last_modified) {
						date = date(last_modified);
						Files.setLastModifiedTime(tmp, FileTime.fromMillis(last_modified.getTime()));
					}
					try {
						BufferedImage img = ImageIO.read(tmp.toFile());
						if (null != img) {
							width  = img.getWidth();
							height = img.getHeight();
						}
					} catch (IOException e) {
					}

					Path blob_dir  = Main.blob_dir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4));
					Path blob_file = blob_dir.resolve(hash);
					if (!Files.exists(blob_file)) {

						Files.createDirectories(blob_dir);
						Files.move(tmp, blob_file);

						debug(hash, "<-", orgkey);

if ((null == width && null == height) || (256 <= width.longValue() && 256 <= height.longValue())) {
	Files.createDirectories(blob_vol.resolve("ref.new").resolve(width + "x" + height));
	Files.createLink(blob_vol.resolve("ref.new").resolve(width + "x" + height).resolve(hash), blob_file);
	proc &= Files.list(blob_vol.resolve("ref.new")).toList().size() < 32;
}

					}
					if (!Files.exists(uri_file)) {

						Files.createDirectories(uri_file.getParent());
						Files.createLink(uri_file, blob_file);

						debug(hash, "->", uri_file);

					}

				} else {
					debug(mime, "? ", orgkey);
				}

			} catch (UnknownHostException e) {
				log.warn("{}", e.toString());

			} catch (java.net.SocketTimeoutException e) {
				log.warn("{}", e.toString());

			} catch (SocketException e) {
				log.warn("{}", e.toString());

			} catch (SSLException e) {
				log.warn("{}", e.toString());

			} catch (ClientProtocolException e) {
				log.warn("{}", e.toString());

			} catch (UnsupportedCharsetException e) {
				log.trace("{} {}", key, e.toString());
			}

			Map<String, Long> count_char = null;
			Map<String, Long> count_word = null;
			Map<String, Long> count_link = null;
			if (null != text) {
				count_char = _0.count(text);
				count_word = _0.count(text, token);
			}
			if (null != html) {

				List<String> links = links(key, html, charset);

				count_link = _0.count(links);

				links.parallelStream().forEach(kvs::set);

			}
if (cli) {
	Comparator<Map.Entry<String, Long>> sort = (o1, o2) -> -1 * _0.compare(o1.getValue(), o2.getValue());
	count_char.entrySet().stream().sorted(sort).forEach(e -> System.out.println(e.getValue() + " " + e.getKey()));
	count_word.entrySet().stream().sorted(sort).forEach(e -> System.out.println(e.getValue() + " " + e.getKey()));
	count_link.entrySet().stream().sorted(sort).forEach(e -> System.out.println(e.getValue() + " " + e.getKey()));
}
			// https?://.*
			{

				Map<String, Object> map = new HashMap<>();
				_0.set(map, "res/code",        code);
				_0.set(map, "res/mime",        mime);
				_0.set(map, "data/date",       date);
				_0.set(map, "data/size",       size);
				_0.set(map, "data/hash",       hash);
				_0.set(map, "data/width",      width);
				_0.set(map, "data/height",     height);

				// TODO: ゴミ掃除
				_0.set(map, "data/count", JSONObject.NULL);
				_0.set(map, "data/wc",    JSONObject.NULL);
				_0.set(map, "wc",         JSONObject.NULL);
				_0.set(map, "attr",       JSONObject.NULL);
				_0.set(map, "attrs",      JSONObject.NULL);

				kvs.set(orgkey, map);

			}

//			// blob://*
//			if (null != hash_prev) {
//
//				String blob_key = "blob://" + hash_prev;
//
//				Map<String, Object> map = new HashMap<>();
//				_0.set(map, "attrs/date", date_prev);
//				_0.set(map, "attrs/size", size_prev);
//				_0.set(map, "tags",       tags(key));
//
//				kvs.set(blob_key, map);
//
//			}
//
//			log_blob("*", hash_prev, key);

			return null;

		}

		private static byte[] content(HttpEntity entity)
				throws IOException {

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			try (InputStream in = new BufferedInputStream(entity.getContent())) {

				byte[] buf = new byte[4096];
				int size = -1;

				while (-1 < (size = in.read(buf))) {
					out.write(buf, 0, size);
				}

				_0.flush(out);

			}

			return out.toByteArray();

		}

//		private static List<String> tags(final String key) {
//
//			List<String> tags = new LinkedList<>(List.of(key.split("/")));
//			tags.remove(0);
//
//			// TODO: host -> namespace
//
//			return tags.parallelStream()
//					.map(_0::normalize)
//					.flatMap(e -> _0.brackets(e, true).stream())
//					.map(e -> e.replaceAll("_+", "_"))
//					.map(e -> e.replaceAll(Regex.spaces, " "))
////					.map(e -> e.replaceAll("^[_ ]+|[_ ]+$", ""))
//					.filter(e -> !e.equals(""))
//					.filter(e -> !e.equals("home"))
//					.filter(e -> !e.equals(_0.username))
//					.filter(e -> !e.equals(_0.hex(_0.openpgp4fpr)))
//					.filter(e -> !e.matches("^[0-9_\\-]+$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{8}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{32}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{40}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{64}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{128}$"))
//					.filter(e -> !e.matches("^[0-9a-fA-F]{8}([_\\-][0-9a-fA-F]{4}){3}[_\\-][0-9a-fA-F]{12}$"))
//					.sequential()
//					.distinct()
//					.toList();
//
//		}

		private static List<String> links(final URI base_url, final Document doc, final Charset charset) {

			Set<String> links = new HashSet<>();
			doc.select("a[href]"    ).stream().map(e -> e.attr("href"  )).forEach(links::add);
			doc.select("link[href]" ).stream().map(e -> e.attr("href"  )).forEach(links::add);
			doc.select("iframe[src]").stream().map(e -> e.attr("src"   )).forEach(links::add);
			doc.select("img[src]"   ).stream().map(e -> e.attr("src"   )).forEach(links::add);
//			doc.select("img[srcset]").stream().map(e -> e.attr("srcset")).flatMap(e -> Arrays.asList(e.split(_0.regex_spaces)).stream()).forEach(links::add);
			doc.select("video[src]" ).stream().map(e -> e.attr("src"   )).forEach(links::add);
			doc.select("script[src]").stream().map(e -> e.attr("src"   )).forEach(links::add);

			return links.parallelStream()
					.map(_0::trim)
//					.filter(e -> !e.startsWith("javascript:"))
					.filter(e -> !e.startsWith("data:image/"))
					.map(e -> e.replaceAll("[\\r\\n\\t]+", ""))
					// TODO: uri resolve
					.map(e -> e.replaceAll(" ",   "%20"))
					.map(e -> e.replaceAll("　",  "%E3%80%80"))
					.map(e -> e.replaceAll("\\|", "%7C"))
					.map(e -> e.replaceAll("\\{", "%7B"))
					.map(e -> e.replaceAll("\\}", "%7D"))
					.map(e -> {
						String ret = null;
						try {
							ret = (e.matches("^https?://.*$") ? new URI(e) : base_url.resolve(e)).normalize().toString();
						} catch (URISyntaxException ex) {
							log.warn("links {}", ex.getMessage());
							ret = e;
						} catch (IllegalArgumentException ex) {
							log.warn("links {}", ex.getMessage());
							ret = e;
						}
						return ret;
					})
					.map(e -> {
						String ret = null;
						try {
							ret = URLDecoder.decode(e, (Charset)_0.nvl(charset, _0.utf8));
						} catch (IllegalArgumentException ex) {
							log.warn("links {}", ex.getMessage());
							ret = e;
						}
						return ret;
					})
					.map(normalize_key.andThen(UserImpl.normalize_key))
					.filter(Objects::nonNull)
//					.sequential()
//					.distinct()
					.sorted()
					.toList();

		}

	}

	private boolean is_vol(final Path file)
			throws IOException {
		return file_store.equals(Files.getFileStore(file));
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

	private static String toString(final Path path) {
		return ("/" + path.toAbsolutePath().normalize())
				.replace('\\', '/')
				.replaceAll("/+", "/");
	}

	private static void debug(final String s64, final String sign2, final Object s) {
		log.debug("{} {} {}", StringUtils.rightPad(_0.nvl(s64, ""), 64, ' '), sign2, s);
	}

	private static void info(final String ymdhms, final String hash, final Long size, final Long width, final Long height, final String mime, final Number code, final Object msg) {

		StringBuilder info = new StringBuilder();
		info.append(StringUtils.rightPad(_0.nvl(ymdhms, ""), 23) + " ");
		info.append(StringUtils.rightPad(_0.nvl(hash,   ""), 64) + " ");
		info.append(StringUtils.leftPad( _0.nvl(size,   ""),  8) + " ");
		info.append(StringUtils.leftPad( _0.nvl(width,  ""),  8) + " ");
		info.append(StringUtils.leftPad( _0.nvl(height, ""),  8) + " ");
		info.append(StringUtils.rightPad(_0.nvl(mime,   ""), 16) + " ");
		info.append(StringUtils.rightPad(_0.nvl(code,   ""),  3) + " ");
		info.append(msg);

		System.out.println(info);

	}

}
