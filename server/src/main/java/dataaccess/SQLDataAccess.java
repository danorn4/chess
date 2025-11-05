package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class SQLDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    public SQLDataAccess() throws DataAccessException, SQLException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException, SQLException {
        DatabaseManager.createDatabase();

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
                    gameID INT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(255) NOT NULL,
                    blackUsername VARCHAR(255) NOT NULL,
                    gameName VARCHAR(255) NOT NULL,
                    gameData TEXT NOT NULL
                )""";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.createStatement()) {
                statement.executeUpdate(createUserTable);
                statement.executeUpdate(createAuthTable);
                statement.executeUpdate(createGameTable);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear database: " + e.getMessage());
        }
    }


    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String clearUserSt = "TRUNCATE TABLE user;";
            String clearAuthSt = "TRUNCATE TABLE auth;";
            String clearGameSt = "TRUNCATE TABLE game;";

            try (var clearStatements = conn.createStatement()) {
                clearStatements.execute(clearAuthSt);
                clearStatements.execute(clearGameSt);
                clearStatements.execute(clearUserSt);
            } catch (SQLException e) {
                throw new DataAccessException("Failed to clear database: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear database: " + e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(user.password(),  BCrypt.gensalt());

        var sqlSt = "INSERT INTO user (username, password, email) VALUES (?, ?, ?);";

        try (var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt)) {
                st.setString(1, user.username());
                st.setString(2, hashedPassword);
                st.setString(3, user.email());

                st.execute();
            } catch (SQLException e) {
                throw new DataAccessException("Username already exists");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
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

    // HELPER METHODS BELOW //

    /*
    Encrypts the given password and writes it to the database
    @param1
     */



}
