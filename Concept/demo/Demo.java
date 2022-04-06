import java.lang.reflect.InvocationTargetException;

/*
 * an easy demo to the essence of JDBC.
 *
 * In JDBC, Sun writes the API as a standard,
 * databases implement the API as drivers,
 * while we the programmer use the API.
 */

/*
 * this is the JDBC API designed by Sun
 * as a standard of database connection.
 */
interface JDBC
{
	void getConnection();
}

/*
 * this is the MySQL implementation of JDBC as a driver.
 */
class MySQL implements JDBC
{
	@Override
	public void getConnection()
	{
		System.out.println("Connecting to MySQL...");
	}

	public MySQL() {}
}

/*
 * this is the Oracle implementation of JDBC as a driver.
 */
class Oracle implements JDBC
{
	@Override
	public void getConnection()
	{
		System.out.println("Connecting to Oracle...");
	}
}

/*
 * this is SQLServer implementation of JDBC as a driver.
 */
class SQLServer implements JDBC
{
	@Override
	public void getConnection()
	{
		System.out.println("Connecting to SQLServer...");
	}
}

/*
 * this is us, the programmer who are orienting to JDBC.
 */
class Main
{
	public static void main (String[] args)
	{
		JDBC jdbc = new MySQL();
		jdbc.getConnection();

		// or use reflection more greater flexibility:
		JDBC j2 = null;

		try
		{
			Class<?> c = Class.forName("MySQL");
			// you can use a proplist to pass the desired database type:
			// Class<?> c = Class.forName(ResourceBundle.getBundle("database").getString("database"));
			j2 = (JDBC)c.getConstructor().newInstance();
		}
		catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e)
		{
			e.printStackTrace();
		}

		j2.getConnection();
	}
}
