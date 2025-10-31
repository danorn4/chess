package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import javax.xml.crypto.Data;
import java.sql.SQLData;
import java.util.Collection;
import java.util.List;

public class SQLDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    public SQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        try(var conn = DatabaseManager.getConnection()) {
            final String createUserTable = """
                    CREATE TABLE IF NOT EXISTS user (
                        username VARCHAR(255) NOT NULL PRIMARY KEY,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL
                    )""";
            final String createAuthTable = """
                    CREATE TABLE IF NOT EXISTS auth (
                        authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                        username VARCHAR(255) NOT NULL
                    )""";
            final String createGameTable = """
                    CREATE TABLE IF NOT EXISTS game (
                        gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        whiteUsername VARCHAR(255),
                        blackUsername VARCHAR(255),
                        gameName VARCHAR(255),
                        gameData TEXT NOT NULL
                    )"""; 
        }
    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {

    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }
}
