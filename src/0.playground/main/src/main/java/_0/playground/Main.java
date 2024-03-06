package _0.playground;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
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
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
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
import java.util.function.Consumer;
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
import _0.playground.core._0;
import _0.playground.debug.Debug;
import _0.playground.debug.StopWatch;
import _0.sshd.Sshd;

public final class Main implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final SimpleDateFormat format_last_modified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

	private static boolean exit = false;

	private static final Path seed_file   = UserConfig.run_dir.resolve("seed.txt");
	private static final Path token_file  = UserConfig.run_dir.resolve("token.txt");
	private static final Path cookie_dir  = UserConfig.run_dir.resolve("cookie");
	private static final Path cookie_file = cookie_dir.resolve("dat");
	private static final Path kvs_file    = UserConfig.run_dir.resolve("sqlite.db");

	private Set<String> token = new HashSet<>();

	private FileStore   blobfs       = null;
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

		blobfs   = Files.getFileStore(UserConfig.blob_dir);
		http_context = HttpClientContext.create();

		worker_task = Executors.newFixedThreadPool(8);
		worker_file = Executors.newFixedThreadPool(1);
		worker_net  = Executors.newFixedThreadPool(UserConfig.tcp_sock);
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

		BasicCookieStore cookies = _0.read(cookie_file);
		if (null == cookies) {
			cookies = new BasicCookieStore();
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
					Path path = null == line2 ? UserConfig.ref_dir : Path.of(line2);
					task = () -> _walk(path);
				}
				if (null == task) {
					// TODO: null val
					kvs.set(line);
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
		task(() -> _walk(UserConfig.ref_dir));

		for (Path i1 : Files.list(UserConfig.ref_uri_dir).toList()) {
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

				if (!UserConfig.kvs_filter_path.test(dir)) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
					throws IOException {

				Thread.currentThread().setName("task/walk" + Main.toString(file));

				kvs.set(file, attrs);

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

			List<Entry> entries = kvs.get_rand(proc_block_size);
			while (!_0.empty(entries)) {

				Entry entry = entries.remove(0);
				String key = entry.getKey();
				String val = entry.getValue();

				String rep = normalize_key.andThen(UserConfig.normalize_key).apply(key);
				if (0 != _0.compare(key, rep)) {
					kvs.del(key);
					if (null == rep) {
						continue;
					}
					key = rep;
					kvs.set(key, val);
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

		// TODO: key type handling
		List<KvsEntryProc<?>> procs = new LinkedList<>();
		procs.add(new KvsEntryProc_openpgp4fpr());
		procs.add(new KvsEntryProc_blob());
		procs.add(new KvsEntryProc_file());
		procs.add(new KvsEntryProc_http());

		while (!_0.empty(procs)) {

			@SuppressWarnings("unchecked")
			KvsEntryProc<T> proc = (KvsEntryProc<T>)procs.remove(0);

			T target = proc.type(key);
			if (null == target) {
				continue;
			}

			futures.add(proc.worker().submit(() -> {

				Thread.currentThread().setName("task/kvs/proc/" + proc.getClass().getName().replaceAll(".*_", ""));

				return proc._call(key, target, _0.nvl(Kvs.json(val), new HashMap<>()), cli);

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

			Number prev_size   = _0.get(val, "meta/size");
			String prev_ymdhms = _0.get(val, "meta/date");
			String prev_hash   = _0.get(val, "meta/hash");

			if (!UserConfig.kvs_filter_path.test(key) || Files.isDirectory(key)) {
				kvs.del(orgkey);
				debug(prev_hash, "- ", orgkey);
				return null;
			}

			// hash計算
			boolean upd  = false;
			String  hash = null;
			{

				BasicFileAttributes attrs = Files.readAttributes(key, BasicFileAttributes.class);
				long   size   = attrs.size();
				long   date   = _0.max(attrs.creationTime().toMillis(), attrs.lastModifiedTime().toMillis());
				String ymdhms = Kvs.date(date);

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

			boolean is_blobfs = is_blobfs(key);

			Path blob_dir  = UserConfig.blob_dir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4));
			Path blob_file = blob_dir.resolve(hash);

			// blob
			if (!Files.exists(blob_file)) {

				// copy
				if (!is_blobfs) {

					String name_l = key.getFileName().toString().toLowerCase();

					// TODO: 残ストレージ

					boolean copy = false;
					for (String ext : UserConfig.copy_blob_exts) {
						copy |= name_l.endsWith("." + ext);
					}

					if (copy) {

						Files.createDirectories(blob_dir);
						Files.copy(key, blob_file, StandardCopyOption.COPY_ATTRIBUTES);

						debug(hash, "<-", orgkey);

						Path copy_dir  = UserConfig.ref_dir.getParent().resolve("ref.copy");
						Path copy_file = copy_dir.resolve(hash + "_" + key.getFileName().toString().replace(hash, ""));
						Files.createDirectories(copy_dir);
						Files.createLink(copy_file, blob_file);

					}

				// link
				} else {

					Files.createDirectories(blob_dir);
					Files.createLink(blob_file, key);

					debug(hash, "<-", orgkey);

				}

			}

			// ref link
			if (Files.exists(blob_file) && is_blobfs && key.startsWith(UserConfig.ref_dir) && _0.ino(key) != _0.ino(blob_file)) {

				long ms1 = Files.getLastModifiedTime(key).toMillis();
				long ms2 = Files.getLastModifiedTime(blob_file).toMillis();
				Files.setLastModifiedTime(blob_file, FileTime.fromMillis(Math.min(ms1, ms2)));

				Files.delete(key);
				Files.createLink(key, blob_file);

				debug(hash, "->", orgkey);

			}

			return null;

		}

		private boolean is_blobfs(final Path file)
				throws IOException {
			return blobfs.equals(Files.getFileStore(file));
		}

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

			// TODO: debug
			if (null == key.getHost()) {
				log.warn("{}", key);
				return null;
			}

			if (!cli) {

				String prev_hash = _0.get(val, "data/hash");
				if (null != prev_hash) {
					return null;
				}

				boolean target = false;
				target |= key.getHost().endsWith("." + UserConfig.tld);
				target |= UserConfig.proc_filter_http.test(key);
				if (!target) {
					debug(null, " #", orgkey);
					return null;
				}

			}

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

			Path    tmp     = null;
			String  hash    = null;
			String  date    = null;
			Number  size    = null;
			Number  code    = null;
			String  mime    = null;
			Charset charset = null;

			try (CloseableHttpClient client = HttpClientBuilder.create().build(); CloseableHttpResponse res = client.execute(req, http_context)) {

				// TODO: redirect
				code = res.getCode();

				HttpEntity entity = res.getEntity();

				ContentType content_type = ContentType.parse(entity.getContentType());
				if (null != content_type) {
					charset = content_type.getCharset();
					mime    = content_type.getMimeType().toLowerCase();
				}

				Date last_modified = date(res);

				tmp = Files.createTempFile(null, null);
				try (InputStream in = entity.getContent(); OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp.toFile()))) {

					MessageDigest md = MessageDigest.getInstance("SHA-256");

					byte[] buffer = new byte[1 << 16];
					int  read  = -1;
					long size_ = 0;

					while (-1 < (read = in.read(buffer))) {
						md.update(buffer, 0, read);
						out.write(buffer, 0, read);
						size_ += read;
					}

					_0.flush(out);

					size = size_;
					hash = _0.hex(md.digest());

				}
				if (null != last_modified) {
					date = Kvs.date(last_modified);
					Files.setLastModifiedTime(tmp, FileTime.fromMillis(last_modified.getTime()));
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

			Number width  = null;
			Number height = null;
			if (null != tmp && Files.exists(tmp)) {

				// text
				if (mime.startsWith("text/")) {
					try {

						Charset charset_ = _0.nvl(charset, _0.utf8);

						Document html = Jsoup.parse(tmp.toFile(), charset_.name());

						Consumer<String> println = cli ? e -> System.out.println(e) : e -> {};

						Map<String, Object> val_ = new HashMap<>();
						_0.set(val_, "parent", List.of(orgkey));

						links(key, html, charset_).stream()
								.sorted((o1, o2) -> _0.compare(o1, o2))
								.peek(println)
								.forEach(e -> kvs.set(e, val_));

						debug("text", "<-", orgkey);

					} catch (IOException e) {
					}
				}

				// blob
				if (mime.startsWith("image/") || mime.startsWith("video/") || mime.endsWith("octet-stream") || "application/pdf".equals(mime) || "jpg".equals(mime)) {

					try {
						BufferedImage img = ImageIO.read(tmp.toFile());
						if (null != img) {
							width  = img.getWidth();
							height = img.getHeight();
						}
					} catch (IOException e) {
					}

					Path blob_dir  = UserConfig.blob_dir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4));
					Path blob_file = blob_dir.resolve(hash);

					if (!Files.exists(blob_file)) {

						Files.createDirectories(blob_dir);
						Files.move(tmp, blob_file);

						debug(hash, "<-", orgkey);

if ((null == width && null == height) || (256 <= width.longValue() && 256 <= height.longValue())) {

	Path new_dir  = UserConfig.ref_dir.getParent().resolve("ref.new").resolve(width + "x" + height);
	Path new_file = new_dir.resolve(hash);

	Files.createDirectories(new_dir);
	Files.createLink(new_file, blob_file);
	proc &= Files.list(new_dir.getParent()).toList().size() < 32;

}

					}

					Path uri_file = UserConfig.ref_uri_dir.resolve(key.getScheme()).resolve(_0.reverse(".", key.getHost())).resolve(key.getPath().replaceAll("^/", ""));
					Path uri_dir  = uri_file.getParent();

					if (!Files.exists(uri_file)) {

						Files.createDirectories(uri_dir);
						Files.createLink(uri_file, blob_file);

						debug(hash, "->", "file://" + Main.toString(uri_file));

					}

				}

				try {
					Files.delete(tmp);
				} catch (IOException e) {
				}

			}

			// https?://.*
			{

				Map<String, Object> val_ = new HashMap<>();
				_0.set(val_, "res/code",    code);
				_0.set(val_, "res/mime",    mime);
				_0.set(val_, "data/date",   date);
				_0.set(val_, "data/size",   size);
				_0.set(val_, "data/hash",   hash);

				if (null != width && null != height) {
					_0.set(val_, "data/width",  width);
					_0.set(val_, "data/height", height);
				}

				_0.set(val_, "data/count",  JSONObject.NULL);
				_0.set(val_, "data/wc",     JSONObject.NULL);
				_0.set(val_, "wc",          JSONObject.NULL);
				_0.set(val_, "attr",        JSONObject.NULL);
				_0.set(val_, "attrs",       JSONObject.NULL);

				kvs.set(orgkey, val_);

			}

			return null;

		}

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
					.map(normalize_key.andThen(UserConfig.normalize_key))
					.filter(Objects::nonNull)
					.distinct()
					.toList();

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

}
