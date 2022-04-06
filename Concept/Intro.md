# Java Database Connectivity (JDBC)

JDBC is an API for a client to access a database.
It comes with JDK, under package `java.sql`.

Every database implements this API from interfaces to classes;
if you download and unzip a connector driver, you'll see many
class files as implementation to the API.

Programmers uses this API, while different databases provides
different implementation to the API by their needs,
makeing JDBC universal to databases.

As a programmer, we only need to learn how to use the JDBC API
and not to worry about how are they implemented by different databases.
Before we begin, we do need to find these implementing classes for what database
we are using.

### Steps of JBDC

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
