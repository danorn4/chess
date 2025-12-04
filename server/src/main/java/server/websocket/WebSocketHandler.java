package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();

    public WebSocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            // 1. Deserialize the command
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            // 2. Handle the specific command type
            switch (command.getCommandType()) {
                case CONNECT -> connect(session, command);
                case MAKE_MOVE -> makeMove(session, command);
                case LEAVE -> leave(session, command);
                case RESIGN -> resign(session, command);
            }
        } catch (Exception e) {
            // Catch-all for unexpected errors
            sendError(session, "Error: " + e.getMessage());
        }
    }

    // --- Command Implementations ---

    private void connect(Session session, UserGameCommand command) throws IOException {
        try {
            // 1. Verify Auth
            AuthData auth = userService.getAuth(command.getAuthToken()); // You added this public method earlier

            // 2. Verify Game
            GameData game = gameService.getGame(command.getGameID()); // You added this public method earlier

            // 3. Add to Connection Manager
            connections.add(command.getAuthToken(), command.getGameID(), session);

            // 4. Send LOAD_GAME to the root client
            var loadGameMsg = new LoadGameMessage(game.game());
            session.getRemote().sendString(gson.toJson(loadGameMsg));

            // 5. Notify others
            var message = String.format("%s joined the game", auth.username());
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void makeMove(Session session, UserGameCommand command) throws IOException {
        try {
            // 1. Call the Service to validate and perform the move
            // (You implemented makeMove in GameService earlier)
            gameService.makeMove(command.getAuthToken(), command.getGameID(), command.getMove());

            // 2. Get the updated game state
            GameData game = gameService.getGame(command.getGameID());

            // 3. Broadcast LOAD_GAME to EVERYONE (including root client)
            var loadGameMsg = new LoadGameMessage(game.game());
            connections.broadcast(command.getGameID(), null, loadGameMsg); // null means "don't exclude anyone"

            // 4. Broadcast NOTIFICATION to others
            AuthData auth = userService.getAuth(command.getAuthToken());
            var message = String.format("%s made a move: %s", auth.username(), command.getMove());
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void leave(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = userService.getAuth(command.getAuthToken());

            // 1. Remove connection
            connections.remove(command.getAuthToken());

            // 2. Notify others
            var message = String.format("%s left the game", auth.username());
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

            // Note: The service might need to update the DB to set whiteUsername/blackUsername to null
            // if you want "leaving" to actually remove them from the database record.
            // Check your specs on this. Often "Leave" just means closing the socket.

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void resign(Session session, UserGameCommand command) throws IOException {
        try {
            // 1. Call Service to mark game as over
            gameService.resign(command.getAuthToken(), command.getGameID());

            // 2. Notify everyone
            AuthData auth = userService.getAuth(command.getAuthToken());
            var message = String.format("%s resigned the game", auth.username());
            var notification = new NotificationMessage(message);

            // Broadcast to EVERYONE (including root)
            connections.broadcast(command.getGameID(), null, notification);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    // --- Helper ---

    private void sendError(Session session, String message) throws IOException {
        var errorMsg = new ErrorMessage(message);
        session.getRemote().sendString(gson.toJson(errorMsg));
    }
}