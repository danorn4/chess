package client;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.servicehelpers.GameResult;
import service.servicehelpers.JoinGameRequest;

import ui.EscapeSequences;

import java.util.Collection;
import java.util.Scanner;

public class Repl {
    private final ServerFacade server;
    private boolean isLoggedIn = false;
    private String authToken = null;
    private Collection<GameData> listGames = null;

    private static final String CMD_COLOR = EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_BLUE;
    private static final String DESC_COLOR = EscapeSequences.SET_TEXT_ITALIC + EscapeSequences.SET_TEXT_COLOR_MAGENTA;
    private static final String RESET = EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.RESET_TEXT_ITALIC + EscapeSequences.RESET_TEXT_COLOR;

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
            case "join" -> joinHandler(args);
            case "observe" -> observeHandler(args);
            case "quit" -> "quit";
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
        this.listGames = listGames;

        if (listGames == null || listGames.isEmpty()) {
            return "No games available.";
        }

        StringBuilder sb = new StringBuilder();
        int listCount = 1;

        for (GameData gameData : listGames) {
            String gameName = gameData.gameName();

            String whiteUsername = gameData.whiteUsername() != null ? gameData.whiteUsername() : "<empty>";
            String blackUsername = gameData.blackUsername() != null ? gameData.blackUsername() : "<empty>";

            String gameInfo = String.format("%d. %s (White: %s, Black: %s)%n",
                    listCount++, gameName, whiteUsername, blackUsername);
            sb.append(gameInfo);
        }

        return sb.toString();
    }


    public String joinHandler(String[] args) throws ResponseException {
        if(args.length != 3) {
            return "Error: invalid command. Use: join <GAME_ID> [WHITE|BLACK]";
        }

        String listNumber = args[1];
        GameData gameToJoin = findGameByList(listNumber);

        if(gameToJoin == null) {
            return "Error: Invalid game number. Run \"list\" to see list of available games.";
        }

        String playerColor = args[2].toUpperCase();
        if(!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
            return "Error: Invalid Color. Must be WHITE or BLACK.";
        }

        JoinGameRequest joinGameRequest = new JoinGameRequest(playerColor, gameToJoin.gameID());
        server.joinGame(authToken, joinGameRequest);

        return "Successfully joined game " + gameToJoin.gameName() + " as " + playerColor;
    }

    public String observeHandler(String[] args) throws ResponseException {
        if(args.length != 2) {
            return "Error: invalid command. Use: observe <GAME_ID>";
        }

        String listNumber = args[1];
        GameData gameToObserve = findGameByList(listNumber);

        if(gameToObserve == null) {
            return "Error: Invalid game number. Run \"list\" to see list of available games.";
        }

        JoinGameRequest joinGameRequest = new JoinGameRequest(null, gameToObserve.gameID());
        server.joinGame(authToken, joinGameRequest);

        return "Successfully observing game " + gameToObserve.gameName();
    }















    private GameData findGameByList(String listIndex) {
        if(listGames == null || listGames.isEmpty()) {
            return null;
        }

        try {
            int listNumber = Integer.parseInt(listIndex);

            if (listNumber < 1 || listNumber > listGames.size()) {
                return null;
            }
            int listCounter = 1;
            for(GameData gameData : listGames) {
                if(listCounter == listNumber) {
                    return gameData;
                }
                listCounter++;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    public String help() {
        if(!isLoggedIn) {
            return String.format(
                    "%s  register <USERNAME> <PASSWORD> <EMAIL>%s %s: to create an account%n" +
                            "%s  login <USERNAME> <PASSWORD>%s %s: to play chess%n" +
                            "%s  quit%s %s: playing chess%n" +
                            "%s  help%s %s: with possible commands%s",
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR, RESET
            );
        } else {
            return String.format(
                    "%s  create <GAME_NAME>%s %s: a game%n" +
                            "%s  list%s %s: games%n" +
                            "%s  join <GAME_ID> [WHITE|BLACK]%s %s: a game%n" +
                            "%s  observe <GAME_ID>%s %s: a game%n" +
                            "%s  logout%s %s: when you are done%n" +
                            "%s  quit%s %s: playing chess%n" +
                            "%s  help%s %s: with possible commands%s",
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR,
                    CMD_COLOR, RESET, DESC_COLOR, RESET
            );
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
