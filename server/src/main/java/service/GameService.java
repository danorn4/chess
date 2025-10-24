package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import service.RequestOrResponse.GameResult;
import service.RequestOrResponse.JoinGameRequest;

import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    private void authenticate(String authToken) throws DataAccessException {
        if(authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        AuthData auth = dataAccess.getAuth(authToken);

        if(auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        authenticate(authToken);
        return dataAccess.listGames();
    }

    public GameResult createGame(String authToken, String gameName) throws DataAccessException {
        authenticate(authToken);

        if(gameName == null ||  gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = dataAccess.createGame(gameName);

        return new GameResult(game.gameID());
    }

    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        authenticate(authToken);


    }


}
