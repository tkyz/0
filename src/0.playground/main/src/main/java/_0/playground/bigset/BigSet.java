package _0.playground.bigset;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.sqlite.Function;

import _0.playground.core._0;
import _0.playground.jdbc.Jdbc;

public final class BigSet implements Set<Entry> {

	public  static final Path store_file = _0.user.home.resolve("bigset.db");
	private static final long timeout    = 5 * 60 * 1000;

	private static Connection   con       = null;
	private static int          bulkmax   = -1;
	private static List<Entry>  queue_upd = null;
	private static List<byte[]> queue_del = null;

	static {
		init();
		daemon();
	}

	public BigSet() {
	}

	private static final void init() {

		try {

			con       = new Jdbc("sqlite").path(store_file).connect();
			bulkmax   = Math.min(Jdbc.bulksize(con, 2), 0x2000); // SQLITE_TOOBIG
			queue_upd = Collections.synchronizedList(new LinkedList<>());
			queue_del = Collections.synchronizedList(new LinkedList<>());

			try (Statement stmt = con.createStatement()) {
				stmt.execute("PRAGMA busy_timeout = " + timeout);
				stmt.execute("""
CREATE TABLE IF NOT EXISTS kvs (
   key BLOB NOT NULL
  ,val BLOB
  ,PRIMARY KEY (key)
)
""");
			}

			Function.create(con, "udf_json_merge", new Function() {
				@Override
				protected final void xFunc()
						throws SQLException {

					byte[] v1  = value_blob(0);
					byte[] v2  = value_blob(1);
					byte[] res = null;

					try {

						Map<?, ?> map1   = new JSONObject(new String(v1)).toMap();
						Map<?, ?> map2   = new JSONObject(new String(v2)).toMap();
						Map<?, ?> merged = (Map<?, ?>)_0.merge(map1, map2);

						res = new JSONObject(merged).toString().getBytes();

					} catch (JSONException e) {
						res = v2;
					}

					result(res);

				}
			});

		} catch (IOException | SQLException e) {
			throw new BigSetException(e);
		}

	}

	private static final void daemon() {

		Thread daemon = new Thread(() -> {

			Thread.currentThread().setName(BigSet.class.getName() + "/daemon");
			try {
				while (!con.isClosed()) {

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
				// break

			} finally {
				_0.close(con);
			}

		});
		daemon.setDaemon(true);
		daemon.start();

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

	@Override
	public final Iterator<Entry> iterator() {

		return new Iterator<Entry>() {

			private int capacity = _0.sys.cpu_core;
			private Deque<Entry> deque = new ArrayDeque<>();

			@Override
			public boolean hasNext() {

				try {
					if (_0.empty(deque) && !con.isClosed()) {

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

						synchronized (con) {
							try (PreparedStatement stmt = con.prepareStatement(query)) {

								stmt.setFetchSize(capacity);
								Jdbc.bind(stmt, capacity);

								try (ResultSet rs = stmt.executeQuery()) {
									while (rs.next()) {
										deque.addLast(new Entry(rs));
									}
								}

							}
						}

					}
				} catch (SQLException e) {
					throw new BigSetException(e);
				}

				return !_0.empty(deque);

			}

			@Override
			public Entry next() {
				return deque.removeFirst();
			}

		};

	}

	private static final int upd()
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

						stmt.setBytes(i * 2 + 1, entry.getKey());
						stmt.setBytes(i * 2 + 2, entry.getValue());

					}

					stmt.executeUpdate();

				}
			}

		}

		return rowcnt;

	}

	private static final int del()
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
						stmt.setBytes(i + 1, queue_del.remove(0));
					}

					stmt.executeUpdate();

				}
			}

		}

		return rowcnt;

	}

	public static final Entry get(final String key) {

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
