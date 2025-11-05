package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SQLDataAccessTest { // Renamed file

    private DataAccess dataAccess;

    @BeforeEach
    public void setUp() throws Exception {
        // We use the real SQL Data Access, not the in-memory one
        dataAccess = new SQLDataAccess();
        // Clear the database before each test
        dataAccess.clear();
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("user1", "pass1", "email1"));
        AuthData auth = dataAccess.createAuth("user1"); // <-- Store the auth data
        dataAccess.createGame("game1");

        dataAccess.clear();

        assertNull(dataAccess.getUser("user1"), "User data was not cleared");
        assertNull(dataAccess.getAuth(auth.authToken()), "Auth data was not cleared"); // <-- Now you can check it
        assertTrue(dataAccess.listGames().isEmpty(), "Game data was not cleared");
    }

    @Test
    public void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("user1", "password1", "user1@gmail.com");
        dataAccess.createUser(user);

        UserData userFound = dataAccess.getUser("user1");

        assertNotNull(userFound);
        assertEquals(user.username(), userFound.username());
        assertEquals(user.email(), userFound.email());
        assertTrue(BCrypt.checkpw("password1", userFound.password()));
    }

    @Test
    public void createUserFailureDuplicate() throws DataAccessException {
        UserData user1 = new UserData("user1", "password1", "user1@gmail.com");
        dataAccess.createUser(user1);

        UserData user2 = new UserData("user1", "password2", "user2@gmail.com");

        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            dataAccess.createUser(user2);
        });
        assertEquals("Username already exists", e.getMessage());
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("user1", "pass", "email"));
        UserData userFound = dataAccess.getUser("user1");
        assertNotNull(userFound);
        assertEquals("user1", userFound.username());
    }

    @Test
    public void getUserFailure_NotFound() throws DataAccessException {
        UserData userFound = dataAccess.getUser("nonexistent");
        assertNull(userFound, "getUser should return null for a user that doesn't exist");
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        GameData game = dataAccess.createGame("Test Game");
        assertNotNull(game, "createGame should return a GameData object");
        assertTrue(game.gameID() > 0, "Game ID should be positive");

        GameData gameFound = dataAccess.getGame(game.gameID());
        assertNotNull(gameFound);
        assertEquals(game.gameID(), gameFound.gameID());
        assertEquals("Test Game", gameFound.gameName());
    }

    @Test
    public void createGameFailureNullName() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createGame(null);
        }, "Should throw an exception when gameName is null");
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        GameData game = dataAccess.createGame("My Game");
        GameData gameFound = dataAccess.getGame(game.gameID());

        assertNotNull(gameFound);
        assertEquals(game.gameID(), gameFound.gameID());
        assertEquals(game.gameName(), gameFound.gameName());
    }

    @Test
    public void getGameFailureNotFound() throws DataAccessException {
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            dataAccess.getGame(9999); // 9999 is a non-existent ID
        });
        assertEquals("Error: Game doesn't exist", e.getMessage());
    }

    @Test
    public void listGamesSuccessMultipleGames() throws DataAccessException {
        dataAccess.createGame("Game 1");
        dataAccess.createGame("Game 2");

        Collection<GameData> games = dataAccess.listGames();
        assertNotNull(games);
        assertEquals(2, games.size(), "Should find 2 games");
    }

    @Test
    public void listGamesSuccessEmpty() throws DataAccessException {
        Collection<GameData> games = dataAccess.listGames();
        assertNotNull(games);
        assertEquals(0, games.size(), "Should return an empty collection, not null");
    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        GameData originalGame = dataAccess.createGame("Original Name");
        int gameID = originalGame.gameID();

        GameData updatedGameData = new GameData(
                gameID,
                "white-player", // new white username
                null,
                "Original Name",
                originalGame.game()
        );

        dataAccess.updateGame(gameID, updatedGameData);

        GameData gameFound = dataAccess.getGame(gameID);
        assertNotNull(gameFound);
        assertEquals("white-player", gameFound.whiteUsername());
        assertNull(gameFound.blackUsername());
    }

    @Test
    public void updateGameFailureNotFound() throws DataAccessException {
        GameData fakeGame = new GameData(9999, "w", "b", "fake", null);
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            dataAccess.updateGame(9999, fakeGame);
        });
        assertEquals("Game doesn't exist", e.getMessage());
    }

    @Test
    public void createAuthSuccess() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("myUser");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("myUser", auth.username());
    }

    @Test
    public void createAuthFailureNullUser() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createAuth(null);
        });
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("myUser");
        AuthData authFound = dataAccess.getAuth(auth.authToken());

        assertNotNull(authFound);
        assertEquals(auth, authFound);
    }

    @Test
    public void getAuthFailureNotFound() throws DataAccessException {
        // getAuth (per your MemDataAccess) returns null
        AuthData authFound = dataAccess.getAuth("fake-token");
        assertNull(authFound);
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("myUser");
        assertNotNull(dataAccess.getAuth(auth.authToken()));

        dataAccess.deleteAuth(auth.authToken());

        assertNull(dataAccess.getAuth(auth.authToken()));
    }

    @Test
    public void deleteAuth_Failure_NotFound() throws DataAccessException {
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            dataAccess.deleteAuth("fake-token");
        });
        assertEquals("Auth doesn't exist", e.getMessage());
    }
}