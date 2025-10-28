package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemDataAcess;
import io.javalin.*;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.ClearService;
import service.GameService;
import service.UserService;

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
        javalin.delete("/db", this::clearHandler();
        javalin.post("/user", this::registerHandler);
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
        } catch (DataAccessException e) {
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
            if("")
        }
    }




}
