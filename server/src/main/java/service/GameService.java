package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import servicehelpers.GameResult;
import servicehelpers.JoinGameRequest;

import java.util.Collection;
import java.util.Objects;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData authenticate(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if(auth == null) {
            throw new DataAccessException("unauthorized");
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
            throw new DataAccessException("bad request");
        }

        GameData game = dataAccess.createGame(gameName);

        return new GameResult(game.gameID());
    }

    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        AuthData auth = authenticate(authToken);
        String username = auth.username();
        String playerColor = request.playerColor();

        GameData game = dataAccess.getGame(request.gameID());

        if (playerColor == null || playerColor.isEmpty()) {
            throw new DataAccessException("bad request");
        }
        if (playerColor.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("already taken");
            }
            GameData updatedGame = new GameData(
                    game.gameID(),
                    username,
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame.gameID(), updatedGame);
        } else if (playerColor.equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("already taken");
            }
            GameData updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    username,
                    game.gameName(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame.gameID(), updatedGame);
        } else {
            throw new DataAccessException("bad request");
        }
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws DataAccessException {
        AuthData auth = authenticate(authToken);
        String username = auth.username();

        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = getChessGame(gameData, username);

        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            throw new DataAccessException("Invalid move: " + e.getMessage());
        }

        GameData updatedGame = new GameData(
                gameID,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );
        dataAccess.updateGame(gameID, updatedGame);
    }

    @NotNull
    private static ChessGame getChessGame(GameData gameData, String username) throws DataAccessException {
        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            throw new DataAccessException("Game is over");
        }

        if (game.getTeamTurn() == ChessGame.TeamColor.WHITE) {
            if (!username.equals(gameData.whiteUsername())) {
                throw new DataAccessException("Not your turn (or you are not the white player)");
            }
        } else {
            if (!username.equals(gameData.blackUsername())) {
                throw new DataAccessException("Not your turn (or you are not the black player)");
            }
        }
        return game;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return dataAccess.getGame(gameID);
    }

    public void leaveGame(String authToken, int gameID) throws DataAccessException {
        AuthData auth = authenticate(authToken);
        String username = auth.username();

        GameData game = dataAccess.getGame(gameID);

        GameData updatedGame = game;
        if (Objects.equals(game.whiteUsername(), username)) {
            updatedGame = new GameData(gameID, null, game.blackUsername(), game.gameName(), game.game());
            dataAccess.updateGame(gameID, updatedGame);
        } else if (Objects.equals(game.blackUsername(), username)) {
            updatedGame = new GameData(gameID, game.whiteUsername(), null, game.gameName(), game.game());
            dataAccess.updateGame(gameID, updatedGame);
        }

    }

    public void resign(String authToken, int gameID) throws DataAccessException {
        AuthData auth = authenticate(authToken);
        String username = auth.username();

        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();

        if (!Objects.equals(gameData.whiteUsername(), username) && !Objects.equals(gameData.blackUsername(), username)) {
            throw new DataAccessException("Observer cannot resign");
        }

        if (game.isGameOver()) {
            throw new DataAccessException("Game is already over");
        }

        game.setGameOver(true);
        game.setTeamTurn(null);

        GameData updatedGame = new GameData(
                gameID,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );
        dataAccess.updateGame(gameID, updatedGame);
    }

}
