package _0.bigset;

import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.math3.complex.Complex;
import org.json.JSONException;
import org.json.JSONObject;

import _0.bigset.BigSet.Entry;
import _0.core.Jdbc;
import _0.core._0;
import _0.core._0.hash.HashCollisionException;

public final class BigSet implements Set<Entry>, Flushable {

	public  static final Path       root_dir     = _0.user.home.resolve(".0");
	public  static final FileSystem root_fs      = root_dir.getFileSystem();
	public  static final Path       store_file   = _0.user.home.resolve("sqlite.db");
//	public  static final Path       store_file   = root_dir.resolve("sqlite.db");
	public  static final Path       obj_dir      = root_dir.resolve("obj");
	public  static final Path       ref_dir      = root_dir.resolve("ref");
	public  static final Path       ref_head_dir = root_dir.resolve("ref/head");
	public  static final Path       tag_dir      = root_dir.resolve("tag");
	public  static final Path       tmp_dir      = root_dir.resolve("tmp");
	private static final int        dir_depth    = 2;
	private static final long       timeout      = 5 * 60 * 1000;

	private static BigSet instance = new BigSet();

	private Connection   con       = null;
	private int          bulkmax   = -1;
	private List<Entry>  queue_upd = Collections.synchronizedList(new LinkedList<>());
	private List<Object> queue_del = Collections.synchronizedList(new LinkedList<>());

	public static BigSet of() {
		return instance;
	}

	private BigSet() {

		try {

			Files.createDirectories(obj_dir);
			Files.createDirectories(ref_dir);
			Files.createDirectories(ref_head_dir);
			Files.createDirectories(tag_dir);
			Files.createDirectories(tmp_dir);

			con     = new Jdbc("sqlite").path(store_file).connect();
			bulkmax = Math.min(Jdbc.bulksize(con, 2), 0x2000); // SQLITE_TOOBIG

			try (Statement stmt = con.createStatement()) {
				stmt.execute("PRAGMA busy_timeout = " + timeout);
				stmt.execute("""
CREATE TABLE IF NOT EXISTS kvs (

   key TEXT NOT NULL
  ,val JSON

  ,PRIMARY KEY (key)

)
""");
			}

			org.sqlite.Function.create(con, "udf_json_merge", new org.sqlite.Function() {
				@Override
				protected final void xFunc()
						throws SQLException {

					try {

						String s1 = value_text(0);
						String s2 = value_text(1);

						Map<String, Object> json1  = null == s1 ? new HashMap<>() : new JSONObject(s1).toMap();
						Map<String, Object> json2  = null == s2 ? new HashMap<>() : new JSONObject(s2).toMap();
						Map<String, Object> merged = _0.merge(json1, json2);

//						// TODO: null -> JSONObject.NULL

						result(new JSONObject(merged).toString());

					} catch (JSONException e) {
						throw new BigSetException(e);
					}

				}
			});

		} catch (IOException | SQLException e) {
			throw new BigSetException(e);
		}

		Consumer<Runnable> daemon = impl -> {
			Thread thread = new Thread(impl);
			thread.setDaemon(true);
			thread.start();
		};

		daemon.accept(() -> _flush());

	}

	private final void _flush() {

		Thread.currentThread().setName(getClass().getName() + "/" + _0.concurrent.methodName());

		try {
			while (true) {

				Thread.yield();

				long cnt = 0;
				cnt += upd();
				cnt += del();
				if (0 == cnt) {
					Thread.sleep(1);
				}

			}
		} catch (SQLException e) {
			throw new BigSetException(e);

		} catch (InterruptedException e) {
			// pass
		}

	}

	@Override
	public final int size() {

		int size = 0;

		synchronized (con) {
//			try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT max(rowid) AS size FROM kvs")) {
			try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT count(1)   AS size FROM kvs")) {

				if (rs.next()) {
					size = Math.toIntExact(rs.getLong("size"));
				}

			} catch (SQLException e) {
				throw new BigSetException(e);
			}
		}

