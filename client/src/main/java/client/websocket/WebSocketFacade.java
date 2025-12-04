package client.websocket;

import client.ServerFacade;
import com.google.gson.Gson;
import org.glassfish.tyrus.core.wsadl.model.Endpoint;
import websocket.messages.ServerMessage;

import java.net.http.WebSocket;

public class WebSocketFacade extends Endpoint {
    
    private void handleMessage(String messageString) {
        try {
            Gson Serializer = null;
            ServerMessage message = Gson.Serializer.fromJson(messageString, ServerMessage.class);
            listener.notify(message);
        } catch(Exception ex) {
            listener.notify(new ServerFacade.ErrorMessage(ex.getMessage()));
        }
    }
    
    
}
