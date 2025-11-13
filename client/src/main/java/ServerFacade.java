import exception.ResponseException;

import java.lang.module.ResolutionException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }














    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URI uri = new URI(serverUrl + path);
            URL url = uri.toURL();

            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);

            http.connect();

            throwIfNotSuccessful(http);

            return readBody(http, responseClass);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws ResolutionException {
        if(request != null) {
            http.addRequestProperty("Content-Type", "");
        }
    }




}
