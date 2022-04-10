import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class Demo
{
	public static void main(String[] args)
	{
		// get connection information from properties file
		ResourceBundle resourceBundle = ResourceBundle.getBundle("Info");
		String url = resourceBundle.getString("url");
		String username = resourceBundle.getString("username");
		String password = resourceBundle.getString("password");

		// *INDENT-OFF*
		String sql = """
			SELECT
				*
			FROM
				`EMP`
			""";

		try
			(
				Connection conn = DriverManager.getConnection(url, username, password);
				Statement stmt = conn.createStatement();
				ResultSet res = stmt.executeQuery(sql)
			)
		{
			while (res.next())
			{
				String name = res.getString("ENAME");
				System.out.printf("%s\n", name);
			}
		}
		catch (SQLException e) { e.printStackTrace(); }
	}
}
