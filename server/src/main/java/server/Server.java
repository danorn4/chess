package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemDataAcess;
import dataaccess.SQLDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.ClearService;
import service.GameService;
import service.servicehelpers.CreateGameRequest;
import service.servicehelpers.GameResult;
import service.servicehelpers.JoinGameRequest;
import service.servicehelpers.LoginRequest;
import service.UserService;

import java.util.Collection;
import java.util.Map;

public class Server {

    private final Javalin javalin;

    private final Gson gson = new Gson();

    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // DataAccess dataAccess = new MemDataAcess();
        DataAccess dataAccess = new SQLDataAccess();
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.clearService = new ClearService(dataAccess);

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", this::clearHandler);
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.get("/game", this::listGamesHandler);
        javalin.post("/game", this::createGameHandler);
        javalin.put("/game", this::joinGameHandler);

        // exception handlers
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

        if("Error: bad request".equals(errorMessage) ||
        "Error: Game doesn't exist".equals(errorMessage)) {
            ctx.status(400);
            ctx.result(gson.toJson(Map.of("message", errorMessage)));
        } else if("Error: unauthorized".equals(errorMessage)) {
            ctx.status(401);
            ctx.result(gson.toJson(Map.of("message", errorMessage)));
        } else if("Error: already taken".equals(errorMessage)) {
            ctx.status(403);
            ctx.result(gson.toJson(Map.of("message", errorMessage)));
        }
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
