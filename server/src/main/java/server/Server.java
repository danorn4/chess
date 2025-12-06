package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.SQLDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import server.websocket.WebSocketHandler;
import service.ClearService;
import service.GameService;
import servicehelpers.CreateGameRequest;
import servicehelpers.GameResult;
import servicehelpers.JoinGameRequest;
import servicehelpers.LoginRequest;
import service.UserService;

import java.util.Collection;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private WebSocketHandler webSocketHandler;

    private final Gson gson = new Gson();

    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        try {
            DataAccess dataAccess = new SQLDataAccess();
            this.userService = new UserService(dataAccess);
            this.gameService = new GameService(dataAccess);
            this.clearService = new ClearService(dataAccess);

            this.webSocketHandler = new WebSocketHandler(userService, gameService);

        } catch (DataAccessException e) {
            System.err.println("FATAL: Failed to initialize database connection.");
            System.err.println(e.getMessage());
            this.userService = null;
            this.gameService = null;
            this.clearService = null;
            this.webSocketHandler = null;
        }

        javalin.delete("/db", this::clearHandler);
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.get("/game", this::listGamesHandler);
        javalin.post("/game", this::createGameHandler);
        javalin.put("/game", this::joinGameHandler);

        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
                System.out.println("WebSocket Connected");
            });
            ws.onMessage(ctx -> {
                webSocketHandler.onMessage(ctx.session, ctx.message());
            });
        });

        javalin.exception(DataAccessException.class, this::handleDataAccessException);
        javalin.exception(Exception.class, this::handleGenericException);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void handleDataAccessException(DataAccessException e, Context ctx) {
        String errorMessage = e.getMessage();

        switch (errorMessage) {
            case "bad request", "Game doesn't exist" -> ctx.status(400);
            case "unauthorized", "Auth doesn't exist" -> ctx.status(401);
            case "already taken", "Username already exists" -> ctx.status(403);
            case null, default -> ctx.status(500);
        }

        assert errorMessage != null;
        ctx.result(gson.toJson(Map.of("message", String.format("Error: %s", errorMessage))));
    }

    private void handleGenericException(Exception e, Context ctx) {
        ctx.status(500);
        ctx.result(gson.toJson(Map.of("message", String.format("Error: %s", e.getMessage()))));

    }

    private void clearHandler(@NotNull Context ctx) throws DataAccessException {
        clearService.clearApplication();
        ctx.status(200);
        ctx.result(gson.toJson(Map.of()));
    }

    private void registerHandler(@NotNull Context ctx) throws DataAccessException {
        UserData user = gson.fromJson(ctx.body(), UserData.class);

        AuthData auth = userService.register(user);

        ctx.status(200);
        ctx.result(gson.toJson(auth));
    }

    private void loginHandler(@NotNull Context ctx) throws DataAccessException {
        LoginRequest loginRequest = gson.fromJson(ctx.body(), LoginRequest.class);
        AuthData authData = userService.login(loginRequest);

        ctx.status(200);
        ctx.result(gson.toJson(authData));
    }

    private void logoutHandler(@NotNull Context ctx) throws DataAccessException {
        String authToken = ctx.header("Authorization");
        userService.logout(authToken);

        ctx.status(200);
        ctx.result(gson.toJson(Map.of()));
    }

    private void listGamesHandler(@NotNull Context ctx) throws DataAccessException {
        String authToken = ctx.header("Authorization");
        Collection<GameData> listGames = gameService.listGames(authToken);

        ctx.status(200);
        ctx.result(gson.toJson(Map.of("games", listGames)));
    }

    private void createGameHandler(@NotNull Context ctx) throws DataAccessException {
        String authToken =  ctx.header("Authorization");
        CreateGameRequest createGameRequest = gson.fromJson(ctx.body(), CreateGameRequest.class);
        GameResult gameResult = gameService.createGame(authToken, createGameRequest.gameName());

        ctx.status(200);
        ctx.result(gson.toJson((gameResult)));
    }

    private void joinGameHandler(@NotNull Context ctx) throws DataAccessException {
        String authToken = ctx.header("Authorization");
        JoinGameRequest joinGameRequest = gson.fromJson(ctx.body(), JoinGameRequest.class);

        gameService.joinGame(authToken, joinGameRequest);

        ctx.status(200);
        ctx.result(gson.toJson((Map.of())));
    }
}
