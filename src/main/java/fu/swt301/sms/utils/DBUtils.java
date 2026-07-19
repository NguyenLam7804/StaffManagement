package fu.swt301.sms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {
    // Đã thêm mã hóa encrypt=true và trustServerCertificate=true để không bị lỗi bắt tay SSL với SQL Server
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=TestDB;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sang";
    private static final String PASS = "123"; 

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(URL, USER, PASS);
    }
}