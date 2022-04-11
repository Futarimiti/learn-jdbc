import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RobustDemo
{
	private static ResourceBundle rb;

	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;

	static
	{
		rb = ResourceBundle.getBundle("Info");

		dbUrl = rb.getString("url");
		dbUsername = rb.getString("username");
		dbPassword = rb.getString("password");
	}

	public static void main(String[] args)
	{
		Console console = System.console();

		if (console == null) System.exit(1);

		String username, password;

		username = console.readLine("Enter username: ");
		password = new String(console.readPassword("Enter password: "));

		boolean loginSuccess = login(username, password);

		if (loginSuccess) console.printf("Login success\n");
		else console.printf("Login failed\n");
	}

	private static boolean login(String username, String password)
	{
		boolean res = false;

		try
			(
				Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
				PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM `t_user` WHERE `username` = ? AND `password` = ?");
			)
		{
			pstmt.setString(1, username);
			pstmt.setString(2, password);

			try (ResultSet rs = pstmt.executeQuery())
			{
				res = rs.next();
			}
		}
		catch (SQLException e) { e.printStackTrace(); }

		return res;
	}
}
