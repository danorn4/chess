package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;

public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    public void onMessage(Session session, String message) {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connect(session, command);
            case MAKE_MOVE -> makeMove(session, command);
            case LEAVE -> leave(session, command);
            case RESIGN -> resign(session, command);
        }
    }

    private void connect(Session session, UserGameCommand command) {
        // TODO: Implement connect logic
    }

    private void makeMove(Session session, UserGameCommand command) {
        // TODO: Implement make move logic
    }

    private void leave(Session session, UserGameCommand command) {
        // TODO: Implement leave logic
    }

    private void resign(Session session, UserGameCommand command) {
        // TODO: Implement resign logic
    }
}