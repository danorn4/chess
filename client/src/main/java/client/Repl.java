package client;

import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import servicehelpers.JoinGameRequest;

import ui.BoardPrinter;

import ui.EscapeSequences;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;

public class Repl implements NotificationHandler {
    private final ServerFacade server;
    private final BoardPrinter boardPrinter = new  BoardPrinter();
    private boolean isLoggedIn = false;
    private String authToken = null;
    private Collection<GameData> listGames = null;

    private final String serverUrl;

    private WebSocketFacade ws;

    private static final String CMD_COLOR = EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_BLUE;
    private static final String DESC_COLOR = EscapeSequences.SET_TEXT_ITALIC + EscapeSequences.SET_TEXT_COLOR_MAGENTA;
    private static final String RESET = EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.RESET_TEXT_ITALIC + EscapeSequences.RESET_TEXT_COLOR;

    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public void run() {
        System.out.println("Welcome to 240 chess. Type Help to get started.");

        Scanner scanner = new Scanner(System.in);
        String result;

        do {
            printPrompt();

            // READ
            String input = scanner.nextLine();

            // EVAL
            result = eval(input);

            // PRINT
            System.out.println(result);

            // QUIT
        } while (!result.equals("quit"));
        System.out.println("Goodbye!");
    }

    private void gameplayLoop() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Different prompt to let them know they are in a game
            System.out.print("[GAMEPLAY] >>> ");
            String line = scanner.nextLine();

            // Call the specific gameplay evaluator
            String result = gameplayEval(line);

            // Print the result (unless it's empty/silent)
            if (!result.isEmpty()) {
                System.out.println(result);
            }

            // Break the loop if the user leaves (this returns to the Main Menu)
            if (result.equals("Left the game")) {
                break;
            }
        }
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


        } catch (ResponseException e) {
            // Just return the clean message from the facade
            return e.getMessage();
        } catch (Exception e) {
            // This catches any other *unexpected* bugs
            return e.getMessage();
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

        server.createGame(authToken, gameName);

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

        String numColor = EscapeSequences.SET_TEXT_COLOR_WHITE;
        String nameColor = EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_YELLOW;
        String infoColor = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        String playerColor = EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_WHITE;
        String reset = EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.RESET_TEXT_COLOR;


        for (GameData gameData : listGames) {
            String gameName = gameData.gameName();
            String whiteUsername = gameData.whiteUsername() != null ? gameData.whiteUsername() : "<empty>";
            String blackUsername = gameData.blackUsername() != null ? gameData.blackUsername() : "<empty>";

            sb.append(numColor);
            sb.append(String.format("%d. ", listCount++));

            sb.append(nameColor);
            sb.append(gameName);

            sb.append(infoColor);
            sb.append(" (White: ");

            sb.append(playerColor);
            sb.append(whiteUsername);

            sb.append(infoColor);
            sb.append(", Black: ");

            sb.append(playerColor);
            sb.append(blackUsername);

            sb.append(infoColor);
            sb.append(")");

            sb.append(reset);
            sb.append("\n");
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

        this.ws = new WebSocketFacade(this.serverUrl, this);        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameToJoin.gameID());
        ws.sendCommand(command);
        // -----------------------------

        // Removed manual board printing. The server will send LOAD_GAME.

        return ""; // Or return "Joining game..."
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

        // --- THIS IS THE NEW LOGIC ---
        this.ws = new WebSocketFacade(this.serverUrl, this);
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameToObserve.gameID());
        ws.sendCommand(command);

        // Note: We don't print the board here manually anymore.
        // The server will send a LOAD_GAME message via WebSocket, which triggers 'notify()'.
        // -----------------------------

        return "";
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

    private String gameplayEval(String input) {
        try {
            String[] args = input.split(" ");
            String command = args[0].toLowerCase();

            return switch (command) {
                case "help" -> helpGameplay();
                case "redraw" -> {
                    // Manually print the board using the saved game state
                    // (We need to save the 'currentGame' state when LOAD_GAME arrives to do this reliably,
                    // but for now we can just ask for a refresh or rely on the last printed board)
                    // Better yet: Just print the board stored in the Repl if you track it,
                    // OR just return "" and rely on the user knowing the state.
                    // Actually, the easiest way is to just output a newline for now, or fix the UI later to store the 'currentBoard'.
                    yield "Board redraw not fully implemented locally yet.";
                }
                case "leave" -> leaveHandler();
                case "move" -> makeMoveHandler(args);
                case "resign" -> resignHandler();
                case "highlight" -> highlightHandler(args);
                default -> "Unknown command. Type 'help' for options.";
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
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

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage loadMsg = new Gson().fromJson(new Gson().toJson(message), LoadGameMessage.class);

                System.out.println();
                boardPrinter.printBoard(loadMsg.getGame(), chess.ChessGame.TeamColor.WHITE);
                printPrompt();
            }
            case NOTIFICATION -> {
                NotificationMessage notification = new Gson().fromJson(new Gson().toJson(message), NotificationMessage.class);
                System.out.println();
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + notification.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
                printPrompt();
            }
            case ERROR -> {
                ErrorMessage error = new Gson().fromJson(new Gson().toJson(message), ErrorMessage.class);
                System.out.println();
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + error.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
                printPrompt();
            }
        }
    }
}
