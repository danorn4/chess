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

        assertDoesNotThrow(() -> clearService.clearApplication());

        AuthData auth = dataAccess.getAuth("auth1");
        assertNull(auth);

        UserData userAfterClear = dataAccess.getUser("user1");

        assertNull(userAfterClear);

        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            dataAccess.getGame(1);
        });
        assertEquals("Game doesn't exist", e.getMessage());



    }

}
