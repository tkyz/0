package _0.playground;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileStore;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0.kvs.Entry;
import _0.kvs.Kvs;
import _0.playground.core._0;
import _0.playground.debug.Debug;
import _0.playground.debug.StopWatch;

public final class Main implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final Path base_dir = _0.userhome;
	private static final Path blob_dir = base_dir.resolve("blob");
	private static final Path ref_dir  = base_dir.resolve("ref");

	private static final String blob_regex = blob_dir.resolve("sha256") + "(/[0-9a-f]{2})*/(?<hash>[0-9a-f]{64})(\\.(?<ext>[^/\\.]*))?";

	private static final int         proc_queue_size = 0x1000;
	private static final List<Entry> proc_queue      = Collections.synchronizedList(new LinkedList<>());

	private static final Function<String, Boolean> find = str -> {
		return false;
	};

	public boolean proc = true;
	public boolean exit = false;

	private Kvs kvs = null;

	public ExecutorService worker  = null;
	public List<Future<?>> futures = null;

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
		task(() -> _proc_add());
		task(() -> _proc_run());
		task(() -> _flush());

		wait_futures();
		exit = true;

	}

	private void init()
			throws ReflectiveOperationException, IOException, SQLException {

		kvs = new Kvs(_0.userhome.resolve("sqlite.db"));

		worker  = Executors.newFixedThreadPool(8);
		futures = Collections.synchronizedList(new LinkedList<>());

	}

	private void wait_futures() {

		while (!_0.empty(futures)) {

			Future<?> future = futures.remove(0);

			if (!future.isDone()) {
				futures.add(future);
				_0.yield();
				continue;
			}
			if (future.isCancelled()) {
				_0.yield();
				continue;
			}

			try {

				@SuppressWarnings("unused")
				Object ret = future.get();

			} catch (ExecutionException | InterruptedException e) {
				log.warn("", e);
				worker.shutdownNow();
			}

		}

	}

	@Override
	public void close() {

		proc = false;
		exit = true;

		worker.shutdown();

	}

	private Void _cli(final InputStream in)
			throws IOException {

		boolean system = System.in == in;

		Thread.currentThread().setName("task/cli");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			while (!exit) {

				String line = _0.trim(reader.readLine());

				if (_0.empty(line)) {
					proc = !proc;
					continue;
				}
				if (system && "exit".equalsIgnoreCase(line)) {
					proc = false;
					exit = true;
					continue;
				}
				if ("?".equalsIgnoreCase(line)) {
					Debug.println(false);
					continue;
				}
				if ("??".equalsIgnoreCase(line)) {
					Debug.println(true);
					continue;
				}

				Callable<Void> task = null;
				if ("size".equalsIgnoreCase(line)) {
					task = () -> _size();
				}
				if ("vacuum".equalsIgnoreCase(line)) {
					task = () -> _vacuum();
				}
				if ("walk".equalsIgnoreCase(line)) {
					task = () -> _walk();
				}
				if (null == task) {
					continue;
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

	private Void _walk()
			throws IOException {

		List<Path> list = new LinkedList<>();
		list.add(ref_dir);
		list.add(blob_dir);

		while (!_0.empty(list)) {
			_walk(list.remove(0));
		}

		return null;

	}

	private Void _walk(final Path path)
			throws IOException {

		Files.walkFileTree(path.toAbsolutePath().normalize(), new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
					throws IOException {

				Thread.currentThread().setName("task/walk" + Main.toString(dir) + "/");

				if (exit) {
					return FileVisitResult.TERMINATE;
				}
				if (dir.startsWith(blob_dir) && dir.toString().matches(blob_dir.resolve("sha256") + "(/[0-9a-f]{2})*/(?<hash>[0-9a-f]{64})\\.tmp")) {
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
				if (file.startsWith(blob_dir) && file.toString().matches(blob_regex) && !file.getFileName().toString().toLowerCase().endsWith(".torrent")) {
					return FileVisitResult.CONTINUE;
				}

				Entry entry = Entry.of("file://" + Main.toString(file));
				kvs.add(entry);

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult visitFileFailed(final Path file, final IOException e)
					throws IOException {

				Thread.currentThread().setName("task/walk" + Main.toString(file));

				if (null != e) {
					log.warn("{}", e.getMessage());
				}

				return FileVisitResult.CONTINUE;

			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
					throws IOException {

				Thread.currentThread().setName("task/walk" + Main.toString(dir) + "/");

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

	private Void _proc_add()
			throws Exception {

		Thread.currentThread().setName("task/kvs/proc/add");
		while (!exit) {

			if (!proc) {
				proc_queue.clear();
				_0.yield();
				continue;
			}

			int size = proc_queue_size - proc_queue.size();
			if (size <= 0) {
				_0.yield();
				continue;
			}

			List<Entry> entries = kvs.rand(proc_queue_size);
			proc_queue.addAll(entries);

		}

		return null;

	}

	private Void _proc_run()
			throws Exception {

//		SimpleDateFormat sw = new SimpleDateFormat("HH:mm:ss.SSS");
//		sw.setTimeZone(TimeZone.getTimeZone("GMT"));

		long proc_cnt  = 0;
//		long time_prev = System.currentTimeMillis();

		Thread.currentThread().setName("task/kvs/proc/run");
		while (!exit) {

			Entry entry = null;
			try {
				entry = proc_queue.remove(0);
			} catch (IndexOutOfBoundsException e) {
			}
			if (null == entry) {
				_0.yield();
				continue;
			}

			String scheme = null;
			String remain = null;
			{

				String key = entry.key();

				int idx = key.indexOf("://");
				if (-1 == idx) {
					_0.yield();
					continue;
				}

				scheme = key.substring(0, idx);
				remain = key.substring(idx + 3);

			}

			if ("file".equals(scheme)) {
				_file(entry, Path.of(remain));

			} else {
				log.warn("{}", scheme);
			}

			proc_cnt++;
			proc_cnt %= proc_queue_size;
			if (0 == proc_cnt) {
//				long   time_now  = System.currentTimeMillis();
//				long   time_diff = time_now - time_prev;
//				double ln        = Math.log(time_diff);
//				log.trace("{} {}", sw.format(new Date(time_diff)), ln);
				log.trace("--------------------------------------------------------------------------------------------------------------------------------");
//				time_prev = time_now;
			}

		}

		return null;

	}

	private Void _file(final Entry entry, final Path file)
			throws Exception {

		if (!Files.exists(file)) {
			return null;
		}
		if (Files.isDirectory(file)) {
			return null;
		}
		if (Files.isSymbolicLink(file)) {
			return null;
		}

		Map<String, Object> state = new HashMap<>();
		_0.set(state, "upd", false);

		boolean is_blob = file.toString().matches(blob_regex);

		Callable<Void> meta = () -> {

			Number size      = entry.val("meta/size");
			Number size_     = Files.size(file);
			String mime_type = entry.val("meta/mime_type");
			String sha256    = entry.val("meta/sha256");

			boolean rehash = false;
			rehash |= 0 != _0.compare(size, size_);
			rehash |= null == mime_type;
			rehash |= null == sha256;
			if (!rehash) {
				return null;
			}

			mime_type = _0.mime_type(file);
			sha256    = is_blob ? file.toString().replaceAll(blob_regex, "${hash}") : _0.hex(_0.sha256(file));

			entry.val("meta/size",      size_);
			entry.val("meta/mime_type", mime_type);
			entry.val("meta/sha256",    sha256);
			_0.set(state, "upd", true);

			return null;

		};

		Callable<Void> attr = () -> {

			if (!is_blob) {
				return null;
			}

			String mime_type = entry.val("meta/mime_type");
			String name      = entry.val("attr/name");

			if ("application/x-bittorrent".equals(mime_type) && null == name) {

				Map<String, Object> bencode = _0.bencode(Files.readAllBytes(file));

				name = _0.get(bencode, "info/name");

				entry.val("attr/name", name);
				_0.set(state, "upd", true);

			}

			return null;

		};

		Callable<Void> upd = () -> {

			if (!(boolean)_0.get(state, "upd")) {
				return null;
			}

			kvs.add(entry);

			return null;

		};

		Callable<Void> link = () -> {

			String mime_type = entry.val("meta/mime_type");
			String sha256    = entry.val("meta/sha256");

			boolean is_link = false;
			is_link |= mime_type.startsWith("image/");
			is_link |= mime_type.startsWith("audio/");
			is_link |= mime_type.startsWith("video/");
			is_link |= "application/pdf".equals(mime_type);
			is_link |= "application/x-bittorrent".equals(mime_type);
			is_link |= "application/x-shockwave-flash".equals(mime_type);
			is_link |= "application/octet-stream".equals(mime_type);

			// TODO: ext <- mime_type
			// a254607ce38dd2f8965e621d59792b220c2fa1d140c9e1cb7d89bb06027d0dac
			String ext = _0.ext(file);
			ext = "jpeg".equals(ext) ? "jpg" : ext;
			ext = "mpeg".equals(ext) ? "mpg" : ext;

			Path blob_dir  = Main.blob_dir.resolve("sha256").resolve(sha256.substring(0, 2)).resolve(sha256.substring(2, 4));
			Path blob_file = blob_dir.resolve(sha256 + (null == ext ? "" : ("." + ext)));

			if (!is_link && !Files.exists(blob_file)) {
				_0.set(state, "lnk", "#");

			} else if (!is_link && Files.exists(blob_file)) {
				Files.delete(blob_file);
				_0.set(state, "lnk", "-");

			} else if (is_link && !Files.exists(blob_file)) {

				Files.createDirectories(blob_dir);

				FileStore store1 = Files.getFileStore(file);
				FileStore store2 = Files.getFileStore(blob_dir);

				if (store1.equals(store2)) {
					Files.createLink(blob_file, file);
				} else {
					Files.copy(file, blob_file);
					Files.setLastModifiedTime(blob_file, Files.getLastModifiedTime(file));
				}

				_0.set(state, "lnk", "<");

			} else if (is_link && Files.exists(blob_file)) {

				FileStore store1 = Files.getFileStore(file);
				FileStore store2 = Files.getFileStore(blob_file);

				BasicFileAttributes attr1 = Files.readAttributes(file,      BasicFileAttributes.class);
				BasicFileAttributes attr2 = Files.readAttributes(blob_file, BasicFileAttributes.class);

				if (!store1.equals(store2) && attr1.size() == attr2.size()) {
					_0.set(state, "lnk", "=");

				} else if (store1.equals(store2) && attr1.fileKey().equals(attr2.fileKey())) {
					_0.set(state, "lnk", " ");

				} else if (store1.equals(store2) && !attr1.fileKey().equals(attr2.fileKey()) && attr1.size() == attr2.size()) {
					Files.delete(file);
					Files.createLink(file, blob_file);
					_0.set(state, "lnk", ">");

				} else {
					_0.set(state, "lnk", "!");
				}

			}

			return null;

		};

		Callable<Void> log = () -> {

			String mime_type = entry.val("meta/mime_type");
			String sha256    = entry.val("meta/sha256");
			String name      = entry.val("attr/name");

			boolean skip = false;
//			skip |= "#".equals(lnk_state);
			skip |= "inode/x-empty".equals(mime_type);
			skip |= "text/plain".equals(mime_type);
			skip |= "text/html".equals(mime_type);
			skip |= "text/x-asm".equals(mime_type);
			skip |= "font/sfnt".equals(mime_type);
			skip |= "message/rfc822".equals(mime_type);
			skip |= "application/gzip".equals(mime_type);
			skip |= "application/json".equals(mime_type);
			skip |= "application/javascript".equals(mime_type);
			skip |= "application/x-mswinurl".equals(mime_type);
			skip |= "application/x-wine-extension-ini".equals(mime_type);
			if (skip) {
				return null;
			}

			// upd [ *]
			// lnk [ =-<>#!]
			// tmp [ .*_!]
			String upd_state = ((boolean)_0.get(state, "upd")) ? "*" : " ";
			String lnk_state = _0.get(state, "lnk");
			String tmp_state = " ";
			if (is_blob) {

				Path cmp_dir = Main.blob_dir.resolve("sha256").resolve(sha256.substring(0, 2)).resolve(sha256.substring(2, 4)).resolve(sha256);
				Path tmp_dir = Main.blob_dir.resolve("sha256").resolve(sha256.substring(0, 2)).resolve(sha256.substring(2, 4)).resolve(sha256 + ".tmp");

				if (Files.isDirectory(cmp_dir) && Files.isDirectory(tmp_dir)) {
					tmp_state = "!";

				} else if (Files.isDirectory(cmp_dir)) {
					tmp_state = ".";

				} else if (Files.isDirectory(tmp_dir) && 0 < Files.list(tmp_dir).count()) {
					tmp_state = "*";

				} else if (Files.isDirectory(tmp_dir) && 0 == Files.list(tmp_dir).count()) {
					tmp_state = "_";
				}

			}

			int nlink = (int)Files.getAttribute(file, "unix:nlink");

			name = is_blob ? _0.normalize(name) : file.toString().replaceFirst("^" + blob_regex, "/${hash}");

			boolean trace = false;
			trace |= !" ".equals(upd_state);
			trace |= !" ".equals(lnk_state);
//			trace |= !" ".equals(tmp_state);
			trace |= !is_blob && 1 == nlink;
			trace |= find.apply(name);
			if (!trace) {
				return null;
			}

			Main.log.trace("{}", String.format("%-64s %-32s [%s%s%s] %2d %s", sha256, mime_type, tmp_state, lnk_state, upd_state, nlink, name));

			return null;

		};

		meta.call();
		attr.call();
		upd.call();
		link.call();
		log.call();

		return null;

	}

	private void task(final Callable<Void> task) {
		futures.add(worker.submit(task));
	}

	public static String toString(final Path path) {
		return ("/" + path.toAbsolutePath().normalize())
				.replace('\\', '/')
				.replaceAll("/+", "/");
	}

}
