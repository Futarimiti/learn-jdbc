# Injection

### Concept

From Wikipedia:

> Code injection is the exploitation of a computer bug that is caused by processing invalid data. The injection is used by an attacker to introduce (or "inject") code into a vulnerable computer program and change the course of execution. The result of successful code injection can be disastrous, for example, by allowing computer viruses or computer worms to propagate.
>
> ...
> Injection can result in data loss or corruption, lack of accountability, or denial of access. Injection can sometimes lead to complete host takeover.

And for SQL injections:

> SQL injection is a code injection technique used to attack data-driven applications, in which malicious SQL statements are inserted into an entry field for execution (e.g. to dump the database contents to the attacker). SQL injection must exploit a security vulnerability in an application's software, for example, when user input is either incorrectly filtered for string literal escape characters embedded in SQL statements or user input is not strongly typed and unexpectedly executed. SQL injection is mostly known as an attack vector for websites but can be used to attack any type of SQL database.

### Example

Suppose we are on an application with a user login
info checker which looks into a user table to see if such a record
with the entered username and password exists.

Not considering injections, we can write the following method
where username and password are concatenated into a full query:

```java
private static boolean login(String username, String password)
{
	boolean res = false;

	try
		(
			Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM t_user WHERE username = '" + username + "' AND password = '" + password + "'")
		)
	{
		res = resultSet.next();
	}
	catch (SQLException e) { e.printStackTrace(); }

	return res;
}
```

When the user enters correct login information,
a `ResultSet` should be returned with with *a* field inside,
otherwise empty; hence here we use `next()` to determine login status.
*Note that we did not check number of results.*

Simply concatenating the value into the query statement
leaves vulnerability for hackers to inject SQL code.
For example, entering the following for username and password
could result in the following statement:

```sql
username: ' or '1' = '1
password: ' or '1' = '1'; DROP TABLE t_user; '

SELECT * FROM t_user WHERE username = '' or '1' = '1' AND password = '' or '1' = '1'; DROP TABLE t_user; ''
```

Given `AND` is prior to `OR`,
This completely bypassed the checker as and also dropped the table.
(the table may survive if method `executeQuery` is used instead of
`execute`)

### Avoiding injection: parameterised `PreparedStatement`

In prevention, **parameterised `PreparedStatement`** may be
used in place of `Statement`.

In parameterised `PreparedStatement`,
query values are passed to `WHERE` or `HAVING` clause in
a pre-defined query template.

The template uses `?` as parameter placeholders:

```sql
SELECT * FROM `t_user` WHERE `username` = ? AND `password` = ?
```

Use method `Connection.prepareStatement` and pass in the
template to get the `PreparedStatement`.
`PreparedStatement` is also auto-closable, so remember to close it:

```java
try
	(
		Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
		PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM `t_user` WHERE `username` = ? AND `password` = ?");
	)
```

Note that we do not need to manually put a pair of quotes
around `?`; java will do that when it knows a string is passed.

Use method `PreparedStatement.set[Type](int n, ...)`
to set a value for nth placeholder.
Note that in JDBC parameter index always start from 1:

```java
try (...)
{
	pstmt.setString(1, username);
	pstmt.setString(2, password);
}
catch (SQLException e) { e.printStackTrace(); }
```

`set[Type]` does throw `SQLException`, as eveything does in JDBC.

Finally, call `PreparedStatement.executeQuery()` to execute query:

```java
try (...)
{
	pstmt.setString(1, username);
	pstmt.setString(2, password);

	try (ResultSet rs = pstmt.executeQuery())
	{
		res = rs.next();
	}
}
catch (SQLException e) { e.printStackTrace(); }
```

In other situations, use `execute` or `executeUpdate`
whichever is more appropriate.

Injection is hence prevented as everything has been properly
escaped to make sure the string has been passed literally as a value,
not a piece of code.

### Why `Statement` then?

Although being more robust,
`PreparedStatement` cannot do eveything:

*   `?` cannot be a placeholder for commands, only values,
    as every string insertion is literal;
*   `?` is only usable in `WHERE` and `HAVING` clauses,
    and cannot hold, e.g., name of a table in `FROM` clause.

This is where we bring back the good old
`Statement` and string concatenation;
But still, cut down every possibility for injection.