		return size;

	}

	@Override
	public final boolean isEmpty() {
		return 0 == size();
	}

	@Override
	public final boolean add(final Entry entry) {
		queue_upd.add(entry);
		return false;
	}

	@Override
	public final boolean addAll(final Collection<? extends Entry> entries) {
		queue_upd.addAll(entries);
		return false;
	}

	public final void remove(final Entry entry) {
		queue_del.add(entry.getKey());
	}

	public final Entry get(final String key) {

		Entry entry = null;

		synchronized (con) {
			try (PreparedStatement stmt = con.prepareStatement("SELECT key, val FROM kvs WHERE key = ?")) {

				stmt.setFetchSize(1);
				Jdbc.bind(stmt, key);

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						entry = new Entry(rs);
					}
				}

			} catch (SQLException e) {
				throw new BigSetException(e);
			}
		}

		return entry;

	}

	@Override
	public final void flush() {
		try {
			upd();
			del();
		} catch (SQLException e) {
			throw new BigSetException(e);
		}
	}

	public static final List<Entry> rand(final int block) {

		List<Entry> list = new LinkedList<>();

		String query = """
WITH random_id(rid) AS (
          SELECT abs(random() % (SELECT max(rowid) FROM kvs)) + 1
UNION ALL SELECT abs(random() % (SELECT max(rowid) FROM kvs)) + 1 FROM random_id
)
SELECT key, val FROM kvs
  INNER JOIN
    (SELECT DISTINCT rid FROM random_id LIMIT ?)
      ON kvs.rowid == rid
""";

		synchronized (of().con) {
			try (PreparedStatement stmt = of().con.prepareStatement(query)) {

				stmt.setFetchSize(block);
				Jdbc.bind(stmt, block);

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						list.add(new Entry(rs));
					}
				}

			} catch (SQLException e) {
				throw new BigSetException(e);
			}
		}

		return list;

	}

	private final int upd()
			throws SQLException {

		int rowcnt = Math.min(queue_upd.size(), bulkmax);
		if (0 < rowcnt) {

			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO kvs VALUES ");
			for (int i = 0; i < rowcnt; i++) {
				query.append(0 == i ? "" : ",");
				query.append("(?, ?)");
			}
			query.append("  ON CONFLICT (key) DO UPDATE SET ");
			query.append("    val = udf_json_merge(val, excluded.val) ");

			synchronized (con) {
				try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

					for (int i = 0; i < rowcnt; i++) {

						Entry entry = queue_upd.remove(0);

						Object              key = entry.getKey();
						Map<String, Object> val = entry.getValue();

						stmt.setObject(i * 2 + 1, key);
						stmt.setObject(i * 2 + 2, null == val ? null : new JSONObject(val).toString());

					}

					stmt.executeUpdate();

				}
			}

		}

		return rowcnt;

	}

	private final int del()
			throws SQLException {

		int rowcnt = Math.min(queue_del.size(), bulkmax);
		if (0 < rowcnt) {

			StringBuilder query = new StringBuilder();
			query.append("DELETE FROM kvs WHERE key IN (");
			for (int i = 0; i < rowcnt; i++) {
				query.append(0 == i ? "" : ",");
				query.append("?");
			}
			query.append(")");

			synchronized (con) {
				try (PreparedStatement stmt = con.prepareStatement(query.toString())) {

					for (int i = 0; i < rowcnt; i++) {
						stmt.setObject(i + 1, queue_del.remove(0));
					}

					stmt.executeUpdate();

				}
			}

		}

		return rowcnt;

	}

	public static class Entry implements Map.Entry<String, Map<String, Object>> {

		private String              key = null;
		private Map<String, Object> val = null;

		private Complex             spin = Complex.ZERO;

		@Deprecated public double          priority   = 1.0d;
		@Deprecated public boolean         exec       = false;
		@Deprecated public boolean         upd        = false;
		@Deprecated public List<Exception> exceptions = Collections.synchronizedList(new LinkedList<>());

		public Entry(final String key) {
			this(key, null);
		}

		public Entry(final String key, final Map<String, Object> val) {
			this.key = key;
			this.val = null == val ? new HashMap<>() : val;
		}

		Entry(final ResultSet rs)
				throws SQLException {

			Object key = rs.getObject("key");
			Object val = rs.getObject("val");

			this.key = (String)key;
			this.val = null == val ? new HashMap<>() : new JSONObject((String)val).toMap();

		}

		@Override
		public final String getKey() {
			return key;
		}

		@Override
		public final Map<String, Object> getValue() {
			return val;
		}

		@Override
		@Deprecated
		public final Map<String, Object> setValue(final Map<String, Object> val) {
			throw new UnsupportedOperationException();
		}

//		@Override
//		public int hashCode() {
//			return getKey().hashCode();
//		}
	//
//		@Override
//		public boolean equals(final Object obj) {
//			return getKey().equals(((Entry)obj).getKey());
//		}

		@Override
		public final String toString() {
			return getKey();
		}

		public final void exec() {

			exec = false;
			if (priority < 0.0d) {
//				map.remove(entry);
			} else if (0.0d == priority) {
				// pass
			} else if (1.0d == priority) {
				exec = true;
			} else if (_0.math.rand() < priority) {
				exec = true;
			}
			if (!exec) {
				return;
			}

			int bef = getValue().hashCode();

			String scheme = scheme();
			if (null != scheme) {
				exec("scheme", scheme);
			}

			Path path = path(true);
			if (null != path && (path.startsWith(ref_head_dir) || path.startsWith(tag_dir))) {
				mkobj(path);
			}

			String mime_type = val("meta/mime_type");
			if (null != mime_type && !mime_type.startsWith("inode/")) {
				exec("mime_type", mime_type);
			}

			int aft = getValue().hashCode();

			upd = bef != aft;
			if (upd) {
				BigSet.of().add(this);
			}

			mkref();
			mkdir();

		}

		private final void exec(final String type, final String val) {

			try {

				String pkgdir = "_0/" + _0.concurrent.methodName() + "/" + type + "/" + val.replaceAll("[\\-\\+\\.]", "_");

				Class<?> clazz    = Class.forName(pkgdir.replace("/", ".") + ".Impl");
				Method   method   = clazz.getMethod("run", Entry.class);
				Object   instance = clazz.getConstructor().newInstance();

				method.invoke(instance, this);

			} catch (ClassNotFoundException | NoSuchMethodException e) {
				// not implemented
			} catch (ReflectiveOperationException e) {
				exceptions.add(e);
			}

		}

		public final <T> T val(final String selector) {
			return _0.get(getValue(), selector);
		}

		public final Path path() {
			return path(false);
		}

		public final Path path(final boolean exists) {

			String key = getKey();
			if (!key.startsWith("file://127.0.0.1/")) {
				return null;
			}

			Path path = Path.of(key.substring("file://127.0.0.1".length()));
			if (exists && !Files.exists(path) && !Files.isSymbolicLink(path)) {
				return null;
			}

			return path;

		}

		public final URI uri() {

			String scheme = scheme();
			if (null == scheme) {
				return null;
			}
			if ("file".equals(scheme)) {
//				return path().toUri();
				return null;
			}

			// TODO: mkuri
			String esc = key;
			esc = esc.replace(" ",  "%20");
//			esc = esc.replace("!",  "%21");
			esc = esc.replace("\"", "%22");
			esc = esc.replace("%",  "%25");
			esc = esc.replace("(",  "%28");
			esc = esc.replace(")",  "%29");
//			esc = esc.replace("+",  "%2B");
//			esc = esc.replace(":",  "%3A");
			esc = esc.replace("[",  "%5B");
//			esc = esc.replace("\\", "%5C");
			esc = esc.replace("]",  "%5D");
			esc = esc.replace("^",  "%5E");
//			esc = esc.replace("-",  "%5F");
//			esc = esc.replace("`",  "%60");
			esc = esc.replace("{",  "%7B");
			esc = esc.replace("|",  "%7C");
			esc = esc.replace("}",  "%7D");
			esc = esc.replace("　", "%E3%80%80");

			URI uri = null;
			try {
				uri = new URI(esc).normalize();
			} catch (URISyntaxException e) {
				exceptions.add(e);
			}

			return uri;

		}

		public final String scheme() {
			return _0.scheme(getKey(), true);
		}

		public final String host() {

			URI uri = uri();
			if (null == uri) {
				return null;
			}

			String host = uri.getHost();
			if (null == host) {
				return null;
			}
			if (-1 == host.indexOf(".")) {
				return null;
			}

			return host.toLowerCase();

		}

		public final String namespace() {

			String host = host();
			if (null == host) {
				return null;
			}

			String ns = _0.reverse(".", host);

			return ns;

		}

		public final boolean media() {

			boolean media = false;

			String mime_type = val("meta/mime_type");
			if (null != mime_type) {
				media |= "application/pdf".equals(mime_type);
				media |= mime_type.startsWith("audio/");
				media |= mime_type.startsWith("image/");
				media |= mime_type.startsWith("video/");
			}

			return media;

		}

		private final Path depth(final Path base_dir) {
			return depth(base_dir, false);
		}

		private final Path depth(final Path base_dir, final boolean exists) {

			String hash = val("meta/sha256");
			if (null == hash) {
				return null;
			}

			Path dir = base_dir;
			for (int i = 0; i < dir_depth; i++) {
				String name = hash.substring(i * 2, (i + 1) * 2);
				dir = dir.resolve(name);
			}

			Path file = dir.resolve(hash);
			if (exists && !Files.exists(file)) {
				file = null;
			}

			return file;

		}

		public final Path obj() {
			return depth(obj_dir, false);
		}

		public final Path obj(final boolean exists) {
			return depth(obj_dir, exists);
		}

		public final Path mkobj(final Path src_file) {

			String hash      = val("meta/sha256");
			String mime_type = val("meta/mime_type");
			if (null == hash || null == mime_type) {
				return null;
			}

			if (mime_type.startsWith("inode/")) {
				return null;
			}

			Path obj_file = obj();
			if (src_file.equals(obj_file)) {
				return obj_file;
			}

			try {

				if (!Files.exists(obj_file)) {

					FileStore store1 = Files.getFileStore(src_file);
					FileStore store2 = Files.getFileStore(obj_dir);

					synchronized (root_fs) {

						Files.createDirectories(obj_file.getParent());

						if (store1.equals(store2)) {
							Files.createLink(obj_file, src_file);

						} else {
							Files.copy(src_file, obj_file);
							Files.setLastModifiedTime(obj_file, Files.getLastModifiedTime(src_file));
						}

					}

				} else {

					FileStore store1 = Files.getFileStore(src_file);
					FileStore store2 = Files.getFileStore(obj_file);

					BasicFileAttributes attr1 = Files.readAttributes(src_file, BasicFileAttributes.class);
					BasicFileAttributes attr2 = Files.readAttributes(obj_file, BasicFileAttributes.class);

					if (store1.equals(store2) && attr1.size() != attr2.size()) {
						throw new HashCollisionException(src_file, obj_file);
					}
					if (store1.equals(store2) && attr1.size() == attr2.size() && !attr1.fileKey().equals(attr2.fileKey())) {
						synchronized (root_fs) {

							byte[] sha512_1 = _0.hash.sha512(src_file);
							byte[] sha512_2 = _0.hash.sha512(obj_file);
							if (0 != _0.compare(sha512_1, sha512_2)) {
								throw new HashCollisionException(src_file, obj_file);
							}

							Files.delete(src_file);
							Files.createLink(src_file, obj_file);

						}
					}

				}

			} catch (HashCollisionException | IOException e) {
				exceptions.add(e);
			}

			return obj_file;

		}

		public final Path headdir() {

			String hash      = val("meta/sha256");
			String mime_type = val("meta/mime_type");
			if (null == hash || null == mime_type) {
				return null;
			}
			if (mime_type.startsWith("inode/")) {
				return null;
			}

			Path dir = ref_head_dir.resolve(mime_type);
			for (int i = 0; i < dir_depth; i++) {
				dir = dir.resolve(hash.substring(i * 2, (i + 1) * 2));
			}
			dir = dir.resolve(hash);

			return dir;

		}

		private final void mkref() {

			String scheme    = scheme();
			String namespace = namespace();
			Number status    = val("meta/status");
			Number size      = val("meta/size");
			String hash      = val("meta/sha256");
			String mime_type = val("meta/mime_type");
			Number width     = val("meta/width");
			Number height    = val("meta/height");

			if (null == scheme || null == namespace || null == size || null == hash || null == mime_type) {
				return;
			}
			if (mime_type.startsWith("inode/")) {
				return;
			}

			Path obj_file = obj(true);
			if (null == obj_file) {
				return;
			}

			int idx = mime_type.indexOf("/");
			String m1 = mime_type.substring(0, idx);
			String m2 = mime_type.substring(idx + 1);

			String nsdir = null;
			if (null != namespace) {

				String[] items = namespace.split(Pattern.quote("."));
				String ns1 = items[0];
				String ns2 = items[1];

				if ("jp".equals(ns1) && Set.of("ac", "co", "go", "or", "ad", "ne", "gr", "ed", "lg").contains(ns2)) {
					nsdir = ns1 + "." + ns2 + "/" + namespace;
				} else {
					nsdir = ns1 + "/" + namespace;
				}

			}

			List<Path> refs = new LinkedList<>();
			refs.add(depth(ref_dir.resolve("mime_type").resolve(m1).resolve(m2)));
			refs.add(depth(ref_dir.resolve("namespace").resolve(nsdir)));

			while (!_0.empty(refs)) {

				Path ref_file = refs.remove(0);

				try {
					synchronized (root_fs) {
						Files.createDirectories(ref_file.getParent());
						Files.createLink(ref_file, obj_file);
					}
				} catch (FileAlreadyExistsException e) {
					// pass
				} catch (IOException e) {
					exceptions.add(e);
				}

			}

		}

		private void mkdir() {

			String scheme    = scheme();
			String mime_type = val("meta/mime_type");
			String namespace = namespace();

//			_0.debug.mksrcdir(pkgdir);
//			_0.debug.mksrcdir(ns.replace(".", "/").replace("-", "_"));

		}

	}

	@SuppressWarnings("serial")
	public static class BigSetException extends IllegalStateException {

		private BigSetException(final String message) {
			super(message);
		}

		private BigSetException(final Throwable cause) {
			super(cause);
		}

		private BigSetException(final String message, final Throwable cause) {
			super(message, cause);
		}

	}

	@Override
	@Deprecated
	public final boolean contains(Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public final boolean containsAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public final Iterator<Entry> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public final Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public final <T> T[] toArray(final T[] arr) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public final boolean remove(final Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public final boolean removeAll(final Collection<?> objs) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public final boolean retainAll(final Collection<?> objs) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public final void clear() {
		throw new UnsupportedOperationException();
	}

}
