# Java Multi-Threaded Terminal Chat Application

A real-time chat application built using Java Socket Programming and Multithreading. The application allows multiple clients to communicate simultaneously through public chats, private messages, and group conversations directly from the terminal.

---

## Features

### Real-Time Messaging

* Multiple clients can connect simultaneously.
* Instant communication using TCP sockets.
* Concurrent message handling through multithreading.

### Public Chat

* Broadcast messages to all connected users.
* Real-time message delivery.

### Private Messaging

* Direct user-to-user communication.
* Username validation before message delivery.

### Group Chats

* Create custom chat groups.
* Add multiple members to groups.
* Send messages within a specific group.
* View group message history.

### Chat History

* Stores recent messages.
* Retrieve previous conversations using commands.

### Colorful Terminal Interface

* ANSI color-coded messages.
* Colored usernames.
* Styled system notifications.

### Multi-Threaded Architecture

* Dedicated thread for every connected client.
* Supports multiple active users concurrently.

---

## Tech Stack

* Java 21
* TCP Sockets
* Multithreading
* ExecutorService
* Concurrent Collections
* Object-Oriented Programming

---

## Project Structure

```text
src/
├── ChatServer.java
├── Client.java
├── ClientHandler.java
└── Group.java
```

### Components

#### ChatServer

* Accepts client connections
* Manages active chat sessions
* Handles message broadcasting
* Maintains chat history
* Manages chat groups

#### Client

* Connects to server
* Sends commands and messages
* Receives messages asynchronously
* Displays terminal UI

#### ClientHandler

* Handles individual client connections
* Processes commands
* Routes messages
* Maintains client sessions

#### Group

* Stores group members
* Maintains group messages
* Supports group communication

---

## Supported Commands

| Command                       | Description               |
| ----------------------------- | ------------------------- |
| `/dm <username> <message>`    | Send private message      |
| `/bc <message>`               | Broadcast message         |
| `/history`                    | View recent messages      |
| `/gc <groupname> <users>`     | Create a new group        |
| `/GM <groupname> <message>`   | Send a message to a group |
| `/group messages <groupname>` | View group chat history   |
| `/help`                       | Show available commands   |
| `/quit`                       | Exit the application      |

---

## System Architecture

```text
                    ┌─────────────┐
                    │ Chat Server │
                    └──────┬──────┘
                           │
       ┌───────────────────┼───────────────────┐
       │                   │                   │
   Client 1           Client 2           Client 3
       │                   │                   │
       └────── ClientHandler Threads ─────────┘
```

Each client connection is handled by a separate thread, enabling multiple users to communicate simultaneously without blocking other connections.

---

## Running the Application

### Compile

```bash
javac *.java
```

### Start Server

```bash
java ChatServer
```

### Start Clients

Open separate terminal windows:

```bash
java Client
```

---

## Example

```text
Tommy > /bc Hello everyone!

[#1] [10:30] Tommy → all: Hello everyone!

Tommy > /dm Alex Hi!

[#2] [10:31] Tommy → Alex: Hi!

Tommy > /gc StudyGroup Alex Sarah

Tommy > /GM StudyGroup Exam starts tomorrow!
```

---

## Concepts Demonstrated

* Socket Programming
* TCP Networking
* Client-Server Architecture
* Concurrent Programming
* Multithreading
* ExecutorService
* Thread-Safe Collections
* Command Parsing
* Real-Time Communication Systems

---

## Future Improvements

* Persistent database storage
* Authentication system
* End-to-end encryption
* File sharing
* Message reactions
* JavaFX GUI
* WebSocket-based version

---

## Author

Built to explore networking, concurrent programming, and real-time communication systems using Java.
