# MySQLAdapter
Ein wahrscheinlich ziemlich peinlicher MySQL-Adapter für unsere Bukkit-Plugins

Continuous integration:
------
* Build-Server: [ci.craft-together.de](https://ci.craft-together.de/)
* Maven Repo:
```xml
<repository>
    <id>ctogether</id>
    <url>https://repo.craft-together.de/</url>
</repository>
```
* Artifact:
```xml
<dependency>
    <groupId>de.crafttogether</groupId>
    <artifactId>MySQLAdapter</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
 ```

Setup:
------
**1. Imports**

```java
import de.crafttogether.mysql.MySQLAdapter;
import de.crafttogether.mysql.MySQLConfig;
import de.crafttogether.mysql.MySQLConnection;
```

**2. Create an instance of MySQLConfig:**
```java
MySQLConfig myCfg = new MySQLConfig();
myCfg.setHost("127.0.0.1");
myCfg.setPort(3306);
myCfg.setUsername("root");
myCfg.setPassword("");
myCfg.setDatabase("db");
myCfg.setTablePrefix("tb_");
```

**3. Validate Inputs:**
```java
if (!myCfg.checkInputs() || myCfg.getDatabase() == null) {
    getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
    getServer().getPluginManager().disablePlugin(this);
    return;
}
```

**4. Create an instance of MySQLAdapter:**
```java
MySQLAdapter mysqlAdapter = new MySQLAdapter(this, myCfg);
```

**5. Create a new connection:**
```java
// Get from instance
MySQLConnection connection = mysqlAdapter.getConnection();

// Static access
MySQLConnection connection = MySQLAdapter.getConnection();

// Close connection
connection.close();
```

**6. Shutdown MySQLAdapter:**
```java
mysqlAdapter.disconnect();
```

Usage:
------
* ### Insert:
**Sync:**
```java
MySQLConnection connection = MySQLAdapter.getConnection();

String name = "Bob";
int age = 18;

try {
    int personId = connection.insert("INSERT INTO `%spersons` " +
    "(" +
        "`name`, " +
        "`age`" +
    ") " +
    
    "VALUES (" +
        "'%s', " +
        "%d" +
    ");", connection.getTablePrefix(), name, age);
}

catch (SQLException ex) {
    ex.printStackTrace();
}

finally {
    connection.close(); // Close preparedStatement (if set), resultSet (if set) & connection
}
``` 
**Async:**
```java
MySQLConnection connection = MySQLAdapter.getConnection();

String name = "Bob";
int age = 18;

connection.insertAsync("INSERT INTO `%spersons` " +
"(" +
    "`name`, " +
    "`age`" +
") " +

"VALUES (" +
    "'%s', " +
    "%d" +
");",

// Process Result
(err, lastInsertedId) -> {
    if (err != null)
        err.printStackTrace();
    
    int personId = lastInsertedId;
    
    connection.close(); // Close preparedStatement (if set), resultSet (if set) & connection
}, connection.getTablePrefix(), name, age);
``` 
* ### Query
**Sync:**
```java
MySQLConnection connection = MySQLAdapter.getConnection();

try {
    ResultSet result = connection.query("SELECT * FROM `%spersons`", connection.getTablePrefix());
    
    // Process Results
    while (result.next()) {
        int id = result.getInt("id");
        String name = result.getString("name");
        int age = result.getInt("age");
        
        System.out.println("User #" + id + " (" + name + ") is " + age + " years old.");
    }
}

catch (SQLException ex) {
    ex.printStackTrace();
}

finally {
    connection.close(); // Close preparedStatement (if set), resultSet (if set) & connection
}
``` 
**Async:**
```java
MySQLConnection connection = MySQLAdapter.getConnection();

connection.queryAsync("SELECT * FROM `%spersons`", (err, result) -> {
    if (err != null)
        err.printStackTrace();
    
    // Process Results
    try {
        while (result.next()) {
            int age = result.getInt("id");
            String name = result.getString("name");
        int age = result.getInt("age");
            
            System.out.println("User #" + id + " (" + name + ") is " + age + " years old.");
        }
    }
    
    catch (SQLException ex) {
        ex.printStackTrace();
    }
    
    finally {
        connection.close(); // Close preparedStatement (if set), resultSet (if set) & connection
    }
}, connection.getTablePrefix());
``` 
* ### Update
**Sync:**
```java
MySQLConnection connection = MySQLAdapter.getConnection();

int userId = 1;
String name = "Bob";
int age = 12;

int affectedRows = 0;

try {
    affectedRows = connection.update("UPDATE `%spersons` SET " +
        "`name` = '%s', " +
        "`age`  = %d, " +
    "WHERE `id` = %d;", connection.getTablePrefix(), name, age, userId);
}

catch (SQLException ex) {
    ex.printStackTrace();
}

finally {
    System.out.println(affectedRows  + " where updated.");
    connection.close(); // Close preparedStatement (if set), resultSet (if set) & connection
}
``` 
**Async:**
```java
MySQLConnection connection = MySQLAdapter.getConnection();

int userId = 1;
String name = "Bob";
int age = 12;

connection.updateAsync("UPDATE `%spersons` SET " +
    "`name` = '%s', " +
    "`age`  = %d, " +
"WHERE `id` = %d;",

(err, affectedRows) -> {
    if (err != null)
        err.printStackTrace();

    System.out.println(affectedRows  + " where updated.");
    connection.close(); // Close preparedStatement (if set), resultSet (if set) & connection
}, connection.getTablePrefix(), name, age, userId);
``` 
* ### Execute
**Sync:**
```java
MySQLConnection connection = MySQLAdapter.getConnection();

String statement =
"""
    CREATE TABLE `%spersons` (
      `id` int(11) NOT NULL,
      `name` varchar(255) NOT NULL,
      `age` int(4) NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
                
    ALTER TABLE `%spersons`
      ADD PRIMARY KEY (`id`);
                
    ALTER TABLE `%spersons`
      MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
    COMMIT;
""";

boolean success = false;
try {
    success = connection.execute(statement, connection.getTablePrefix());
}

catch (SQLException ex) {
    ex.printStackTrace();
}

finally {
    if (success)
        System.out.println("Statement executed successfully.");

    connection.close(); // Close preparedStatement (if set), resultSet (if set) & connection
}
``` 
**Async:**
```java
MySQLConnection connection = MySQLAdapter.getConnection();

String statement =
"""
    CREATE TABLE `%spersons` (
      `id` int(11) NOT NULL,
      `name` varchar(255) NOT NULL,
      `age` int(4) NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
                
    ALTER TABLE `%spersons`
      ADD PRIMARY KEY (`id`);
                
    ALTER TABLE `%spersons`
      MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
    COMMIT;
""";

connection.executeAsync(statement, (err, success) -> {
    if (err != null)
        err.printStackTrace();

    if (success)
        System.out.println("Statement executed successfully.");

    connection.close(); // Close preparedStatement (if set), resultSet (if set) & connection
}, connection.getTablePrefix());
``` 