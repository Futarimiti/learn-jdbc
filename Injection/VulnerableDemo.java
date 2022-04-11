// inject this programme using
// "' or '1'='1" for both username and pass

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class VulnerableDemo
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
				Statement stmt = conn.createStatement();
				ResultSet resultSet = stmt.executeQuery("SELECT * FROM t_user WHERE username = '" + username + "' AND password = '" + password + "'");
			)
		{
			res = resultSet.next();
		}
		catch (SQLException e) { e.printStackTrace(); }

		return res;
	}
}
