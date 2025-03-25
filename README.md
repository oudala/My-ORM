# MyORM - Lightweight Java ORM 🚀

MyORM is a lightweight, easy-to-use Object-Relational Mapping (ORM) library designed to simplify Java database operations. It provides automatic CRUD operations, caching, query building, and more to make database interaction seamless and efficient.

## 🔥 Features
- ✨ Simple and intuitive API
- 🔄 Automatic CRUD operations
- 🏃 Connection pooling
- 💾 Caching system
- 📝 Query builder
- 🔄 Transaction management
- 📦 Batch operations support

## 📦 Installation

To add MyORM to your project, use one of the following methods:

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.oudala</groupId>
    <artifactId>My-ORM</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.oudala:My-ORM:v1.0.0'
}
```

## 🚀 Quick Start

Here's how to get started with MyORM:

1. **Configure Database Connection**
```java
// Initialize the Database Manager with your connection details
DatabaseManager.initialize("jdbc:mysql://localhost:3306/mydb", "user", "pass");
```

2. **Define Your Entity**
Create a model class and annotate it with `@Table` and `@Column` annotations:
```java
@Table(name = "users")
public class User {
    @Column(name = "id", primaryKey = true)
    private int id;
    
    @Column(name = "name")
    private String name;

    // Getters and setters...
}
```

3. **CRUD Operations**
Perform basic CRUD operations:
```java
// Create a new user
User user = new User();
user.setName("John Doe");z
CRUDManager.insert(user);

// Fetch users from the database
List<User> users = CRUDManager.select(User.class);

// Update a user
user.setName("Jane Doe");
CRUDManager.update(user);

// Delete a user
CRUDManager.delete(user);
```

4. **Use the Query Builder**
Build advanced queries with filtering, ordering, and limiting results:
```java
List<User> users = new QueryBuilder<>(User.class)
    .where("name", "LIKE", "John%")
    .orderBy("id", true)  // Ascending order
    .limit(10)
    .execute();
```

## 📚 Documentation
For detailed documentation, including setup guides, API reference, and advanced usage, visit our [Wiki](wiki/).

## 🤝 Contributing
We welcome contributions! If you would like to contribute, please fork the repository, make your changes, and submit a pull request. For guidelines, check out our [Contributing Guidelines](CONTRIBUTING.md).

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. You are free to use, modify, and distribute this project, but please include the original license in your distributed copies.
