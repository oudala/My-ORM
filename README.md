# MyORM - Lightweight Java ORM 🚀

A simple yet powerful Java ORM for seamless database operations.

## 🔥 Features
- ✨ Simple and intuitive API
- 🔄 Automatic CRUD operations
- 🏃 Connection pooling
- 💾 Caching system
- 📝 Query builder
- 🔄 Transaction management
- 📦 Batch operations support

## 📦 Installation

### Maven
```xml
<dependency>
    <groupId>com.myorm</groupId>
    <artifactId>myorm</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'com.myorm:myorm:1.0.0'
```

## 🚀 Quick Start

```java
// Configure database connection
DatabaseManager.initialize("jdbc:mysql://localhost:3306/mydb", "user", "pass");

// Define your entity
@Table(name = "users")
public class User {
    @Column(name = "id", primaryKey = true)
    private int id;
    
    @Column(name = "name")
    private String name;
}

// Use CRUD operations
User user = new User();
user.setName("John Doe");
CRUDManager.insert(user);

// Use Query Builder
List<User> users = new QueryBuilder<>(User.class)
    .where("name", "LIKE", "John%")
    .orderBy("id", true)
    .limit(10)
    .execute();
```

## 📚 Documentation
Visit our [Wiki](wiki/) for detailed documentation.

## 🤝 Contributing
Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md).

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
