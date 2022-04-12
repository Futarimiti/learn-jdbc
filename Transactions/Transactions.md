# JDBC transactions

In JDBC, SQL statements are auto-committed after execution;
that is, committing is done everytime when a statement is
executed via JDBC.

To perform a business logic as concurrent executions,
you need disable this behaviour first, by calling
`Connection.setAutoCommit(boolean)` and pass in `false`:

```java
try (Connection conn = DriverManager.getConnection(url, usr, pass))
{
	conn.setAutoCommit(false);
}
catch (SQLException e) { e.printStackTrace(); }
```

Then you should manually commit at the end of the transaction,
by calling `Connection.commit()`:

```java
try (Connection conn = DriverManager.getConnection(url, usr, pass))
{
	conn.setAutoCommit(false);

	transactions...
	
	conn.commit();
}
catch (SQLException e) { e.printStackTrace(); }
```

If an exception stopped the current transaction
and did not reach `commit()`, then all changes should be `ROLLBACK`ed.
Hence call `Connection.rollback()` inside the catch block:

```java
try (Connection conn = DriverManager.getConnection(url, usr, pass))
{
	conn.setAutoCommit(false);

	transactions...
	
	conn.commit();
}
catch (SQLException e)
{
	e.printStackTrace();
	try { conn.rollback(); }
	catch (SQLException e1) { e1.printStackTrace(); }
}
catch (Exception exception)
{
	try { conn.rollback(); }
	catch (SQLException e1) { e1.printStackTrace(); }
}
```

Both commit and rollback must be included for manual commit,
otherwise the ending behaviour depends on the database.

There we have it; A bank transfer demo is included
for better understanding.
