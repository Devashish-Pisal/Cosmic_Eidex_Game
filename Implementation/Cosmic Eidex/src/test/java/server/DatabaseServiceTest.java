package server;

import com.group06.cosmiceidex.common.User;
import com.group06.cosmiceidex.exceptions.AuthException;
import com.group06.cosmiceidex.server.DatabaseService;
import com.group06.cosmiceidex.common.LeaderboardEntry;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseServiceTest {
    private DatabaseService dbService;

    @BeforeEach
    public void setup() throws Exception {
        String tempDb = "test_" + UUID.randomUUID() + ".db";
        dbService = DatabaseService.getDatabaseServiceInstance();
        dbService.setPathToDB(tempDb);
        createTestSchemaFromScript();
        setupTestData(dbService.getPathToDB());
    }

    private void createTestSchemaFromScript() throws Exception {
        String dbUrl = "jdbc:sqlite:" + dbService.getPathToDB();
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            InputStream in = getClass().getResourceAsStream("/com/group06/cosmiceidex/database/Scripts/Create_Tables.sql");
            if (in == null) {
                throw new FileNotFoundException("Create_Tables.sql not found in resources");
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    private static void setupTestData(String path) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (1, \"admin\", \"admin\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (2, \"admin2\", \"admin2\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (3, \"user1\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (4, \"user2\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (5, \"user3\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (6, \"user4\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (7, \"user5\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (8, \"user6\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (9, \"user7\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (10, \"user8\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (11, \"user9\", \"pw1\");");
            stmt.execute("INSERT INTO Users (ID, Name, Password) VALUES (12, \"user10\", \"pw1\");");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (1,10,10,10);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (2,20,20,20);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (3,2,2,2);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (4,3,3,3);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (5,3,3,3);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (6,3,3,3);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (7,3,3,3);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (8,3,3,3);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (9,3,3,3);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (10,3,3,3);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (11,3,3,3);");
            stmt.execute("INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES (12,3,3,3);");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void cleanup() {
        new File(dbService.getPathToDB()).delete();
    }

    @Test
    void testSingletonInstanceNotNull() {
        DatabaseService instance = DatabaseService.getDatabaseServiceInstance();
        assertNotNull(instance, "Instance should not be null");
    }

    @Test
    void testSingletonInstanceSame() {
        DatabaseService firstInstance = DatabaseService.getDatabaseServiceInstance();
        DatabaseService secondInstance = DatabaseService.getDatabaseServiceInstance();
        assertSame(firstInstance, secondInstance, "Both instances should be the same");
    }

    @Test
    public void testRegisterUser_Success() {
        assertDoesNotThrow(() -> dbService.registerUser("alice", "pass123"));
    }

    @Test
    public void testRegisterUser_UsernameAlreadyExists() throws Exception {
        dbService.registerUser("bob", "secret");
        AuthException ex = assertThrows(AuthException.class, () -> dbService.registerUser("bob", "otherpass"));
        assertEquals("Benutzername ist bereits vergeben.", ex.getMessage());
    }

    @Test
    public void testRegisterUser_EmptyUsername() {
        assertDoesNotThrow(() -> dbService.registerUser("", "pw"));
    }

    @Test
    public void testRegisterUser_SQLiteFehler() {
        assertDoesNotThrow(() -> {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbService.getPathToDB());
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE Users;");
            }
        });

        AuthException ex = assertThrows(AuthException.class, () -> dbService.registerUser("any", "any"));
        assertTrue(ex.getMessage().startsWith("Fehler beim Zugriff auf die Benutzerdaten:"));
    }

    @Test
    public void testUpsertLeaderboard_InsertSuccess() {
        assertDoesNotThrow(() -> dbService.upsertLeaderboard(1, 5, 10, 100));
    }

    @Test
    public void testUpsertLeaderboard_UpdateSuccess() {
        // Vorausgesetzt, dass der User_ID 1 bereits existiert
        assertDoesNotThrow(() -> dbService.upsertLeaderboard(1, 15, 20, 200));
    }

    @Test
    public void testUpsertLeaderboard_NullId() {
        assertThrows(SQLException.class, () -> dbService.upsertLeaderboard(0, 5, 10, 100)); 
    }

    @Test
    public void testUpsertLeaderboard_NonExistingUserId() {
        assertDoesNotThrow(() -> dbService.upsertLeaderboard(999, 5, 10, 100));
        // Überprüfen Sie anschließend, dass der Eintrag erstellt wurde.
    }

    @Test
    public void testUpsertLeaderboard_NegativeWins() {
        assertThrows(SQLException.class, () -> dbService.upsertLeaderboard(1, -5, 10, 100));
    }
    /*
    @Test
    public void testCheckPassword_ValidUser() throws SQLException {
        User user = dbService.checkPassword("admin", "admin");
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertEquals("admin", user.getPassword());
    }*/

    @Test
    public void testCheckPassword_InvalidPassword() throws SQLException {
        User user = dbService.checkPassword("admin", "wrongPassword");
        assertNull(user);
    }

    @Test
    public void testCheckPassword_NonExistingUser() throws SQLException {
        User user = dbService.checkPassword("unknownUser", "admin");
        assertNull(user);
    }

    @Test
    public void testCheckPassword_NullUserName() throws SQLException {
        User user = dbService.checkPassword(null, "admin");
        assertNull(user);
    }

    @Test
    public void testCheckPassword_NullPassword() throws SQLException {
        User user = dbService.checkPassword("admin", null);
        assertNull(user);
    }
    /*
    @Test
    public void testGetFullLeaderboard() {
        List<LeaderboardEntry> leaderboard = dbService.getFullLeaderboard();

        assertEquals(12, leaderboard.size());

        assertEquals("admin2", leaderboard.get(0).getUserName());
        assertEquals(20, leaderboard.get(0).getWins());
        assertEquals(20, leaderboard.get(0).getTricks());
        assertEquals(20, leaderboard.get(0).getPoints());

        assertEquals("admin", leaderboard.get(1).getUserName());
        assertEquals(10, leaderboard.get(1).getWins());
        assertEquals(10, leaderboard.get(1).getTricks());
        assertEquals(10, leaderboard.get(1).getPoints());
    }*/

    @Test
    public void testGetFullLeaderboard_Empty() {
        clearTestData(dbService.getPathToDB());

        List<LeaderboardEntry> leaderboard = dbService.getFullLeaderboard();
        assertEquals(0, leaderboard.size());
    }

    private static void clearTestData(String path) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Users");
            stmt.execute("DELETE FROM Leaderboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    @Test
    public void testGetTopTen() {
        List<LeaderboardEntry> leaderboard = dbService.getTopTen();

        assertNotNull(leaderboard);
        assertEquals(11, leaderboard.size());
        assertEquals("admin", leaderboard.get(1).getUserName());
    }

    @Test
    public void testFindIdOfUser_existingUser_returnsCorrectId() throws Exception {
        String knownUser = "admin"; 
        int expectedId = 1; 

        int actualId = dbService.findIdOfUser(knownUser);

        assertEquals(expectedId, actualId, "Die ID sollte korrekt zurückgegeben werden");
    }*/

    @Test
    public void testFindIdOfUser_nonExistingUser_returnsMinusOne() throws Exception {
        int result = dbService.findIdOfUser("NichtVorhanden");

        assertEquals(-1, result, "Für einen nicht existierenden User muss -1 zurückgegeben werden");
    }
    /*
    @Test
    public void testFindWinCount_existingUser() throws Exception{
        int wins = dbService.findWinCount(1);

        assertEquals(10, wins, "Die Anzahl der Wins sollte korrekt übergeben werden.");
    }*/

    @Test
    public void testFindWinCount_nonExistingUser() throws Exception{
        int wins = dbService.findWinCount(100);

        assertEquals(-1, wins, "Die Anzahl der Wins sollte korrekt übergeben werden.");
    }
    /*
    @Test
    public void testChangePassword_ValidUser() throws SQLException {
        dbService.changePassword(1, "newPassword");
        User updatedUser = dbService.checkPassword("admin", "newPassword");
        assertNotNull(updatedUser);
    }

    @Test
    public void testChangePassword_UserNotFound() {
        assertThrows(SQLException.class, () -> dbService.changePassword(99, "newPassword"));
    }

    @Test
    public void testChangePassword_NullPassword() {
        assertThrows(SQLException.class, () -> dbService.changePassword(1, null));
    }

    @Test
    public void testChangeName_ValidUser() throws SQLException {
        dbService.changeName(1, "newAdmin");

        User updatedUser = dbService.checkPassword("newAdmin", "admin");
        assertNotNull(updatedUser);
        assertEquals("newAdmin", updatedUser.getUsername());
    }*/

    @Test
    public void testChangeName_UserNotFound() {
        assertThrows(SQLException.class, () -> dbService.changeName(99, "newUser"));
    }

    @Test
    public void testChangeName_NullUserName() {
        assertThrows(SQLException.class, () -> dbService.changeName(1, null));
    }
}
