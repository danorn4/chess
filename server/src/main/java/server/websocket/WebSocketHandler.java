package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final UserService userService;
    private final GameService gameService;

    public WebSocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

            // 1. Verify the User (The "Gatekeeper")
            AuthData auth = userService.getAuth(command.getAuthToken());
            String username = auth.username();

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, command);
                case MAKE_MOVE -> makeMove(session, username, message);
                case LEAVE -> leave(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void connect(Session session, String username, UserGameCommand command) throws IOException {
        try {
            // 1. Get the game
            GameData game = gameService.getGame(command.getGameID());

            // 2. Add connection to the manager
            connections.add(command.getAuthToken(), command.getGameID(), session);

            // 3. Send LOAD_GAME to the root client
            var loadGameMsg = new LoadGameMessage(game.game());
            session.getRemote().sendString(new Gson().toJson(loadGameMsg));

            // 4. Notify everyone else
            var message = String.format("%s joined the game", username);
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void makeMove(Session session, String username, String jsonMessage) throws IOException {
        try {
            MakeMoveCommand command = new Gson().fromJson(jsonMessage, MakeMoveCommand.class);

            gameService.makeMove(command.getAuthToken(), command.getGameID(), command.getMove());

            GameData gameData = gameService.getGame(command.getGameID());
            ChessGame game = gameData.game();

            LoadGameMessage loadMsg = new LoadGameMessage(game);
            connections.broadcast(command.getGameID(), "", loadMsg); // "" means send to everyone

            String message = String.format("%s made a move: %s", username, command.getMove().toString());
            NotificationMessage notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

            ChessGame.TeamColor opponentColor;
            String opponentName;

            if (username.equals(gameData.whiteUsername())) {
                opponentColor = ChessGame.TeamColor.BLACK;
                opponentName = gameData.blackUsername();
            } else {
                opponentColor = ChessGame.TeamColor.WHITE;
                opponentName = gameData.whiteUsername();
            }

            if (opponentName == null) {
                opponentName = "Opponent";
            }

            if (game.isInCheckmate(opponentColor)) {
                String msg = String.format("%s is in CHECKMATE", opponentName);
                NotificationMessage mateNotif = new NotificationMessage(msg);
                connections.broadcast(command.getGameID(), "", mateNotif);
            } else if (game.isInCheck(opponentColor)) {
                String msg = String.format("%s is in CHECK", opponentName);
                NotificationMessage checkNotif = new NotificationMessage(msg);
                connections.broadcast(command.getGameID(), "", checkNotif);
            } else if (game.isInStalemate(opponentColor)) {
                String msg = String.format("%s is in STALEMATE", opponentName);
                NotificationMessage staleNotif = new NotificationMessage(msg);
                connections.broadcast(command.getGameID(), "", staleNotif);
            }

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void leave(Session session, String username, UserGameCommand command) throws IOException {
        try {
            connections.remove(command.getAuthToken());

            gameService.leaveGame(command.getAuthToken(), command.getGameID());

            String message = String.format("%s left the game", username);
            NotificationMessage notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void resign(Session session, String username, UserGameCommand command) throws IOException {
        try {
            gameService.resign(command.getAuthToken(), command.getGameID());

            String message = String.format("%s resigned the game", username);
            NotificationMessage notification = new NotificationMessage(message);

            session.getRemote().sendString(new Gson().toJson(notification));
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void sendError(Session session, String message) throws IOException {
        ErrorMessage errorMessage = new ErrorMessage(message);
        session.getRemote().sendString(new Gson().toJson(errorMessage));
    }
}