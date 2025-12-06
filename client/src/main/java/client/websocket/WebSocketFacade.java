package client.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
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
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    try {
                        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

                        String typeString = jsonObject.get("serverMessageType").getAsString();

                        ServerMessage.ServerMessageType type = ServerMessage.ServerMessageType.valueOf(typeString);

                        switch (type) {
                            case LOAD_GAME -> notificationHandler.notify(new Gson().fromJson(message, LoadGameMessage.class));
                            case ERROR -> notificationHandler.notify(new Gson().fromJson(message, ErrorMessage.class));
                            case NOTIFICATION -> notificationHandler.notify(new Gson().fromJson(message, NotificationMessage.class));
                        }
                    } catch (Exception e) {
                        System.out.println("CRITICAL CLIENT ERROR: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, "Unable to connect to server: " + ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    /**
     * A generic method to send any command (CONNECT, MAKE_MOVE, etc.) to the server.
     */
    public void sendCommand(UserGameCommand command) throws ResponseException {
        try {
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

}