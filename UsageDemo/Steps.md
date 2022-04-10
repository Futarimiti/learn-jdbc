# Steps of JDBC

1.  Register driver
    *   Tell java to connect to which database, MySQL, Oracle, etc
2.  Get connection
    *   Open the channel for JVM and database processes
    *   This drains resources and must be closed after use
3.  Fetch statement object for query
    *   Which executes SQL statements
4.  Execute SQL queries
5.  Analyse query results
    *   Only if DQL was executed in step 4
6.  Free resources
    *   Again this connection is a communication between JVM and database process and MUST be closed after use

### Register drive

<!-- Java API specification on DriverManager:
NOTE: The DataSource interface,
provides another way to connect to a data source. 
The use of a DataSource object is the preferred means of connecting to a data source.

Consider use of DataSource in the future. TODO
-->

This step registers database driver(s) to `DriverManager`
for further database operations.

<!-- OUTDATED; see uncommented instruction below.

To register a drive, call `DriverManager.registerDriver(Driver)`
and pass in a `Driver` of a specific database.

*   If the driver is currently registered, no action is taken.

Here MySQL is used, and hence `com.mysql.cj.jdbc.Driver`,
which implements `java.mysql.Driver`, is used as our driver.

To avoid naming clash of two classes,
write full class path for one of them:

```java
Driver driver = new com.mysql.cj.jdbc.Driver();
DriverManager.registerDriver(driver);
```

Where both methods throws `SQLException` which extends `Exception`;
hence surround with `try catch`:

```java
try
{
	Driver driver = new com.mysql.cj.jdbc.Driver();
	DriverManager.registerDriver(driver);
}
catch (SQLException e) { e.printStackTrace(); }
```

We herein registered a MySQL driver.

Alternatively, if you look at the static block of MySQL
driver class:
(with some format alterations)

```java
// Register ourselves with the DriverManager
static
{
	try { java.sql.DriverManager.registerDriver(new Driver()); }
	catch (SQLException e) { throw new RuntimeException("Can't register driver!"); }
}
```

Which has already done this step for you.
Therefore you can also load the class to do this step,
using `Class.forName(String)`:

```java
try { Class.forName("com.mysql.cj.jdbc.Driver"); }
catch (ClassNotFoundException e) { e.printStackTrace(); }
```

Which involves use of reflection,
where the path of the driver class can be sourced
from an external config file;
hence this method is more frequently used for its flexibility.
-->

As from JDBC 4.0, you do not need to manually register drivers;
when being class loaded, `DriverManager` attempts to load
available drivers as part of its initialisation.

This is done with help of `ServiceLoader`
from Service Provider Interface (SPI):

*   `DriverManager` looks for file `META-INF/services/java.sql.Driver`;
    *   The file should contain path to the implementation class of
        `java.sql.Driver`.
    *   You can find this file in the MySQL j-connector jar.
*   For MySQL, the file contains `com.mysql.cj.jdbc.Driver`;
*   `com.mysql.cj.jdbc.Driver` is loaded through `ServiceLoader`;
*   Which executes its static block, registering self to `DriverManager`:

```java
/* Static block of com.mysql.cj.jdbc.Driver, with minor format alterations */

// Register ourselves with the DriverManager
static
{
	try { java.sql.DriverManager.registerDriver(new Driver()); }
	catch (SQLException e) { throw new RuntimeException("Can't register driver!"); }
}
```

<!-- TODO: add more clarification about SPI -->

### Get connection

At this step we connect JVM process to the database server.
This is done by calling `DriverManager.getConnection(String url, String usr, String pass): Connection`
and passing in URL, username and password;
`DriverManager` will try to select an appropriate one
from its registered drivers to establish a connection to the URL;
returns a `Connection` instance to the URL on success,
coming to usage in the following steps.

Note that this method also throws `SQLException`, so include it
in catch statement:

```java
try
{
	Connection conn = 
		DriverManager.getConnection("jdbc:mysql://localhost:3306/dbname", "username", "pass");
}
catch (SQLException e) { e.printStackTrace(); }
```

For flexibility and confidential concerns, putting connecting
information inside source code is highly discouraged;
properties files are more intensely used for
reading and storing them:

```java
ResourceBundle resourceBundle = ResourceBundle.getBundle("Info");
String url = resourceBundle.getString("url");
String username = resourceBundle.getString("username");
String password = resourceBundle.getString("password");

try
{
	Connection conn = 
		DriverManager.getConnection(url, username, password);
}
catch (SQLException e) { e.printStackTrace(); }
```

