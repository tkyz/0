package _0;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Jdbc {

	private static final Logger log = LoggerFactory.getLogger(Jdbc.class);

	/** フェッチサイズ */
	public static final int fetchsize = 1 << 16;

	private Map<String, Object> attrs = new HashMap<>();

	public Jdbc(final String type) {
		type(type);
	}

	public Jdbc(final Map<String, Object> attrs) {

		String type = (String)attrs.get("type");
		if (null != type) {
			type(type);
		}

		this.attrs.putAll(attrs);

	}

	public Map<String, Object> attrs() {
		return new HashMap<>(attrs);
	}

	private Jdbc type(final String type) {

		String driver = null;
		String uri    = null;
		int    port   = -1;

		if ("sqlserver".equals(type)) {
			driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//			uri    = "jdbc:sqlserver://{host}[:{port}]";
//			uri    = "jdbc:sqlserver://{host}[:{port}][;databaseName={database}]";
			uri    = "jdbc:sqlserver://{host}[:{port}];encrypt=true;trustServerCertificate=true";
			port   = 1433;

		} else if ("oracle".equals(type)) {
			driver = "oracle.jdbc.OracleDriver";
			uri    = "jdbc:oracle:thin://{host}[:{port}]/{database}";
			port   = 1521;

		} else if ("cache".equals(type)) {
			driver = "com.intersys.jdbc.CacheDriver";
			uri    = "jdbc:Cache://{host}[:{port}]/{database}";
			port   = 1972;

		} else if ("mysql".equals(type)) {
			driver = "com.mysql.cj.jdbc.Driver";
			uri    = "jdbc:mysql://{host}[:{port}]/[{database}]";
			port   = 3306;

		} else if ("mariadb".equals(type)) {
			driver = "org.mariadb.jdbc.Driver";
			uri    = "jdbc:mariadb://{host}[:{port}]/[{database}]";
			port   = 3306;

		} else if ("postgres".equals(type)) {
			driver = "org.postgresql.Driver";
			uri    = "jdbc:postgresql://{host}[:{port}]/[{database}]";
			port   = 5432;

		} else if ("hive".equals(type)) {
			driver = "org.apache.hive.jdbc.HiveDriver";
			uri    = "jdbc:hive2://{host}[:{port}][/{database}]";
			port   = 10000;

		} else if ("sqlite".equals(type)) {
			driver = "org.sqlite.JDBC";
			uri    = "jdbc:sqlite:{file}";

		} else if ("derby".equals(type)) {
			driver = "org.apache.derby.jdbc.EmbeddedDriver";
			uri    = "jdbc:derby:{file}";

//			driver = "org.apache.derby.jdbc.ClientDriver";
//			uri    = "jdbc:derby://{host}[:{port}]/{file}";
//			port   = 1527;

//		} else if ("access".equals(type)) {
//			driver = "net.ucanaccess.jdbc.UcanaccessDriver";
//			uri    = "jdbc:ucanaccess://{file}";

		} else {
			throw new IllegalArgumentException(type);
		}

		driver(driver);
		uri(uri);
		port(port);

		return this;

	}

	public Jdbc driver(final String driver) {
		attrs.put(_0.methodName(), driver);
		return this;
	}

	public String driver() {
		return (String)attrs.get(_0.methodName());
	}

	public Jdbc uri(final String uri) {
		attrs.put(_0.methodName(), uri);
		return this;
	}

	public String uri() {
		return (String)attrs.get(_0.methodName());
	}

	public Jdbc host(final String host) {
		attrs.put(_0.methodName(), host);
		return this;
	}

	public Jdbc host(final InetAddress addr) {
		return host(addr.getHostAddress());
	}

	public String host() {
		return (String)attrs.get(_0.methodName());
	}

	public Jdbc port(final int port) {
		attrs.put(_0.methodName(), port);
		return this;
	}

	public int port() {
		return ((Integer)attrs.get(_0.methodName())).intValue();
	}

	public Jdbc database(final String database) {
		attrs.put(_0.methodName(), database);
		return this;
	}

	public String database() {
		return (String)attrs.get(_0.methodName());
	}

	public Jdbc file(final String file) {
		attrs.put(_0.methodName(), file);
		return this;
	}

	public Jdbc file(final Path file) {
		return file(file.toString());
	}

	public Jdbc file(final File file) {
		return file(file.toPath());
	}

	public Path file() {
		return Path.of((String)attrs.get(_0.methodName()));
	}

	public Jdbc username(final String username) {
		attrs.put(_0.methodName(), username);
		return this;
	}

	public String username() {
		return (String)attrs.get(_0.methodName());
	}

	public Jdbc password(final String password) {
		attrs.put(_0.methodName(), password);
		return this;
	}

	public String password() {
		return (String)attrs.get(_0.methodName());
	}

	public Jdbc readonly() {
		attrs.put(_0.methodName(), Boolean.TRUE);
		return this;
	}

	public synchronized Connection connect()
			throws SQLException {

		String  uri      = (String)attrs.get("uri");
		String  driver   = (String)attrs.get("driver");
		String  username = (String)attrs.get("username");
		String  password = (String)attrs.get("password");
		Boolean readonly = (Boolean)attrs.get("readonly");

		// uri置換
		{

			Set<String> keys = new HashSet<>();
			keys.add("host");
			keys.add("port");
			keys.add("database");
			keys.add("file");

			for (String key : keys) {

				Object val = attrs.get(key);
				if (null == val) {
					val = "";
				}

				uri = uri.replaceAll("\\[?(:?)\\{" + key + "\\}\\]?", "$1" + Matcher.quoteReplacement(val.toString()));

			}

		}

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}

		Connection con = null;
		if (null == username) {
			con = DriverManager.getConnection(uri);
		} else {
			con = DriverManager.getConnection(uri, username, password);
		}

		if (null != readonly) {
			con.setReadOnly(readonly.booleanValue());
		}
//		con.setAutoCommit(false);

		if (log.isDebugEnabled()) {
			DatabaseMetaData meta = con.getMetaData();
			log.debug("---");
			log.debug("connect:");
			log.debug("  database:");
			log.debug("    name: {}",       meta.getDatabaseProductName());
			log.debug("    version: {}",    meta.getDatabaseProductVersion());
			log.debug("  jdbc:");
			log.debug("    version: {}.{}", meta.getJDBCMajorVersion(), meta.getJDBCMinorVersion());
			log.debug("    driver:");
			log.debug("      class: {}",    driver);
			log.debug("      name: {}",     meta.getDriverName());
			log.debug("      version: {}",  meta.getDriverVersion());
			log.debug("    uri: {}",        uri);
		}

		return con;

	}

	public static final Map<String, Object> map(final ResultSet rs)
			throws SQLException {

		Map<String, Object> map = new LinkedHashMap<>();

		ResultSetMetaData meta = rs.getMetaData();
		for (int i = 1; i <= meta.getColumnCount(); i++) {

			String lbl = meta.getColumnLabel(i);
			Object val = rs.getObject(i);

			map.put(lbl, val);

		}

		return map;

	}

	public static final void bind(final PreparedStatement stmt, final List<?> params)
			throws SQLException {
		bind(stmt, params.toArray());
	}

	public static final void bind(final PreparedStatement stmt, final Object... params)
			throws SQLException {
		for (int i = 0; i < params.length; i++) {
			stmt.setObject(1 + i, params[i]);
		}
	}

	public static boolean sqlserver(final Connection con)
			throws SQLException {
		return "Microsoft SQL Server".equals(con.getMetaData().getDatabaseProductName());
	}

	public static boolean mysql(final Connection con)
			throws SQLException {
		return "MySQL".equals(con.getMetaData().getDatabaseProductName());
	}

	public static boolean mariadb(final Connection con)
			throws SQLException {
		return "MariaDB".equals(con.getMetaData().getDatabaseProductName());
	}

	public static boolean postgres(final Connection con)
			throws SQLException {
		return "PostgreSQL".equals(con.getMetaData().getDatabaseProductName());
	}

	public static boolean sqlite(final Connection con)
			throws SQLException {
		return "SQLite".equals(con.getMetaData().getDatabaseProductName());
	}

	public static String esc(final Connection con, final String val)
			throws SQLException {

		String esc = val;
		if (null != esc) {

//			boolean is_esc = false;
//			is_esc |= -1 < esc.indexOf('.');
//			is_esc |= -1 < esc.indexOf('/');
//			is_esc |= -1 < esc.indexOf('-');
//			is_esc |= -1 < esc.indexOf(' '); is_esc |= -1 < esc.indexOf('　');
//			is_esc |= -1 < esc.indexOf('('); is_esc |= -1 < esc.indexOf('（');
//			is_esc |= -1 < esc.indexOf(')'); is_esc |= -1 < esc.indexOf('）');
//			is_esc |= esc.matches("^[0-9].*$");

			if (mysql(con) || mariadb(con)) {
				esc = "`" + esc + "`";

			} else {
				esc = "[" + esc + "]";
			}

		}

		return esc;

	}

	public static boolean meta(final Connection con, final String catalog, final String schema, final String table)
			throws SQLException {

		boolean meta = false;

		if (sqlserver(con)) {
			meta |= "INFORMATION_SCHEMA".equals(schema);
			meta |= "db_accessadmin".equals(schema);
			meta |= "db_backupoperator".equals(schema);
			meta |= "db_datareader".equals(schema);
			meta |= "db_datawriter".equals(schema);
			meta |= "db_ddladmin".equals(schema);
			meta |= "db_denydatareader".equals(schema);
			meta |= "db_denydatawriter".equals(schema);
			meta |= "db_owner".equals(schema);
			meta |= "db_securityadmin".equals(schema);
			meta |= "guest".equals(schema);
			meta |= "sys".equals(schema);

		} else if (mysql(con) || mariadb(con)) {
			meta |= "information_schema".equals(catalog);
			meta |= "mysql".equals(catalog);
			meta |= "performance_schema".equals(catalog);
			meta |= "sys".equals(catalog);

		} else if (postgres(con)) {
			meta |= "postgres".equals(catalog);
			meta |= "information_schema".equals(schema);
			meta |= "pg_catalog".equals(schema);
		}

		return meta;

	}

	public static List<Map<String, Object>> catalogs(final Connection con)
			throws SQLException {

		List<Map<String, Object>> list = new LinkedList<>();

		try (ResultSet rs = con.getMetaData().getCatalogs()) {
			while (rs.next()) {
				list.add(map(rs));
			}
		}

		return list;

	}

	public static List<Map<String, Object>> schemas(final Connection con, final String catalog)
			throws SQLException {

		List<Map<String, Object>> list = new LinkedList<>();

		try (ResultSet rs = con.getMetaData().getSchemas(catalog, null)) {
			while (rs.next()) {
				list.add(map(rs));
			}
		} catch (AbstractMethodError e) {
			log.trace("{}", catalog, e);
		} catch (SQLFeatureNotSupportedException e) {
			// pass
		}

		return list;

	}

	public static List<Map<String, Object>> tables(final Connection con, final String catalog, final String schema)
			throws SQLException {

		List<Map<String, Object>> list = new LinkedList<>();

		try (ResultSet rs = con.getMetaData().getTables(catalog, schema, null, new String[] {"TABLE"})) {
			while (rs.next()) {
				list.add(map(rs));
			}
		}

		return list;

	}

	public static List<Map<String, Object>> columns(final Connection con, final String catalog, final String schema, final String table)
			throws SQLException {

		Map<String, Number> pk = new HashMap<>();
		try (ResultSet rs = con.getMetaData().getPrimaryKeys(catalog, schema, table)) {
			while (rs.next()) {

				String key = (String)rs.getObject("COLUMN_NAME");
				Number seq = (Number)rs.getObject("KEY_SEQ");

				pk.put(key, seq);

			}
		} catch (SQLException e) {
			log.trace("", e);
		}

		List<Map<String, Object>> list = new LinkedList<>();
		try (ResultSet rs = con.getMetaData().getColumns(catalog, schema, table, null)) {
			while (rs.next()) {

				Map<String, Object> map = map(rs);
				map.put("pk", pk.get(map.get("COLUMN_NAME")));

				list.add(map);

			}
		}

		return list;

	}

	public static List<Map<String, Object>> pk(final Connection con, final String catalog, final String schema, final String table)
			throws SQLException {

		List<Map<String, Object>> list = new LinkedList<>();

		try (ResultSet rs = con.getMetaData().getPrimaryKeys(catalog, schema, table)) {
			while (rs.next()) {
				list.add(map(rs));
			}
		}

		return list;

	}

	public static List<Map<String, Object>> table_types(final Connection con)
			throws SQLException {

		List<Map<String, Object>> list = new LinkedList<>();

		try (ResultSet rs = con.getMetaData().getTableTypes()) {
			while (rs.next()) {
				list.add(map(rs));
			}
		}

		return list;

	}

	public static int bulksize(final Connection con, final int cols)
			throws SQLException {

		int bulksize = fetchsize;

		if (sqlserver(con)) {
			bulksize = cols / (2100 - 1);
		}

		return bulksize;

	}

