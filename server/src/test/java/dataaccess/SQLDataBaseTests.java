package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLDbTests {
    private DataAccess dataAccess;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new SQLDataAccess();
        dataAccess.clear();
    }

    @Test
    public void createUser_Success() throws DataAccessException {
        UserData user = new UserData("user1", "password1", "user1@gmail.com");
        dataAccess.createUser(user);

        UserData userFound = dataAccess.getUser("user1");

        assertNotNull(userFound);
        assertEquals(user.username(), userFound.username());
    }

    @Test
    public void createUser_Failure_Duplicate() throws DataAccessException {
        UserData user1 = new UserData("user1", "password1", "user1@gmail.com");
        dataAccess.createUser(user1);

        UserData user2 = new UserData("user1", "password2", "user2@gmail.com");

        assertThrows(DataAccessException.class, () -> {
            dataAccess.createUser(user2);
        }, "Username already exists");
    }



}
