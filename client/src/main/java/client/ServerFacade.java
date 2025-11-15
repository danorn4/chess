package client;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import servicehelpers.CreateGameRequest;
import servicehelpers.GameResult;
import servicehelpers.JoinGameRequest;
import servicehelpers.ListGamesResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

public class ServerFacade {

    private final String serverUrl;

    private record ErrorMessage(String message) {}

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public AuthData register(UserData user) throws ResponseException {
        var path = "/user";
        // Call makeRequest
        // 1. Method: 'POST'
        // 2. Path: '/user'
        // 3. Request Body: the 'user' object
        // 4. AuthToken: null
        // 5. Response Class: AuthData class
        return makeRequest("POST", path, user, null, AuthData.class);
    }

    public AuthData login(UserData user) throws ResponseException {
        var path = "/session";
        // Call makeRequest
        // 1. Method: 'POST'
        // 2. Path: '/user'
        // 3. Request Body: the 'user' object
        // 4. AuthToken: null
        // 5. Response Class: AuthData class
        return makeRequest("POST", path, user, null, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        // Call makeRequest
        // 1. Method: 'DELETE'
        // 2. Path: '/session'
        // 3. Request Body: the 'user' object
        // 4. AuthToken: String authToken
        // 5. Response Class: null
        makeRequest("DELETE", path, null, authToken, null);
    }

    public GameResult createGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName);

        // Call makeRequest
        // 1. Method: 'POST'
        // 2. Path: '/game'
        // 3. Request Body: the 'createGameRequest' object
        // 4. AuthToken: String authToken
        // 5. Response Class: GameResult class
        return makeRequest("POST", path, createGameRequest, authToken, GameResult.class);
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        var path = "/game";

        // Call makeRequest
        // 1. Method: 'GET'
        // 2. Path: '/game'
        // 3. Request Body: null
        // 4. AuthToken: String authToken
        // 5. Response Class: Collection<GameData>
        ListGamesResult result = makeRequest("GET", path, null, authToken, ListGamesResult.class);

        return result.games();
    }

    public void joinGame(String authToken, JoinGameRequest request) throws ResponseException{
        var path = "/game";

        // Call makeRequest
        // 1. Method: 'PUT'
        // 2. Path: '/game'
        // 3. Request Body: JoinGameRequest object
        // 4. AuthToken: String authToken
        // 5. Response Class: null
        makeRequest("PUT", path, request, authToken, null);
    }

    public void clear() throws ResponseException {
        var path = "/db";

        // Call makeRequest
        // 1. Method: 'DELETE'
        // 2. Path: '/db'
        // 3. Request Body: null
        // 4. AuthToken: null
        // 5. Response Class: null
        makeRequest("DELETE", path, null, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, String authToken, Type responseType) throws ResponseException {
        try {
            URI uri = new URI(serverUrl + path);
            URL url = uri.toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if(authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);

            return readBody(http, responseType);

        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (status / 100 != 2) { // Check if not a 2xx success code
            // Read the error body
            String errorBody;
            try (InputStream errorStream = http.getErrorStream()) {
                errorBody = new String(errorStream.readAllBytes());
            }

            String message;
            try {
                // 1. Parse the JSON error body
                ErrorMessage errorResponse = new Gson().fromJson(errorBody, ErrorMessage.class);
                // 2. Get the clean message from the JSON
                message = errorResponse.message();
            } catch (Exception e) {
                // If parsing fails, just use the raw body
                message = errorBody;
            }

            // 3. Throw an exception with the *clean* message
            throw new ResponseException(status, message);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Type responseType) throws IOException {
        T response = null;
        if (http.getContentLength() > 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseType != null) {
                    // This simple line now works for everything
                    response = new Gson().fromJson(reader, responseType);
                }
            }
        }
        return response;
    }

}
