package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemDataAcess;
import io.javalin.*;
import io.javalin.http.Context;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.ClearService;
import service.GameService;
import service.RequestOrResponse.CreateGameRequest;
import service.RequestOrResponse.GameResult;
import service.RequestOrResponse.JoinGameRequest;
import service.RequestOrResponse.LoginRequest;
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

        DataAccess dataAccess = new MemDataAcess();
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
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void clearHandler(@NotNull Context ctx) {
        try {
            clearService.clearApplication();
            ctx.status(200);
            ctx.json(Map.of());
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("message", String.format("Error: %s", e.getMessage())));
        }
    }

    private void registerHandler(@NotNull Context ctx) {
        try {
            UserData user = gson.fromJson(ctx.body(), UserData.class);

            AuthData auth = userService.register(user);

            ctx.status(200);
            ctx.json(auth);
        } catch (DataAccessException e) {
            String errorMessage = e.getMessage();
            if("Error: bad request".equals(errorMessage)){
                ctx.status(400);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            } else if("Error: already taken".equals(errorMessage)){
                ctx.status(403);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            } else {
                ctx.status(500);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(Map.of("message", String.format("Error: %s", e.getMessage())));
        }
    }

    private void loginHandler(@NotNull Context ctx) {
        try {
            LoginRequest loginRequest = gson.fromJson(ctx.body(), LoginRequest.class);
            AuthData authData = userService.login(loginRequest);

            ctx.status(200);
            ctx.json(authData);
        } catch (DataAccessException e) {
            String errorMessage = e.getMessage();
            if("Error: bad request".equals(errorMessage)){
                ctx.status(400);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            } else if("Error: unauthorized".equals(errorMessage)){
                ctx.status(401);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            } else {
                ctx.status(500);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(Map.of("message", String.format("Error: %s", e.getMessage())));
        }
    }

    private void logoutHandler(@NotNull Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            userService.logout(authToken);

            ctx.status(200);
            ctx.json(Map.of());
        } catch (DataAccessException e) {
            String errorMessage =  e.getMessage();
            if("Error: unauthorized".equals(errorMessage)){
                ctx.status(401);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            } else {
                ctx.status(500);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(Map.of("message", String.format("Error: %s", e.getMessage())));
        }
    }

    private void listGamesHandler(@NotNull Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            Collection<GameData> listGames = gameService.listGames(authToken);

            ctx.status(200);
            ctx.json(Map.of("games", listGames));
        } catch(DataAccessException e) {
            String errorMessage = e.getMessage();
            if("Error: unauthorized".equals(errorMessage)){
                ctx.status(401);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            } else {
                ctx.status(500);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(Map.of("message", String.format("Error: %s", e.getMessage())));
        }
    }

    private void createGameHandler(@NotNull Context ctx) {
        try {
            String authToken =  ctx.header("Authorization");
            CreateGameRequest createGameRequest = gson.fromJson(ctx.body(), CreateGameRequest.class);
            GameResult gameResult = gameService.createGame(authToken, createGameRequest.gameName());

            ctx.status(200);
            ctx.json(gameResult);
        } catch(DataAccessException e) {
            String errorMessage = e.getMessage();
            if("Error: bad request".equals(errorMessage)){
                ctx.status(400);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            } else if("Error: unauthorized".equals(errorMessage)){
                ctx.status(401);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            } else {
                ctx.status(500);
                ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(Map.of("message", String.format("Error: %s", e.getMessage())));
        }
    }

    private void joinGameHandler(@NotNull Context ctx) {
        try {
            String authToken = ctx. header("Authorization");
            JoinGameRequest joinGameRequest = gson.fromJson(ctx.body(), JoinGameRequest.class);

            gameService.joinGame(authToken, joinGameRequest);

            ctx.status(200);
            ctx.json(Map.of());
        } catch(DataAccessException e) {
            String errorMessage = e.getMessage();
            switch (errorMessage) {
                case "Error: bad request" -> {
                    ctx.status(400);
                    ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
                }
                case "Error: unauthorized" -> {
                    ctx.status(401);
                    ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
                }
                case "Error: already taken" -> {
                    ctx.status(403);
                    ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
                }
                case null, default -> {
                    ctx.status(500);
                    ctx.json(Map.of("message", String.format("Error: %s", errorMessage)));
                }
            }
        } catch (Exception e) {
            ctx.status(400);
            ctx.json(Map.of("message", String.format("Error: %s", e.getMessage())));
        }
    }


}
