package client.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

// Using javax.websocket for Tyrus
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            // 1. Convert http url to ws url (http://localhost:8080 -> ws://localhost:8080)
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            // 2. Open the connection
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // 3. Set up the message handler (Receiver)
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    // Deserialize the JSON string from the server into a ServerMessage
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    // Notify the UI (Repl) so it can redraw the board or print text
                    notificationHandler.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, "Unable to connect to server: " + ex.getMessage());
        }
    }

    // Endpoint requires this method, but we don't need custom logic here
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    /**
     * A generic method to send any command (CONNECT, MAKE_MOVE, etc.) to the server.
     */
    public void sendCommand(UserGameCommand command) throws ResponseException {
        try {
            // Serialize the command object to JSON and send it
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

}