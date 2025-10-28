package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MemDataAcess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private final HashMap<String, AuthData> auths = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public void clear() throws DataAccessException {
        users.clear();
        games.clear();
        auths.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if(users.containsKey(user.username())) {
            throw new DataAccessException("Username already exists");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (!users.containsKey(username)) {
            throw new DataAccessException("Username doesn't exist");
        } else {
            return users.get(username);
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        int gameID = nextGameID++;

        ChessGame newGame = new ChessGame();

        GameData gameData = new GameData(gameID, null, null, gameName, newGame);

        games.put(gameID, gameData);

        return gameData;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData gameData = games.get(gameID);
        if(gameData == null) {
            throw new DataAccessException("Game doesn't exist");
        }
        return gameData;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        if (!games.containsKey(gameID)) {
            throw new  DataAccessException("Game doesn't exist");
        } else {
            games.put(gameID, gameData);
        }
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken =  UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);

        auths.put(authToken, authData);
        return authData;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (!auths.containsKey(authToken)) {
            throw new DataAccessException("Auth doesn't exist");
        } else {
            auths.remove(authToken);
        }
    }
}
