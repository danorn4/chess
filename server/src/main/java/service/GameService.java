package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import service.servicehelpers.GameResult;
import service.servicehelpers.JoinGameRequest;

import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    private AuthData authenticate(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if(auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return auth;
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
        AuthData auth = authenticate(authToken);
        String username = auth.username();

        if(request.playerColor() == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = dataAccess.getGame(request.gameID());

        // WHITE LOGIC
        if(request.playerColor().equals("WHITE")) {
            if(game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            GameData updatedGame = new GameData(
                    request.gameID(),
                    username,
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame.gameID(), updatedGame);
        }
        // BLACK LOGIC
        else if(request.playerColor().equals("BLACK")) {
            if(game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            GameData updatedGame = new GameData(
                    request.gameID(),
                    game.whiteUsername(),
                    username,
                    game.gameName(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame.gameID(), updatedGame);
        } else {
            throw new DataAccessException("Error: bad request");
        }
    }


}
