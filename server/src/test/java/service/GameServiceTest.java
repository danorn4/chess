package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemDataAcess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.RequestOrResponse.GameResult;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private DataAccess dataAccess;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemDataAcess();
        gameService = new GameService(dataAccess);
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
        assertEquals("Error: unauthorized",  e.getMessage());
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
}
