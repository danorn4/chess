package client.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

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
                    System.out.println("DEBUG: Received JSON: " + message);

                    try {
                        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);

                        switch (serverMessage.getServerMessageType()) {
                            case LOAD_GAME -> {
                                LoadGameMessage msg = new Gson().fromJson(message, LoadGameMessage.class);
                                System.out.println("DEBUG: LOAD_GAME received. Game object: " + msg.getGame());
                                notificationHandler.notify(msg);
                            }
                            case ERROR -> notificationHandler.notify(new Gson().fromJson(message, ErrorMessage.class));
                            case NOTIFICATION -> notificationHandler.notify(new Gson().fromJson(message, NotificationMessage.class));
                        }
                    } catch (Throwable e) {
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

    public void sendCommand(UserGameCommand command) throws ResponseException {
        try {
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}