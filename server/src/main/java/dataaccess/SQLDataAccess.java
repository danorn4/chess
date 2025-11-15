package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class SQLDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    public SQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        final String createUserTable = """
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(255) NOT NULL PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )""";
        final String createAuthTable = """
                CREATE TABLE IF NOT EXISTS auth (
                    username VARCHAR(255) NOT NULL,
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY
                )""";
        final String createGameTable = """
                CREATE TABLE IF NOT EXISTS game (
                    gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
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
            throw new DataAccessException("Failed to configure database: " + e.getMessage());
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
                st.executeUpdate();
            }
        } catch (SQLException e) {
            if(e.getErrorCode() == 1062) {
                throw new DataAccessException("Error: Username already exists");
            }
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sqlSt =  "SELECT * FROM user WHERE username = ?;";

        try (var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt)) {
                st.setString(1, username);
                try (var rs = st.executeQuery()) {
                    if(rs.next()) {
                        var foundUsername =  rs.getString("username");
                        var foundPassword = rs.getString("password");
                        var foundEmail = rs.getString("email");
                        return new UserData(foundUsername, foundPassword, foundEmail);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        var sqlSt = "INSERT INTO game (gameName, gameData) VALUES(?, ?);";
        ChessGame newGame = new ChessGame();
        newGame.getBoard().resetBoard();
        String gameDataJson = gson.toJson(newGame);

        try (var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt, Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, gameName);
                st.setString(2, gameDataJson);
                st.executeUpdate();
                try(var rs = st.getGeneratedKeys()) {
                    if(rs.next()) {
                        int gameID = rs.getInt(1);
                        return new GameData(gameID, null, null, gameName, newGame);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        throw new DataAccessException("Failed to create game and retrieve ID");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var  sqlSt = "SELECT * FROM game WHERE gameID = ?;";

        try (var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt)) {
                st.setInt(1, gameID);
                try (var rs = st.executeQuery()) {
                    if(rs.next()) {
                        int gameIDFound = rs.getInt("gameID");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        String gameDataJson = rs.getString("gameData");

                        ChessGame game = gson.fromJson(gameDataJson, ChessGame.class);

                        return new GameData(gameIDFound, whiteUsername, blackUsername, gameName, game);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        throw new DataAccessException("Error: Game doesn't exist");
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var sqlSt = "SELECT * FROM game ORDER BY gameID;";

        Collection<GameData> gameDataList = new ArrayList<>();
        try(var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt)) {
                try (var rs = st.executeQuery()) {
                    while(rs.next()) {
                        int gameIDFound = rs.getInt("gameID");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        String gameDataJson = rs.getString("gameData");

                        ChessGame game = gson.fromJson(gameDataJson, ChessGame.class);
                        GameData gameData = new GameData(gameIDFound, whiteUsername, blackUsername, gameName, game);
                        gameDataList.add(gameData);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        return gameDataList;
    }

    @Override
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        var sqlSt = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameData = ? WHERE gameID = ?";

        String gameDataJson = gson.toJson(gameData.game());

        try (var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt)) {
                st.setString(1, gameData.whiteUsername());
                st.setString(2, gameData.blackUsername());
                st.setString(3, gameDataJson);
                st.setInt(4, gameID);

                int rowsAffected = st.executeUpdate();

                if (rowsAffected == 0) {
                    throw new DataAccessException("Error: Game doesn't exist");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game: " + e.getMessage());
        }
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        var sqlSt = "INSERT INTO auth (username, authToken) VALUES (?, ?);";

        try(var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt)) {
                String auth = UUID.randomUUID().toString();

                st.setString(1, username);
                st.setString(2, auth);
                st.executeUpdate();

                return new AuthData(username, auth);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var sqlSt =  "SELECT * FROM auth WHERE authToken = ?;";

        try (var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt)) {
                st.setString(1, authToken);
                try (var rs = st.executeQuery()) {
                    if(rs.next()) {
                        String usernameFound = rs.getString("username");
                        String authTokenFound = rs.getString("authToken");
                        return new AuthData(usernameFound, authTokenFound);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var sqlSt = "DELETE FROM auth WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var st = conn.prepareStatement(sqlSt)) {
                st.setString(1, authToken);

                int rowsAffected = st.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Error: Auth doesn't exist");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth: " + e.getMessage());
        }
    }
}