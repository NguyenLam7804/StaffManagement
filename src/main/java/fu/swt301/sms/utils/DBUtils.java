package fu.swt301.sms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {

    private static boolean initialized = false;

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if ("true".equals(System.getProperty("useH2"))) {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:StaffManagementDB;DB_CLOSE_DELAY=-1;MODE=MSSQLServer", "sa", "123");
            initializeH2(conn);
            return conn;
        }

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        String url =
                "jdbc:sqlserver://localhost:1433;"
                + "databaseName=StaffManagementDB;"
                + "encrypt=false;"
                + "trustServerCertificate=true;";

        return DriverManager.getConnection(url, "sa", "123");
    }

    private static synchronized void initializeH2(Connection conn) throws SQLException {
        if (initialized) return;
        try (java.io.InputStream schemaStream = DBUtils.class.getClassLoader().getResourceAsStream("schema.sql");
             java.io.InputStream dataStream = DBUtils.class.getClassLoader().getResourceAsStream("data.sql")) {
            if (schemaStream != null) {
                executeSqlScript(conn, schemaStream);
            }
            if (dataStream != null) {
                executeSqlScript(conn, dataStream);
            }
            initialized = true;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static void executeSqlScript(Connection conn, java.io.InputStream stream) throws SQLException, java.io.IOException {
        String script = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        try (java.sql.Statement stmt = conn.createStatement()) {
            for (String sql : script.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql.trim());
                }
            }
        }
    }
}