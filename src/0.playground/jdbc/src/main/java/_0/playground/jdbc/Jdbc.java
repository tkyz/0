package _0.playground.jdbc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import _0.playground.core._0;

public final class Jdbc {

	public static final int fetchsize = 1 << 16;

	private Map<String, Object> attrs = new HashMap<>();

	public Jdbc(final String type) {
		type(type);
	}

	private Jdbc type(final String type) {
		attrs.put(_0.concurrent.methodName(), type);
		return this;
	}

	public String type() {
		return (String)attrs.get(_0.concurrent.methodName());
	}

	public Jdbc driver(final String driver) {
		attrs.put(_0.concurrent.methodName(), driver);
		return this;
	}

	public String driver() {
		return (String)attrs.get(_0.concurrent.methodName());
	}

	public Jdbc uri(final String uri) {
		attrs.put(_0.concurrent.methodName(), uri);
		return this;
	}

	public String uri() {
		return (String)attrs.get(_0.concurrent.methodName());
	}

	public Jdbc host(final String host) {
		attrs.put(_0.concurrent.methodName(), host);
		return this;
	}

	public Jdbc host(final InetAddress addr) {
		return host(addr.getHostAddress());
	}

	public String host() {
		return (String)attrs.get(_0.concurrent.methodName());
	}

	public Jdbc port(final int port) {
		attrs.put(_0.concurrent.methodName(), Integer.valueOf(port));
		return this;
	}

	public int port() {
		Integer port = (Integer)attrs.get(_0.concurrent.methodName());
		return null == port ? -1 : port.intValue();
	}

	public Jdbc database(final String database) {
		attrs.put(_0.concurrent.methodName(), database);
		return this;
	}

	public String database() {
		return (String)attrs.get(_0.concurrent.methodName());
	}

	public Jdbc path(final String path) {
		attrs.put(_0.concurrent.methodName(), path.replace('\\', '/'));
		return this;
	}

	public Jdbc path(final Path path) {
		return path(path.toAbsolutePath().normalize().toString());
	}

	public Jdbc path(final File path) {
		return path(path.toPath());
	}

	public Path path() {
		String path = (String)attrs.get(_0.concurrent.methodName());
		return null == path ? null : Path.of(path);
	}

	public Jdbc username(final String username) {
		attrs.put(_0.concurrent.methodName(), username);
		return this;
	}

	public String username() {
		return (String)attrs.get(_0.concurrent.methodName());
	}

	public Jdbc password(final String password) {
		attrs.put(_0.concurrent.methodName(), password);
		return this;
	}

	public String password() {
		return (String)attrs.get(_0.concurrent.methodName());
	}

//	public Jdbc readonly() {
//		attrs.put(_0.methodName(), Boolean.TRUE);
//		return this;
//	}

