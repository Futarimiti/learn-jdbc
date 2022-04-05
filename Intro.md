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
