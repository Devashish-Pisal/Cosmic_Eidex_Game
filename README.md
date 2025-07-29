# Cosmic Eidex

A multiplayer, client-server card game based on the traditional Swiss game "Eidex". This project features a JavaFX GUI, bot integration, and a robust server architecture for real-time gameplay.

## Table of Contents
- [Description](#description)
- [Features](#features)
- [Architecture](#architecture)
- [Setup](#setup)
- [Usage](#usage)
- [Technologies](#technologies)
- [About](#about)
- [Contributors](#contributors)

---

## Description
Cosmic Eidex is a digital implementation of the classic Swiss card game "Eidex". The game supports multiplayer matches (up to 3 players per room), bot opponents, and a persistent leaderboard. The project is designed for both local and networked play, making use of Java's socket programming and JavaFX for a modern user interface.

## Features
- Multiplayer support (client-server architecture)
- Play against human players and/or bots (EasyBot, HardBot)
- JavaFX-based graphical user interface
- User authentication and registration
- Game rooms (create, join, leave)
- Waiting room and in-game chat
- Persistent leaderboard and statistics (SQLite)
- Custom game logic for Eidex (trick-taking, trump, etc.)
- Thread-safe server with delayed bot moves

## Architecture
### High-Level Overview
- **Client-Server Model:** The server manages game logic, rooms, and user data. Clients connect via TCP sockets.
- **MVC Pattern:**
    - **Model:** GameSession, Player, Card, User, etc.
    - **View:** JavaFX FXML files for all screens (Login, Lobby, Game Room, etc.)
    - **Controller:** JavaFX controllers for UI, plus server-side controllers for networking and game management.
- **Bot Integration:** Bots act as players and can play automatically with a configurable delay.
- **Database:** SQLite for user data and leaderboard.

### Main Components
- `src/main/java/com/group06/cosmiceidex/game/` — Game logic and models
- `src/main/java/com/group06/cosmiceidex/controllers/` — JavaFX UI controllers
- `src/main/java/com/group06/cosmiceidex/server/` — Server, session, and database management
- `src/main/java/com/group06/cosmiceidex/client/` — Client logic
- `src/main/resources/com/group06/cosmiceidex/FXMLFiles/` — UI layouts

## Setup
### Prerequisites
- Java 17 or higher
- SQLite for persistent storage

### Build & Run
1. **Locate the JAR files:**
    ```bash
    The pre-built JAR files are located in the `Product` folder.
    ```
2. **Start the server:**
   ```bash
   To start the server, read README.txt file from ../Product/JAR_Files/Server_JAR/ 
   ```
3. **Start a client (in a new terminal):**
   ```bash
   To start the client, read README.txt file from ../Product/JAR_Files/Client_JAR/
   ```
4. **(Optional) Run tests:**
    - If you want to run tests, use Maven inside the project directory:
   ```bash
   mvn test
   ```

## Usage
- Register or log in as a user.
- Create or join a game room.
- Add bots or wait for other players.
- Start the game and play rounds of Eidex.
- View statistics and leaderboard.

## Technologies
- Java 17+
- JavaFX
- Maven
- SQLite
- Sockets (TCP)
- JUnit (for testing)


## About
This project was developed as part of the **Software Engineering Project (SEP)** course at **RPTU Kaiserslautern** during the Summer Semester 2025. It serves as a comprehensive demonstration of software engineering principles, including client-server architecture, design patterns, and collaborative development.

## Contributors
- [Tim Brombacher](https://github.com/Tim-b0)
- [Niklas Brühl](https://github.com/NexoBee)
- [Oliver Thull](https://github.com/unbenutzterName)
- [Ruslan Sidukov](https://github.com/ruslanSidukov)
- [Devashish Pisal](https://github.com/Devashish-Pisal)

---

For questions or issues, please contact the repository owner or open an issue on GitHub. 