	public synchronized Connection connect()
			throws IOException, SQLException {

		String  type     = type();
		String  driver   = driver();
		String  uri      = uri();
		int     port     = port();
		String  username = username();
		String  password = password();
//		Boolean readonly = (Boolean)attrs.get("readonly");

		{

			String def_driver = null;
			String def_uri    = null;
			int    def_port   = -1;

			if ("sqlserver".equalsIgnoreCase(type)) {
				def_driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//				def_uri    = "jdbc:sqlserver://{host}[:{port}]";
//				def_uri    = "jdbc:sqlserver://{host}[:{port}][;databaseName={database}]";
				def_uri    = "jdbc:sqlserver://{host}[:{port}];encrypt=true;trustServerCertificate=true";
				def_port   = 1433;

			} else if ("oracle".equalsIgnoreCase(type)) {
				def_driver = "oracle.jdbc.OracleDriver";
				def_uri    = "jdbc:oracle:thin://{host}[:{port}]/{database}";
				def_port   = 1521;

			} else if ("cache".equalsIgnoreCase(type)) {
				def_driver = "com.intersys.jdbc.CacheDriver";
				def_uri    = "jdbc:Cache://{host}[:{port}]/{database}";
				def_port   = 1972;

			} else if ("mysql".equalsIgnoreCase(type)) {
				def_driver = "com.mysql.cj.jdbc.Driver";
				def_uri    = "jdbc:mysql://{host}[:{port}]/[{database}]";
				def_port   = 3306;

			} else if ("mariadb".equalsIgnoreCase(type)) {
				def_driver = "org.mariadb.jdbc.Driver";
				def_uri    = "jdbc:mariadb://{host}[:{port}]/[{database}]";
				def_port   = 3306;

			} else if ("postgres".equalsIgnoreCase(type) || "postgresql".equalsIgnoreCase(type)) {
				def_driver = "org.postgresql.Driver";
				def_uri    = "jdbc:postgresql://{host}[:{port}]/[{database}]";
				def_port   = 5432;

			} else if ("hive".equalsIgnoreCase(type)) {
				def_driver = "org.apache.hive.jdbc.HiveDriver";
				def_uri    = "jdbc:hive2://{host}[:{port}][/{database}]";
				def_port   = 10000;

			} else if ("sqlite".equalsIgnoreCase(type)) {
				def_driver = "org.sqlite.JDBC";
				def_uri    = "jdbc:sqlite:{path}";

			} else if ("derby".equalsIgnoreCase(type)) {
				def_driver = "org.apache.derby.jdbc.EmbeddedDriver";
				def_uri    = "jdbc:derby:{path}";

//				def_driver = "org.apache.derby.jdbc.ClientDriver";
//				def_uri    = "jdbc:derby://{host}[:{port}]/{path}";
//				def_port   = 1527;

//			} else if ("access".equalsIgnoreCase(type)) {
//				def_driver = "net.ucanaccess.jdbc.UcanaccessDriver";
//				def_uri    = "jdbc:ucanaccess://{path}";

			} else {
				throw new UnsupportedOperationException("type: " + type);
			}

			driver = _0.nvl(driver, def_driver);
			uri    = _0.nvl(uri,    def_uri);
			port   = -1 == port ? def_port : port;

		}

		// uri置換
		{

			Map<String, Object> uri_bind = new HashMap<>();
			uri_bind.putAll(attrs);
			uri_bind.put("port",     port);
			uri_bind.put("database", database());
			uri_bind.put("path",     path());

			for (Entry<String, Object> entry : uri_bind.entrySet()) {

				String key = entry.getKey();
				Object val = entry.getValue();
				if (null == val) {
					val = "";
				}

				uri = uri.replaceAll("\\[?(:?)\\{" + key + "\\}\\]?", "$1" + Matcher.quoteReplacement(val.toString()));

			}

		}

		{

			Path p = path();

			if (null != p) {
				Files.createDirectories(p.getParent());
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

//		if (null != readonly) {
//			con.setReadOnly(readonly.booleanValue());
//		}
//		con.setAutoCommit(false);

//		DatabaseMetaData meta = con.getMetaData();
//		log.debug("---");
//		log.debug("connect:");
//		log.debug("  database:");
//		log.debug("    name: {}",       meta.getDatabaseProductName());
//		log.debug("    version: {}",    meta.getDatabaseProductVersion());
//		log.debug("  jdbc:");
//		log.debug("    version: {}.{}", meta.getJDBCMajorVersion(), meta.getJDBCMinorVersion());
//		log.debug("    driver:");
//		log.debug("      class: {}",    driver);
//		log.debug("      name: {}",     meta.getDriverName());
//		log.debug("      version: {}",  meta.getDriverVersion());
//		log.debug("    uri: {}",        uri);

		return con;

	}

	public static String type(final Connection con, final int type)
			throws SQLException {

		if (!sqlite(con)) {
			throw new UnsupportedOperationException();
		}

		Map<Integer, String> types = new HashMap<>();
		types.put(Types.BIT,           "INTEGER");
		types.put(Types.BOOLEAN,       "INTEGER");
		types.put(Types.TINYINT,       "INTEGER");
		types.put(Types.SMALLINT,      "INTEGER");
		types.put(Types.INTEGER,       "INTEGER");
		types.put(Types.BIGINT,        "INTEGER");
		types.put(Types.FLOAT,         "REAL");
		types.put(Types.DOUBLE,        "REAL");
		types.put(Types.REAL,          "REAL");
		types.put(Types.NUMERIC,       "REAL");
		types.put(Types.DECIMAL,       "REAL");
		types.put(Types.CHAR,          "TEXT");
		types.put(Types.NCHAR,         "TEXT");
		types.put(Types.VARCHAR,       "TEXT");
		types.put(Types.NVARCHAR,      "TEXT");
		types.put(Types.LONGVARCHAR,   "TEXT");
		types.put(Types.LONGNVARCHAR,  "TEXT");
		types.put(Types.TIMESTAMP,     "TEXT");
		types.put(Types.DATE,          "TEXT");
		types.put(Types.TIME,          "TEXT");
		types.put(Types.BINARY,        "BLOB");
		types.put(Types.VARBINARY,     "BLOB");
		types.put(Types.LONGVARBINARY, "BLOB");
		types.put(Types.DISTINCT,      "NULL");
		types.put(Types.ARRAY,         "NULL");
		types.put(Types.OTHER,         "NULL");

		Integer key = Integer.valueOf(type);
		if (!types.containsKey(key)) {
			throw new IllegalArgumentException(String.valueOf(key));
		}

		return types.get(key);

	}

	public static Map<String, Object> map(final ResultSet rs)
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

	@Deprecated
	public static <T> T map(final ResultSet rs, T out)
			throws IOException, SQLException {

		ResultSetMetaData meta = rs.getMetaData();
		for (int i = 1; i <= meta.getColumnCount(); i++) {

			String label = meta.getColumnLabel(i);
			Object val   = rs.getObject(i);

			try {

				Field field = out.getClass().getDeclaredField(label);
				field.setAccessible(true);
				field.set(out, val);

			} catch (NoSuchFieldException e) {
				e.printStackTrace();

			} catch (IllegalArgumentException e) {
				e.printStackTrace();

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}

		return out;

	}

	public static void bind(final PreparedStatement stmt, final List<?> params)
			throws SQLException {
		bind(stmt, params.toArray());
	}

	public static void bind(final PreparedStatement stmt, final Object... params)
			throws SQLException {
		for (int i = 0; i < params.length; i++) {
			stmt.setObject(1 + i, params[i]);
		}
	}

	public static boolean sqlserver(final Connection con)
			throws SQLException {
		return "Microsoft SQL Server".equalsIgnoreCase(con.getMetaData().getDatabaseProductName());
	}

	public static boolean mysql(final Connection con)
			throws SQLException {
		return "MySQL".equalsIgnoreCase(con.getMetaData().getDatabaseProductName());
	}

	public static boolean mariadb(final Connection con)
			throws SQLException {
		return "MariaDB".equalsIgnoreCase(con.getMetaData().getDatabaseProductName());
	}

	public static boolean postgres(final Connection con)
			throws SQLException {
		return "PostgreSQL".equalsIgnoreCase(con.getMetaData().getDatabaseProductName());
	}

	public static boolean sqlite(final Connection con)
			throws SQLException {
		return "SQLite".equalsIgnoreCase(con.getMetaData().getDatabaseProductName());
	}

	public static String esc(final Connection con, final String val)
			throws SQLException {

		String esc = val;
		if (null == esc) {
			// pass

		} else if (mysql(con) || mariadb(con)) {
			esc = "`" + esc + "`";

		} else {
			esc = "[" + esc + "]";
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
		}

		return list;

	}

	public static List<Map<String, Object>> tables(final Connection con, final String catalog, final String schema)
			throws SQLException {

		List<Map<String, Object>> list = new LinkedList<>();

		List<String> types = new LinkedList<>();
		types.add("TABLE");
		types.add("SYSTEM TABLE");

		try (ResultSet rs = con.getMetaData().getTables(catalog, schema, null, types.toArray(new String[0]))) {
			while (rs.next()) {
				list.add(map(rs));
			}
		}

		return list;

	}

	public static List<Map<String, Object>> columns(final Connection con, final String catalog, final String schema, final String table)
			throws SQLException {

		DatabaseMetaData meta = con.getMetaData();

		Map<String, Number> pk = new HashMap<>();
		try (ResultSet rs = meta.getPrimaryKeys(catalog, schema, table)) {
			while (rs.next()) {

				String key = (String)rs.getObject("COLUMN_NAME");
				Number seq = (Number)rs.getObject("KEY_SEQ");

				pk.put(key, seq);

			}
		}

		List<Map<String, Object>> list = new LinkedList<>();
		try (ResultSet rs = meta.getColumns(catalog, schema, table, null)) {
			while (rs.next()) {

				Map<String, Object> map = map(rs);
				map.put("pk", pk.get(map.get("COLUMN_NAME")));

				list.add(map);

			}
		}

		return list;

	}

	public static List<String> columns(final ResultSet rs)
			throws SQLException {

		List<String> list = new LinkedList<>();

		ResultSetMetaData meta = rs.getMetaData();

		int cnt = meta.getColumnCount();
		for (int i = 0; i < cnt; i++) {
			list.add(meta.getColumnLabel(1 + i));
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

		if (sqlite(con)) {

			// TODO: SQLITE_MAX_SQL_LENGTH:      1,000,000,000
			// TODO: SQLITE_MAX_VARIABLE_NUMBER:        32,766
			bulksize = 32766 / cols;

		} else if (sqlserver(con)) {
			bulksize = 2100 / cols;
		}

		return bulksize;

	}

}
