# Tic Tac Toe Server

A robust JavaFX-based server application for managing multiplayer Tic Tac Toe games. This server handles client connections, user authentication, game sessions, and player challenges with a modern graphical user interface.

## 🎯 Features

- **Multi-client Support**: Handles multiple concurrent client connections using thread pooling
- **User Management**: Complete authentication system with user registration and login
- **Game Session Management**: Manages active game sessions between players
- **Challenge System**: Players can challenge each other to games
- **Database Integration**: Apache Derby database for persistent user data and scores
- **JavaFX GUI**: Modern server administration interface
- **Score Tracking**: Tracks and updates player scores
- **Online User Monitoring**: Real-time tracking of online users

## 🛠️ Technology Stack

- **Java 11**: Core programming language
- **JavaFX 13**: GUI framework for the server interface
- **Apache Derby 10.9.1.0**: Embedded database for data persistence
- **Maven**: Build and dependency management
- **JSON**: Data serialization and communication protocol
- **Socket Programming**: Network communication layer

## 📋 Prerequisites

Before running the server, ensure you have:

- Java Development Kit (JDK) 11 or higher
- Maven 3.6 or higher
- Apache Derby database server running on `localhost:1527`
- Database named `tic_tac_toe` with credentials:
  - Username: `root`
  - Password: `root`

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Server
```

### 2. Database Setup

1. Start Apache Derby Network Server on port 1527
2. Create a database named `tic_tac_toe`
3. The server will automatically create the required tables on first run

The database schema includes:

- **users** table: Stores user credentials and scores
  - `id` (INT, Primary Key, Auto-increment)
  - `username` (VARCHAR(50), Unique)
  - `email` (VARCHAR(100))
  - `password` (VARCHAR(255))
  - `score` (INT, Default: 0)

### 3. Build the Project

```bash
cd Server
mvn clean install
```

This will:

- Compile the source code
- Run tests
- Create an executable JAR file
- Generate a Windows executable (.exe) using Launch4j

## 🎮 Running the Server

### Option 1: Using Maven

```bash
mvn javafx:run
```

### Option 2: Using the JAR File

```bash
java -jar target/Tic\ Tac\ Toe\ server.jar
```

### Option 3: Using the Windows Executable

```bash
target/Tic\ Tac\ Toe\ server.exe
```

### Option 4: Debug Mode

```bash
mvn javafx:run@debug
```

This starts the server with debug agent on port 8000.

## 📁 Project Structure

```
Server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mycompany/server/
│   │   │       ├── App.java                    # Main JavaFX application
│   │   │       ├── Launcher.java               # Application entry point
│   │   │       ├── db/
│   │   │       │   └── DatabaseManager.java    # Database operations
│   │   │       ├── manager/
│   │   │       │   ├── ChallengeManager.java   # Challenge handling
│   │   │       │   ├── GameSessionManager.java # Game session management
│   │   │       │   └── OnlineUsersManager.java # Online user tracking
│   │   │       ├── model/
│   │   │       │   ├── Challenge.java          # Challenge data model
│   │   │       │   ├── GameSession.java        # Game session model
│   │   │       │   └── OnlineUser.java         # User model
│   │   │       ├── network/
│   │   │       │   ├── ClientHandler.java      # Client connection handler
│   │   │       │   └── SocketServer.java       # Socket server
│   │   │       ├── service/
│   │   │       │   ├── AuthService.java        # Authentication service
│   │   │       │   └── ChallengeService.java   # Challenge service
│   │   │       └── ServerUi/
│   │   │           └── ServeruiController.java # UI controller
│   │   └── resources/
│   │       └── com/mycompany/server/
│   │           ├── *.fxml                      # FXML layout files
│   │           └── assets/                     # Images and icons
│   └── test/
├── pom.xml                                     # Maven configuration
└── README.md
```

## 🔧 Configuration

### Server Port

The server runs on port **5000** by default. To change this, modify the `PORT` constant in `SocketServer.java`:

```java
private static final int PORT = 5000;
```

### Database Configuration

Database connection settings can be modified in `DatabaseManager.java`:

```java
private static final String DB_URL = "jdbc:derby://localhost:1527/tic_tac_toe;create=false";
private static final String USER = "root";
private static final String PASS = "root";
```

## 📡 Server Architecture

### Network Layer

- **SocketServer**: Manages the server socket and accepts client connections
- **ClientHandler**: Handles individual client communication in separate threads
- Uses ExecutorService with cached thread pool for efficient resource management

### Service Layer

- **AuthService**: Handles user authentication and registration
- **ChallengeService**: Manages game challenges between players

### Manager Layer

- **OnlineUsersManager**: Tracks currently connected users
- **GameSessionManager**: Manages active game sessions
- **ChallengeManager**: Handles challenge requests and responses

### Data Layer

- **DatabaseManager**: Singleton pattern for database connections and operations
- Automatic table creation on first run
- Connection pooling for efficient database access

## 🎨 User Interface

The server includes a JavaFX-based administration interface that provides:

- Server start/stop controls
- Real-time online user count
- Total registered users display
- Server status monitoring

## 🔒 Security Considerations

⚠️ **Important**: This is a development/educational project. For production use, consider:

- Implementing password hashing (currently passwords may be stored in plain text)
- Adding SSL/TLS encryption for network communication
- Implementing rate limiting and DDoS protection
- Adding input validation and sanitization
- Implementing proper session management
- Adding logging and monitoring

## 🐛 Troubleshooting

### Server won't start

- Ensure port 5000 is not already in use
- Check that Java 11+ is installed: `java -version`
- Verify Derby database is running and accessible

### Database connection errors

- Confirm Derby server is running on localhost:1527
- Verify database credentials are correct
- Check that the `tic_tac_toe` database exists

### Build errors

- Clean and rebuild: `mvn clean install`
- Ensure Maven is properly installed: `mvn -version`
- Check internet connection for dependency downloads

## 📝 Development

### Running Tests

```bash
mvn test
```

### Creating a Production Build

```bash
mvn clean package
```

This creates:

- `target/Tic Tac Toe server.jar` - Executable JAR with all dependencies
- `target/Tic Tac Toe server.exe` - Windows executable

### IDE Setup

The project includes NetBeans configuration files (`nbactions.xml`). For other IDEs:

**IntelliJ IDEA**:

1. Import as Maven project
2. Set JDK to version 11+
3. Enable JavaFX support

**Eclipse**:

1. Import as existing Maven project
2. Install e(fx)clipse plugin
3. Configure Java 11+ JDK

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is part of an educational/training program.

## 👥 Authors

- **Amro Mohamed Ali Mahmoud**
- **Eslam Ehab Mohamed Lotfy**
- **Ahmed Khaled Mahmoud**
- **Antoneos Philip Samir**
- **Mohamed Ali Abdelfattah**

## 🙏 Acknowledgments

- JavaFX community for excellent documentation
- Apache Derby team for the embedded database solution
- Maven community for build tool support

---

**Note**: This server is designed to work with a corresponding Tic Tac Toe client application. Ensure both server and client are compatible versions.
