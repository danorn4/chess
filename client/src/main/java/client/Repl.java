package client;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.servicehelpers.GameResult;

import java.util.Collection;
import java.util.Scanner;

public class Repl {
    private final ServerFacade server;
    private boolean isLoggedIn = false;
    private String authToken = null;


    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to 240 chess. Type Help to get started.");

        Scanner scanner = new Scanner(System.in);
        String result = "";

        while(true) {
            printPrompt();

            // READ
            String input = scanner.nextLine();

            // EVAL
            result = eval(input);

            // PRINT
            System.out.println(result);

            // QUIT
            if(result.equals("quit")) {
                break;
            }
        }
        System.out.println("Goodbye!");
    }

    public String eval(String input) {
        try {
            String[] args = input.split(" ");

            // Pre-Login
            if(!isLoggedIn) {
                return preLoginEval(args);

            // Post-Login
            } else {
                return postLoginEval(args);
            }


        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String preLoginEval(String[] args) throws ResponseException {
        String command = args[0].toLowerCase();
        return switch (command) {
            case "help" -> help();
            case "quit" -> "quit";
            case "login" -> loginHandler(args);
            case "register" -> registerHandler(args);
            default -> "Unknown command. Type 'help' for a list of available commands.";
        };
    }

    public String postLoginEval(String[] args) throws ResponseException {
        String command = args[0].toLowerCase();
        return switch (command) {
            case "help" -> help();
            case "logout" -> logoutHandler(args);
            case "create" -> createHandler(args);
            case "list" -> listHandler(args);
            case "join" -> "implement joinHandler()";
            case "observe" -> "implement observeHandler()";
            default -> "Unknown command. Type 'help' for a list of available commands.";
        };
    }

    public String registerHandler(String[] args) throws ResponseException {
        if(args.length != 4) {
            return "Error: invalid command. Use: register <USERNAME> <PASSWORD> <EMAIL>";
        }

        String username = args[1];
        String password = args[2];
        String email = args[3];

        UserData userData = new UserData(username, password, email);
        AuthData authData = server.register(userData);

        isLoggedIn = true;
        authToken = authData.authToken();

        return "Logged in as " + username;
    }

    public String loginHandler(String[] args) throws ResponseException {
        if(args.length != 3) {
            return "Error: invalid command. Use:  login <USERNAME> <PASSWORD>";
        }

        String username = args[1];
        String password = args[2];

        UserData userData = new UserData(username, password, null);
        AuthData authData = server.login(userData);

        isLoggedIn = true;
        authToken = authData.authToken();

        return "Logged in as " + username;
    }

    public String logoutHandler(String[] args) throws ResponseException {
        if(args.length != 1) {
            return "Error: invalid command. Use: logout";
        }

        server.logout(authToken);

        isLoggedIn = false;
        authToken = null;

        return "Successfully logged out";
    }

    public String createHandler(String[] args) throws ResponseException {
        if(args.length != 2) {
            return "Error: invalid command. Use: create <GAME_NAME>";
        }

        String gameName = args[1];

        GameResult gameResult = server.createGame(authToken, gameName);

        return "Successfully created game " + gameName;
    }

    public String listHandler(String[] args) throws ResponseException {
        if(args.length != 1) {
            return "Error: invalid command. Use: list";
        }

        Collection<GameData> listGames = server.listGames(authToken);

        if (listGames == null || listGames.isEmpty()) {
            return "No games available.";
        }

        StringBuilder sb = new StringBuilder();
        int listCount = 1;

        for (GameData gameData : listGames) {
            String gameName = gameData.gameName();

            // temp id
            int gameID = gameData.gameID();

            String whiteUsername = gameData.whiteUsername() != null ? gameData.whiteUsername() : "<empty>";
            String blackUsername = gameData.blackUsername() != null ? gameData.blackUsername() : "<empty>";

            String gameInfo = String.format("%d. <<<GameID: %d>>> %s (White: %s, Black: %s)%n",
                    listCount++, gameID, gameName, whiteUsername, blackUsername);
            sb.append(gameInfo);
        }

        return sb.toString();
    }

    /*
    public String joinHandler(String[] args) throws ResponseException {
        if(args.length != 3) {
            return "Error: invalid command. Use: join <GAME_ID> [WHITE|BLACK]";
        }

        int gameID = ;
    }
    */













    public String help() {
        if(!isLoggedIn) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> : to create an account
                    login <USERNAME> <PASSWORD> : to play chess
                    quit : playing chess
                    help : with possible commands
                    """;
        } else {
            return """
                  create <GAME_NAME> - a game
                  list - games
                  join <GAME_ID> [WHITE|BLACK] - a game
                  observe <GAME_ID> - a game
                  logout - when you are done
                  quit - playing chess
                  help - with possible commands
            """;
        }
    }




















    private void printPrompt() {
        if(!isLoggedIn) {
            System.out.print("[LOGGED_OUT] >>> ");
        } else {
            System.out.print("[LOGGED_IN] >>> ");
        }
    }

}