//	public static final List<Map<String, Object>> list(final Connection con, final CharSequence query, final Object... params)
//			throws SQLException {
//
//		List<Map<String, Object>> entities = new LinkedList<>();
//
//		try (PreparedStatement stmt = con.prepareStatement(query.toString())) {
//
//			stmt.setFetchSize(Jdbc.fetchsize);
//			Jdbc.bind(stmt, params);
//
//			try (ResultSet rs = stmt.executeQuery()) {
//				while (rs.next()) {
//					entities.add(Jdbc.map(rs));
//				}
//			}
//
//		}
//
//		return entities;
//
//	}

	public static final long transfer(final Connection in, final CharSequence in_query, final List<Object> in_params, final Consumer<Map<String, Object>> filter, final Connection out, final CharSequence out_table)
			throws SQLException {

		int cnt = 0;

		try (PreparedStatement in_stmt = in.prepareStatement(in_query.toString())) {

			in_stmt.setFetchSize(Jdbc.fetchsize);

			Jdbc.bind(in_stmt, in_params);

			try (ResultSet in_rs = in_stmt.executeQuery()) {

				StringBuilder out_values     = null;
				StringBuilder out_query_base = null;
				List<Object>  out_params     = null;

				int bulksize = 0;
				int bulkcnt  = 0;

				while (in_rs.next()) {

					Map<String, Object> map = Jdbc.map(in_rs);
					if (null != filter) {
						filter.accept(map);
					}

					// 初期化
					if (0 == cnt && 0 == bulkcnt) {

						out_values     = new StringBuilder();
						out_query_base = new StringBuilder();
						out_params     = new LinkedList<>();

						out_values.append("(");
						for (int i = 0; i < map.size(); i++) {
							out_values.append(0 == i ? "" : ",");
							out_values.append("?");
						}
						out_values.append(")");

						out_query_base.append("INSERT INTO ");
						out_query_base.append("  " + out_table + " ");
						out_query_base.append("    (" + String.join(",", map.keySet()) + ")");
						out_query_base.append("  VALUES ");

						bulksize = Jdbc.bulksize(out, map.size());

					}

					out_params.addAll(map.values());

					bulkcnt++;
					if (bulksize != bulkcnt) {
						continue;
					}

					// TODO: bulkcntが前回と同じなら使い回せる
					{

						StringBuilder out_query = new StringBuilder();
						out_query.append(out_query_base);
						for (int i = 0; i < bulkcnt; i++) {
							out_query.append(0 == i ? "" : ",");
							out_query.append(out_values);
						}

						try (PreparedStatement out_stmt = out.prepareStatement(out_query.toString())) {

							Jdbc.bind(out_stmt, out_params);

							cnt += out_stmt.executeUpdate();

						}

					}

					out_params.clear();

					bulkcnt = 0;

				}

				// TODO: 2重定義
				{

					StringBuilder out_query = new StringBuilder();
					out_query.append(out_query_base);
					for (int i = 0; i < bulkcnt; i++) {
						out_query.append(0 == i ? "" : ",");
						out_query.append(out_values);
					}

					try (PreparedStatement out_stmt = out.prepareStatement(out_query.toString())) {

						Jdbc.bind(out_stmt, out_params);

						cnt += out_stmt.executeUpdate();

					}

				}

			}

		}

		return cnt;

	}

}
