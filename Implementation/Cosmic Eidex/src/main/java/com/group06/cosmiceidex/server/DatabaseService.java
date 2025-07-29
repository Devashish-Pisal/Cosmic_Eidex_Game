package com.group06.cosmiceidex.server;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.net.URISyntaxException;
import java.net.URL;

import com.group06.cosmiceidex.common.LeaderboardEntry;
import com.group06.cosmiceidex.common.User;
import com.group06.cosmiceidex.exceptions.AuthException;
//import com.mysql.cj.protocol.ResultSet;;


/**
 * Für den kompletten Zugriff auf die Datenbank zuständig. 
 * @author O.T.
 */
public class DatabaseService {

    String projectPath;
    String pathToDB;
    String pathToScript;
    private static DatabaseService databaseService;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("[ERROR] [DatabaseService] Unable to find org.sqlite.JDBC");
        }
    }


    /**
     * Konstruktor
     * private weil Objekt als Singleton erstellt werden soll.
     * @author O.T.
     */
    private DatabaseService() {
        ensureDatabaseExists();
    }

    /** 
     * Erstellt Singleton
     * @return DatabaseService
     * @author O.T.
     */
    public static DatabaseService getDatabaseServiceInstance(){
        if (databaseService == null){
            databaseService = new DatabaseService();
        }
        return databaseService;
    }

    /**
     * @param path
     * @author O.T.
     */
    public void setPathToDB(String path) {
        this.pathToDB = path;
    }

    /**
     * @return pathToDB
     * @author O.T.
     */
    public String getPathToDB() {
        return pathToDB;
    }

    /**
     * @param path
     * @author O.T.
     */
    public void setPathToScript(String path) {
        this.pathToScript = path;
    }

    /**
     * @return pathToDB
     * @author O.T.
     */
    public String getPathToScript() {
        return pathToScript;
    }

    /**
     * Testet ob die Datenbank erreichbar ist. Wenn nicht wird sie mit allen Tabellen angelegt
     * @author O.T.
     */
    private void ensureDatabaseExists() {
        this.findPathToDB();
        this.findPathToScript();
        File database = new File(this.pathToDB);
        if (!database.exists()) {
            try {
                if (database.createNewFile()) {
                    System.out.println("[INFO] [DatabaseService] New Database successfully created at " + this.pathToDB);
                    this.runScript();
                }
            } catch (Exception e){
                System.out.println("[ERROR] [DatabaseService] Unable to create a new Database at location " + this.pathToDB);
                e.printStackTrace();
            }
        }
    }

    /** 
     * Erstellt Tabellen in Datenbank.
     * @throws Exception
     * @author O.T.
     */
    private void runScript() throws IOException, SQLException {
        var url = "jdbc:sqlite:" + this.pathToDB;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            String sqlScript;
            try (InputStream inputStream = getClass().getResourceAsStream("/com/group06/cosmiceidex/database/Scripts/Create_Tables.sql")) {
                if (inputStream == null) {
                    throw new IOException("SQL script not found in resources");
                }
                sqlScript = new String(inputStream.readAllBytes());
            }

            String[] sqlStatements = sqlScript.split(";");
            for (String sql : sqlStatements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
            System.out.println("[INFO] [DatabaseService] Database tables created successfully");
        }
    }

    /** 
     * Generiert den Pfad zur Datenbank.
     * @author O.T.
     */
    private void findPathToDB() throws IllegalStateException{
        // Create database in external location (user's home directory)
        String userHome = System.getProperty("user.home");
        String appDir = userHome + File.separator + "CosmicEidex";
        File dir = new File(appDir);

        // Create directory if it doesn't exist
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.out.println("[ERROR] [DatabaseService] Unable to create application directory: " + appDir);
                // Fallback to temp directory
                appDir = System.getProperty("java.io.tmpdir") + File.separator + "CosmicEidex";
                dir = new File(appDir);
                dir.mkdirs();
            }
        }

        this.pathToDB = appDir + File.separator + "Tables.db";
        System.out.println("[INFO] [DatabaseService] Database path: " + this.pathToDB);
    }

    /**
     * Generiert den Pfad zum Skript welches die Datenbank aufsetzt
     * wenn nicht vorhanden
     * @author O.T.
     */
    private void findPathToScript(){
        URL resourceUrl = getClass().getResource("/com/group06/cosmiceidex/database/Scripts/Create_Tables.sql");
        if (resourceUrl == null) {
            throw new IllegalStateException("SQL script not found in resources");
        }

        try {
            this.pathToScript = resourceUrl.toString();
        } catch (Exception e) {
            System.out.println("[ERROR] [DatabaseService] Unable to find SQL script.");
            e.printStackTrace();
            throw new IllegalStateException("Cannot locate SQL script", e);
        }
    }

    /** 
     * Fügt einen User in die Tabelle Users ein.
     * @param user
     * @param password
     * @throws AuthException
     * @author O.T.
     */
    public void registerUser(String user, String password) throws AuthException{
        var url = "jdbc:sqlite:" + pathToDB;
        String sql = "INSERT INTO Users(Name, Password) VALUES (?, ?)";
        if(user == null || password == null){
            throw new IllegalArgumentException ("Benutzername oder Passwort ist leer.");
        }
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setString(1, user);
                ps.setString(2, password);
                ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                System.out.println("[INFO] [DatabaseManager] Benutzername existiert bereits.");
                throw new AuthException("Benutzername ist bereits vergeben.");
            } else {
                System.out.println("[ERROR] [DatabaseService] Unable to insert User in Database.");
                throw new AuthException("Fehler beim Zugriff auf die Benutzerdaten: " + e.getMessage());
            }
        }
    }

    /**
     * Fügt Leader in Leaderboard ein wenn er nicht existiert und updated wenn er existiert.
     * @param id
     * @param wins
     * @param tricks
     * @param points
     * @throws SQLException
     * @author O.T.
     */
    public void upsertLeaderboard(int id, int wins, int tricks, int points) throws SQLException {
        var url = "jdbc:sqlite:" + pathToDB;
        if (id < 1) {
            throw new SQLException("ID muss größer 0 sein.");
        }
        if (wins < 0 || tricks < 0 || points < 0) {
            throw new SQLException("Einträge dürfen nicht kleiner 0 sein.");
        }
        //Upsert = Insert und Update in einem
        String sql =    "INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (?, ?, ?, ?) " + 
                        "ON CONFLICT(User_ID) " + 
                        "DO UPDATE SET Wins = excluded.Wins, Tricks = excluded.Tricks, Points = excluded.Points;";

        try (var conn = DriverManager.getConnection(url); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, wins);
            ps.setInt(3, tricks);
            ps.setInt(4, points);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Unable to UPSERT Leaderboard entry.");
            e.printStackTrace();
            throw e;
        }
    }

    /** 
     * Testet ob das eingegebene Passwort mit dem in der Datenbank übereinstimmt.
     * @param userName
     * @param password
     * @return boolean
     * @throws SQLException
     * @author O.T.
     */
    public User checkPassword(String userName, String password) throws SQLException{
        var url = "jdbc:sqlite:" + pathToDB;
        String sql =    "SELECT Password FROM Users " + 
                        "WHERE Name = ?";
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setString(1, userName);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    if(rs.getString("Password").equals(password)){
                        return new User(userName, password);
                    }else{
                        return null;
                        //throw new SQLException("Ungültige Passwort!");
                    }
                }else{
                    return null;
                    //throw new SQLException("Benutzer existiert nicht!");
                }
        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Unable to check Password.");
            e.printStackTrace();
            return null;
            //throw e;
        }
    }

    /** 
     * Gibt das vollständige Leaderboard zurück.
     * @return List<LeaderboardEntry>
     * @author O.T.
     */
    public List<LeaderboardEntry> getFullLeaderboard(){
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        var url = "jdbc:sqlite:" + pathToDB;
        String sql =    "SELECT Users.Name, Leaderboard.Wins, Leaderboard.Tricks, Leaderboard.points " + 
                        "FROM Users JOIN Leaderboard " + 
                        "ON Users.ID = Leaderboard.User_ID ORDER BY Wins DESC, Tricks DESC, Points DESC";
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement ps = conn.prepareStatement(sql);) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    LeaderboardEntry leader = new LeaderboardEntry(rs.getString("Name"), rs.getInt("Wins"), rs.getInt("Tricks"), rs.getInt("Points"));
                    leaderboard.add(leader);
                }
        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Unable to get complete leaderboard.");
        }
        return leaderboard;
    }

    /**
     * Gibt die ersten 10 Einträge aus dem Leaderboard zurück.
     * @return
     * @author O.T.
     */
    public List<LeaderboardEntry> getTopTen(){

        List<LeaderboardEntry> topEntries = new ArrayList<>();

        var url = "jdbc:sqlite:" + pathToDB;

        String query = "SELECT Users.Name, Leaderboard.Wins, Leaderboard.Tricks, Leaderboard.Points " +
                       "FROM Users JOIN Leaderboard ON Users.ID = Leaderboard.User_ID " +
                       "ORDER BY Wins DESC, Tricks DESC, Points DESC " +
                       "LIMIT 10";
        try (var conn = DriverManager.getConnection(url);
             var ps = conn.prepareStatement(query);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                LeaderboardEntry entry = new LeaderboardEntry(
                    rs.getString("Name"),
                    rs.getInt("Wins"),
                    rs.getInt("Tricks"),
                    rs.getInt("Points")
                );
                topEntries.add(entry);
            }

        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Unable to get leaderboard (Top 10) for user.");
            e.printStackTrace();
        }

        return topEntries;
    }


    /**
     * Suchen nach ID vom Benutzer in Datenbank anhand von Benutzername
     * @param username Benutzername
     * @return Falls gefunden, ID der Benutzer, sonst -1
     * @author Devashish Pisal
     */
    public int findIdOfUser(String username){
        var url = "jdbc:sqlite:" + pathToDB;
        String sql = "SELECT ID FROM Users WHERE Name = ?";
        try (var conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            int id = -1;
            if(rs.next()){
                id = rs.getInt("ID");
            }
            return id;
        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Something went wrong while searching for ID of User '" + username + "'.");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gewonnen Siegpunkte vom Benutzer in Datenbank suchen
     * @param id ID der Benutzer
     * @return Siegpunkte falls vorhanden, sonst -1
     * @author Devashish Pisal
     */
    public int findWinCount(int id){
        var url = "jdbc:sqlite:" + pathToDB;
        String sql = "SELECT Wins FROM Leaderboard WHERE User_ID = ?";
        try (var conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, String.valueOf(id));
            ResultSet rs = ps.executeQuery();
            int wins = -1;
            if(rs.next()){
                wins = rs.getInt("Wins");
            }
            return wins;
        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Something went wrong while searching for Wins of User with ID'" + id + "'.");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gewonnen Siegpunkte vom Benutzer in Datenbank suchen
     * @param id ID der Benutzer
     * @return Siegpunkte falls vorhanden, sonst -1
     * @author O.T.
     */
    public int findPointsCount(int id){
        var url = "jdbc:sqlite:" + pathToDB;
        String sql = "SELECT Points FROM Leaderboard WHERE User_ID = ?";
        try (var conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, String.valueOf(id));
            ResultSet rs = ps.executeQuery();
            int wins = -1;
            if(rs.next()){
                wins = rs.getInt("Points");
            }
            return wins;
        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Something went wrong while searching for Points of User with ID'" + id + "'.");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gewonnen Siegpunkte vom Benutzer in Datenbank suchen
     * @param id ID der Benutzer
     * @return Siegpunkte falls vorhanden, sonst -1
     * @author Devashish Pisal
     */
    public int findTrickCount(int id){
        var url = "jdbc:sqlite:" + pathToDB;
        String sql = "SELECT Tricks FROM Leaderboard WHERE User_ID = ?";
        try (var conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, String.valueOf(id));
            ResultSet rs = ps.executeQuery();
            int wins = -1;
            if(rs.next()){
                wins = rs.getInt("Tricks");
            }
            return wins;
        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Something went wrong while searching for Tricks of User with ID'" + id + "'.");
            e.printStackTrace();
            return -1;
        }
    }


    /** 
     * Ändert das Passwort eines Users.
     * @param id
     * @param newPW
     * @author O.T.
     */
    public void changePassword(Integer id, String newPW){
        var url = "jdbc:sqlite:" + pathToDB;
        String sql = "UPDATE Users SET Password = ? WHERE ID = ?";
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, newPW);
            ps.setInt(2, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("[INFO] [DatabaseService] No user found with ID: " + id);
            }
        } catch(SQLException e){
            System.out.println("[ERROR] [DatabaseService] Verbindung zu Datenbank fehlgeschlagen.");
        }
    }

    /** 
     * Ändert den Namen eines Users.
     * @param id
     * @param newUserName
     * @throws SQLException
     * @author O.T.
     */
    public void changeName(Integer id, String newUserName) throws SQLException{
        var url = "jdbc:sqlite:" + pathToDB;
        String sql = "UPDATE Users SET Name = ? WHERE ID = ?";
        try (var conn = DriverManager.getConnection(url);
            PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, newUserName);
            ps.setInt(2, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("[INFO] [DatabaseService] No user found with ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] [DatabaseService] Unable to set new Username.");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Konstruktor nur für Tests gedacht.
     * @param pathToDB
     * @param pathToSqlFile
     */
    public DatabaseService(String pathToDB, String pathToSqlFile){
        this.findPathToDB();
        this.findPathToScript();
    }
}
