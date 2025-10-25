package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemDataAcess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private DataAccess dataAccess;
    private ClearService clearService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new MemDataAcess();
        clearService = new ClearService(dataAccess);
    }

    @Test
    public void testClearApplication() throws DataAccessException {
        UserData user1 = new UserData("user1", "p1", "p1@gmail.com");
        UserData user2 = new UserData("user2", "p2", "p2@gmail.com");
        AuthData auth1 = dataAccess.createAuth("auth1");
        AuthData auth2 = dataAccess.createAuth("auth2");
        dataAccess.createUser(user1);
        dataAccess.createUser(user2);
        GameData game1 = dataAccess.createGame("game1");
        GameData game2 = dataAccess.createGame("game2");

        clearService.clearApplication();

        DataAccessException e;

        e = assertThrows(DataAccessException.class, () -> dataAccess.getUser("user1"));
        assertEquals("Username doesn't exist", e.getMessage());

        e = assertThrows(DataAccessException.class, () -> dataAccess.getUser("user2"));
        assertEquals("Username doesn't exist", e.getMessage());

        e = assertThrows(DataAccessException.class, () -> dataAccess.getAuth(auth1.authToken()));
        assertEquals("Auth doesn't exist", e.getMessage());

        e = assertThrows(DataAccessException.class, () -> dataAccess.getAuth(auth2.authToken()));
        assertEquals("Auth doesn't exist", e.getMessage());

        e = assertThrows(DataAccessException.class, () -> dataAccess.getGame(game1.gameID()));
        assertEquals("Game doesn't exist", e.getMessage());

        e = assertThrows(DataAccessException.class, () -> dataAccess.getGame(game2.gameID()));
        assertEquals("Game doesn't exist", e.getMessage());
    }

}