As a resource, `conn` should be `close`d after usage;
Hence adding a `finally` statement to `try-catch`
while moving `conn` out of `try` block.
Note that closing throws `SQLException`:

```java
Connection conn = null;

try ...
catch ...
finally
{
	if (conn != null)
	{
		try { conn.close(); }
		catch (SQLException e) { e.printStackTrace(); }
	}
}
```

An alterative approach is `try`-with-resources:

```java
String sampleURL = "jdbc:mysql://localhost:3306/dbname";
String username = "root";
String password = "pass";

try (Connection conn =                                              )
		DriverManager.getConnection(sampleURL, username, password))
{...}
catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); }
```

#### About database URL

A URL consists of 4 parts. Speaking of database servers,
they correspond to:

| URL 				| Database server        |
|-------------------|------------------------|
| communication protocol | database protocol |
| server IP         | database server IP 	 |
| port 				| port 			  		 |
| resource name 	| database name   		 |

In URL `jdbc:mysql://localhost:3306/dbname`:

| Part | Meaning |
|-|-|
| `jdbc:mysql` | database communication protocol `jbdc:mysql` |
| `localhost` | database server IP, where `localhost` or `127.0.0.1` refer to the current device |
| `3306` | port 3306 (default port for MySQL) |
| `dbname` | the database resource to be used |

### Fetch statement object

At this step a `Statement` instance is created,
used for executing a static SQL statement and returning the results it produces.

`Statement`s are created upon `Connection`s.
To create a `Statement`, call `Connection.createStatement(): Statement`:

```java
Statement stmt = conn.createStatement();
```

Note that `Statement` is also a resource and must be closed after use.

If you're on `finally` block, remember to close each resource individually
so exceptions in closing some resources do not affect closing of others:

```java
Connection conn = null;
Statement stmt = null;

try ...
catch ...
finally
{
	if (conn != null)
	{
		try { conn.close(); }
		catch (SQLException e) { e.printStackTrace(); }
	}

	if (stmt != null)
	{
		try { conn.close(); }
		catch (SQLException e) { e.printStackTrace(); }
	}
}
```

If you're on `try`-with-resources:

```java
try
	(
		Connection conn = DriverManager.getConnection(url, username, password);
		Statement stmt = conn.createStatement()
	)
```

### Execute SQL statements

`Statement` provides a number of methods to execute SQL statements,
as tabulated:

| Method                     | Usage               |
|----------------------------|---------------------|
| `execute: boolean`         | Universal           |
| `executeQuery: ResultSet`  | Queries (DQL)       |
| `executeUpdate: int`       | Updates (DML + DDL) |
| `executeBatch` and related | Batch update        |

I'm not going to dig further into these methods which turn out
to be a complete copypasta of
[java API specification on `Statement`](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/Statement.html#execute\(java.lang.String\));
Instead I'll throw some examples here:

*   Querying

```java
String sql = """
	SELECT                                                                                                           
		*
	FROM
		`table_name`
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
		String value = res.getString("field_name");
		System.out.printf("%s\n", value);
	}
}
catch (SQLException e) { e.printStackTrace(); }
```

*   Inserting

```java
String sql = """
	INSERT INTO
		`table_name`
		(`field1`, `field2`, `field3`)
	VALUES
		(value1, value2, value3)
	"""

try
	(
		Connection conn = DriverManager.getConnection(url, username, password);
		Statement stmt = conn.createStatement()
	)
{
	int rowsAffected = stmt.executeUpdate(sql);
	System.out.printf("%d rows affected\n", rowsAffected);
}
catch (SQLException e) { e.printStackTrace(); }
```

Remember to close all `Closable` resources, including `ResultSet`.

### Analyse result

You should get a `ResultSet` from successful DQL queries.
`ResultSet` is like an iterator iterating through
each record of the querying result:

*   `next()` moves a `ResultSet` to the next record, and returns
    boolean based on successfulness;
*   `getByte, getBoolean, getDate...`
    retrieve value of a particular field for the current record,
    given its column index (`int`) or column label (`String`).
    *   Considering robustness, use of label is preferred.
    *   When a column is aliased, use its alias as label.
    *   `getString` retrieves value of a field **as** a `String`,
        regardless of its original type.
    *   **NOTE**: column indexes are **1-based**.

Again, check [Java API specification on `ResultSet`](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/ResultSet.html)
for comprehensive layout of methods.

### Release closable resources

The best practice is to close a resource in finally block
once after you create one; or use `try`-with-resources statement,
where java will take care of resources for you.

Again, resources must be closed **individually**.

The order of closing does not really matter
but is recommended to be on the last-in-first-out basis (LIFO)
i.e. the reverse order of opening them.
