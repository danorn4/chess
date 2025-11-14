package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.SQLDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.servicehelpers.LoginRequest;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    private DataAccess dataAccess;
    private UserService userService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new SQLDataAccess();
        userService = new UserService(dataAccess);

        dataAccess.clear();
    }

    @Test
    public void registerUserSuccess() throws DataAccessException {
        UserData testUser = new UserData("player1", "password1", "p1@egmail.com");

        AuthData auth = userService.register(testUser);

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("player1", auth.username());

        UserData userDb = dataAccess.getUser("player1");
        assertNotNull(userDb);
        assertEquals("player1",  userDb.username());
    }

    @Test
    public void registerUserFailsUserTaken() throws DataAccessException {
        UserData existingUser = new UserData("player1", "password1", "p1@egmail.com");
        dataAccess.createUser(existingUser);

        UserData newUser = new UserData("player1", "password2", "p2@gmail.com");

        DataAccessException e = assertThrows(DataAccessException.class, () -> userService.register(newUser));

        assertEquals("Error: already taken",  e.getMessage());
    }

    @Test
    public void loginSuccess()  throws DataAccessException {
        UserData user = new UserData("player1", "password1", "p1@gmail.com");
        dataAccess.createUser(user);

        LoginRequest loginRequest = new LoginRequest(user.username(), user.password());

        AuthData auth = userService.login(loginRequest);

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("player1", auth.username());
    }

    @Test
    public void loginFailsWrongPassword() throws DataAccessException {
        UserData user = new UserData("player1", "password1", "p1@gmail.com");
        dataAccess.createUser(user);

        LoginRequest loginRequest = new LoginRequest(user.username(), "WRONG PASSWORD");

        DataAccessException e = assertThrows(DataAccessException.class, () -> userService.login(loginRequest));

        assertEquals("Error: unauthorized",  e.getMessage());
    }

    @Test
    public void loginFailsWrongUsername() throws DataAccessException {
        UserData user = new UserData("player1", "password1", "p1@gmail.com");
        dataAccess.createUser(user);

        LoginRequest loginRequest = new LoginRequest(null, null);

        DataAccessException e = assertThrows(DataAccessException.class, () -> userService.login(loginRequest));

        assertEquals("Error: bad request",  e.getMessage());
    }

    @Test
    public void logoutSuccess() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("player1");

        assertDoesNotThrow(() -> {
            userService.logout(auth.authToken());
        });

        AuthData deletedAuth = dataAccess.getAuth(auth.authToken());

        assertNull(deletedAuth);
    }

    @Test
    public void logoutFailsBadToken() {
        String fakeToken = "fakeToken";

        DataAccessException e = assertThrows(DataAccessException.class, () -> userService.logout(fakeToken));

        assertEquals("Error: unauthorized", e.getMessage());
    }
}
