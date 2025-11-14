package client;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import service.servicehelpers.GameResult;
import service.servicehelpers.JoinGameRequest;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() throws ResponseException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        facade = new ServerFacade("http://localhost:" + port);

        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() throws ResponseException {
        facade.clear();
    }

// =================================================================================
    //  Clear
    // =================================================================================

    @Test
    public void clearSuccess() throws ResponseException {
        UserData user = new UserData("player1", "pass", "p1@email.com");
        AuthData auth = facade.register(user);
        facade.createGame(auth.authToken(), "testGame");

        assertDoesNotThrow(() -> facade.clear());

        assertThrows(ResponseException.class, () -> facade.listGames(auth.authToken()));
    }

    // =================================================================================
    //  Register
    // =================================================================================

    @Test
    public void registerSuccess() throws ResponseException {
        UserData testUserData = new UserData("player1", "p1", "p1@gmail.com");

        AuthData authData = facade.register(testUserData);

        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals("player1", authData.username());
    }

    @Test
    public void registerFailureAlreadyTaken() throws ResponseException {
        UserData testUserData = new UserData("player1", "p1", "p1@gmail.com");

        facade.register(testUserData);

        ResponseException e = assertThrows(ResponseException.class, () -> {
            facade.register(testUserData);
        });

        assertEquals(403, e.getStatusCode());
    }

    // =================================================================================
    //  Login
    // =================================================================================

    @Test
    public void loginSuccess() throws ResponseException {
        UserData testUser = new UserData("player1", "p1", "p1@gmail.com");
        facade.register(testUser);

        AuthData authData = facade.login(testUser);

        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals("player1", authData.username());
    }

    @Test
    public void loginFailureWrongPassword() throws ResponseException {
        UserData testUser = new UserData("player1", "p1", "p1@gmail.com");
        facade.register(testUser);

        UserData wrongPasswordUser = new UserData("player1", "wrong_password", null);

        ResponseException e = assertThrows(ResponseException.class, () -> {
            facade.login(wrongPasswordUser);
        });

        assertEquals(401, e.getStatusCode());
    }

    // =================================================================================
    //  Logout
    // =================================================================================

    @Test
    public void logoutSuccess() throws ResponseException {
        UserData testUser = new UserData("player1", "p1", "p1@gmail.com");
        AuthData auth = facade.register(testUser);

        assertDoesNotThrow(() -> facade.logout(auth.authToken()));

        ResponseException e = assertThrows(ResponseException.class, () -> {
            facade.listGames(auth.authToken());
        });
        assertEquals(401, e.getStatusCode());
    }

    @Test
    public void logoutFailureBadToken() throws ResponseException {
        ResponseException e = assertThrows(ResponseException.class, () -> {
            facade.logout("fake_token");
        });

        assertEquals(401, e.getStatusCode());
    }

    // =================================================================================
    //  Create Game
    // =================================================================================

    @Test
    public void createGameSuccess() throws ResponseException {
        UserData testUser = new UserData("player1", "p1", "p1@gmail.com");
        AuthData auth = facade.register(testUser);

        GameResult gameResult = facade.createGame(auth.authToken(), "testGame");

        assertNotNull(gameResult);
        assertTrue(gameResult.gameID() > 0);
    }

    @Test
    public void createGameFailureBadAuth() throws ResponseException {
        ResponseException e = assertThrows(ResponseException.class, () -> {
            facade.createGame("fake_token", "testGame");
        });

        assertEquals(401, e.getStatusCode());
    }

    // =================================================================================
    //  List Games
    // =================================================================================

    @Test
    public void listGamesSuccess() throws ResponseException {
        UserData testUser = new UserData("player1", "p1", "p1@gmail.com");
        AuthData auth = facade.register(testUser);
        facade.createGame(auth.authToken(), "game1");
        facade.createGame(auth.authToken(), "game2");

        Collection<GameData> games = facade.listGames(auth.authToken());

        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesFailureBadAuth() throws ResponseException {
        ResponseException e = assertThrows(ResponseException.class, () -> {
            facade.listGames("fake_token");
        });

        assertEquals(401, e.getStatusCode());
    }

    // =================================================================================
    //  Join Game
    // =================================================================================

    @Test
    public void joinGameSuccess() throws ResponseException {
        UserData testUser = new UserData("player1", "p1", "p1@gmail.com");
        AuthData auth = facade.register(testUser);
        GameResult gameResult = facade.createGame(auth.authToken(), "testGame");

        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", gameResult.gameID());

        assertDoesNotThrow(() -> facade.joinGame(auth.authToken(), joinRequest));
    }

    @Test
    public void joinGameFailureSpotTaken() throws ResponseException {
        UserData user1 = new UserData("player1", "p1", "p1@email.com");
        AuthData auth1 = facade.register(user1);
        GameResult gameResult = facade.createGame(auth1.authToken(), "testGame");

        JoinGameRequest joinRequest1 = new JoinGameRequest("WHITE", gameResult.gameID());
        facade.joinGame(auth1.authToken(), joinRequest1);

        UserData user2 = new UserData("player2", "p2", "p2@email.com");
        AuthData auth2 = facade.register(user2);

        JoinGameRequest joinRequest2 = new JoinGameRequest("WHITE", gameResult.gameID());

        ResponseException e = assertThrows(ResponseException.class, () -> {
            facade.joinGame(auth2.authToken(), joinRequest2);
        });

        assertEquals(403, e.getStatusCode());
    }



}
