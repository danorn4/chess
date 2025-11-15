package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.SQLDataAccess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servicehelpers.GameResult;
import servicehelpers.JoinGameRequest;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private DataAccess dataAccess;
    private GameService gameService;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new SQLDataAccess();
        gameService = new GameService(dataAccess);

        dataAccess.clear();
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("user1");
        dataAccess.createGame("game 1");
        dataAccess.createGame("game 2");

        Collection<GameData> games = gameService.listGames(auth.authToken());

        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesFail() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("user1");
        dataAccess.createGame("game 1");
        dataAccess.createGame("game 2");

        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            gameService.listGames("fake token");
        });
        assertEquals("unauthorized",  e.getMessage());
    }

    @Test
    public void createGameSuccess() throws DataAccessException{
        AuthData authData = dataAccess.createAuth("user1");
        String gameName = "game1";

        GameResult gameResult = gameService.createGame(authData.authToken(), gameName);

        assertNotNull(gameResult);
        assertEquals(1, gameResult.gameID());

        GameData gameData = dataAccess.getGame(gameResult.gameID());
        assertNotNull(gameData);
        assertEquals("game1", gameData.gameName());
    }

    @Test
    public void createGameFailsBadToken() throws DataAccessException{
        String gameName = "game1";

        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            gameService.createGame("non-existent token", gameName);
        });

        assertEquals("unauthorized", e.getMessage());
    }

    @Test
    public void createGameFailsInvalidGameName() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("user1");
        String gameName = "";

        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(auth.authToken(), gameName);
        });

        assertEquals("bad request", e.getMessage());
    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        AuthData auth1 = dataAccess.createAuth("user1");
        AuthData auth2 = dataAccess.createAuth("user2");
        GameData game = dataAccess.createGame("game1");

        JoinGameRequest joinRequest1 = new JoinGameRequest("WHITE", game.gameID());
        assertDoesNotThrow(() -> {
            gameService.joinGame(auth1.authToken(), joinRequest1);
        });

        JoinGameRequest joinRequest2 = new JoinGameRequest("BLACK", game.gameID());
        assertDoesNotThrow(() -> {
            gameService.joinGame(auth2.authToken(), joinRequest2);
        });

        GameData joinedGame = dataAccess.getGame(game.gameID());

        assertNotNull(joinedGame);
        assertEquals(auth1.username(), joinedGame.whiteUsername());
        assertEquals(auth2.username(), joinedGame.blackUsername());
    }

    @Test
    public void joinGameFailBadToken() throws DataAccessException {
        GameData game = dataAccess.createGame("game");

        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", game.gameID());
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("non-existent token", joinRequest);
        });

        assertEquals("unauthorized", e.getMessage());
    }

    @Test
    public void joinGameFailsWrongColor() throws DataAccessException {
        AuthData auth1 = dataAccess.createAuth("user1");
        GameData game = dataAccess.createGame("game");

        DataAccessException e;

        JoinGameRequest requestNullColor = new JoinGameRequest(null, game.gameID());

        e = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(auth1.authToken(), requestNullColor);
        });
        assertEquals("bad request", e.getMessage());

        JoinGameRequest requestWrongColor = new JoinGameRequest("BLUE", game.gameID());
        e = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(auth1.authToken(), requestWrongColor);
        });
        assertEquals("bad request", e.getMessage());
    }

    @Test
    public void joinGameFailsTaken() throws DataAccessException {
        AuthData auth1 = dataAccess.createAuth("user1");
        AuthData auth2 = dataAccess.createAuth("user2");
        GameData game = dataAccess.createGame("game1");

        GameData alreadyJoinedGame = new GameData(game.gameID(),
                auth1.username(),
                auth2.username(),
                game.gameName(),
                new ChessGame());

        dataAccess.updateGame(alreadyJoinedGame.gameID(), alreadyJoinedGame);

        JoinGameRequest request = new JoinGameRequest("WHITE", game.gameID());

        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(auth1.authToken(), request);
        });

        assertEquals("already taken", e.getMessage());
    }

    @Test
    public void joinGameFailsWrongGame() throws DataAccessException {
        AuthData auth = dataAccess.createAuth("user1");

        JoinGameRequest request = new JoinGameRequest("WHITE", 99999);

        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(auth.authToken(), request);
        });

        assertEquals("Game doesn't exist", e.getMessage());
    }


}
