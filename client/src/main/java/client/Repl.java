package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import servicehelpers.JoinGameRequest;

import ui.BoardPrinter;
import ui.EscapeSequences;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;

public class Repl implements NotificationHandler {
    private final ServerFacade server;
    private final BoardPrinter boardPrinter = new BoardPrinter();
    private boolean isLoggedIn = false;
    private String authToken = null;
    private Collection<GameData> listGames = null;
    private Integer currentGameID = null;
    private ChessGame.TeamColor playerColor = null;
    private ChessGame currentGame = null;

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
            String input = scanner.nextLine();
            result = eval(input);
            if (result != null && !result.isEmpty()) {
                System.out.println(result);
            }
        } while (result == null || !result.equals("quit"));
        System.out.println("Goodbye!");
    }

    private void gameplayLoop() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("[GAMEPLAY] >>> ");
            String line = scanner.nextLine();
            String result = gameplayEval(line);

            if (result != null && !result.isEmpty()) {
                System.out.println(result);
            }

            if ("Left the game".equals(result)) {
                break;
            }
        }
    }

    public String eval(String input) {
        try {
            String[] args = input.split(" ");
            if (!isLoggedIn) {
                return preLoginEval(args);
            } else {
                return postLoginEval(args);
            }
        } catch (Exception e) {
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
        if (args.length != 3) {
            return "Error: invalid command. Use: join <GAME_ID> [WHITE|BLACK]";
        }

        String listNumber = args[1];
        GameData gameToJoin = findGameByList(listNumber);

        if (gameToJoin == null) {
            return "Error: Invalid game number. Run \"list\" to see list of available games.";
        }

        String playerColorStr = args[2].toUpperCase();
        if (!playerColorStr.equals("WHITE") && !playerColorStr.equals("BLACK")) {
            return "Error: Invalid Color. Must be WHITE or BLACK.";
        }

        this.playerColor = ChessGame.TeamColor.valueOf(playerColorStr);
        this.currentGameID = gameToJoin.gameID();

        JoinGameRequest joinGameRequest = new JoinGameRequest(playerColorStr, gameToJoin.gameID());
        server.joinGame(authToken, joinGameRequest);

        this.ws = new WebSocketFacade(this.serverUrl, this);
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameToJoin.gameID());
        ws.sendCommand(command);

        gameplayLoop();

        return "";
    }

    public String observeHandler(String[] args) throws ResponseException {
        if (args.length != 2) {
            return "Error: invalid command. Use: observe <GAME_ID>";
        }

        String listNumber = args[1];
        GameData gameToObserve = findGameByList(listNumber);

        if (gameToObserve == null) {
            return "Error: Invalid game number. Run \"list\" to see list of available games.";
        }

        this.playerColor = ChessGame.TeamColor.WHITE;
        this.currentGameID = gameToObserve.gameID();

        this.ws = new WebSocketFacade(this.serverUrl, this);
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameToObserve.gameID());
        ws.sendCommand(command);

        gameplayLoop();

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
                    if (currentGame != null) {
                        System.out.println();
                        boardPrinter.printBoard(currentGame, playerColor);
                        System.out.println();
                    }
                    yield "";
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


    private String leaveHandler() throws ResponseException {
        if (ws != null) {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID);
            ws.sendCommand(command);
        }

        this.currentGameID = null;
        this.playerColor = null;
        this.currentGame = null;

        return "Left the game";
    }


    private String resignHandler() throws ResponseException {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine().trim().toLowerCase();

        if (!answer.equals("yes")) {
            return "Resignation cancelled.";
        }

        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID);
        ws.sendCommand(command);

        return "Resigned.";
    }


    private String makeMoveHandler(String[] args) throws ResponseException {
        if (args.length < 3) {
            return "Error: Invalid move. Usage: move <START> <END> [PROMOTION]";
        }

        String startStr = args[1];
        String endStr = args[2];
        String promotion = (args.length > 3) ? args[3].toUpperCase() : null;

        ChessPosition start = parsePosition(startStr);
        ChessPosition end = parsePosition(endStr);

        if (start == null || end == null) {
            return "Error: Invalid position. Use format 'e2'.";
        }

        ChessPiece.PieceType promoPiece = null;
        if (promotion != null) {
            promoPiece = parsePieceType(promotion);
        }

        ChessMove move = new ChessMove(start, end, promoPiece);

        MakeMoveCommand command = new MakeMoveCommand(authToken, currentGameID, move);
        ws.sendCommand(command);

        return "";
    }

    private String highlightHandler(String[] args) {
        if (args.length != 2) {
            return "Error: Invalid command. Usage: highlight <POSITION> (e.g., highlight e2)";
        }

        if (currentGame == null) {
            return "Error: No game currently loaded. Join or observe a game first.";
        }

        String positionStr = args[1];
        ChessPosition position = parsePosition(positionStr);

        if (position == null) {
            return "Error: Invalid position. Use format 'e2'.";
        }

        if (currentGame.getBoard().getPiece(position) == null) {
            return "Error: No piece at " + positionStr;
        }

        Collection<ChessMove> validMoves = currentGame.validMoves(position);

        System.out.println();
        boardPrinter.printBoardWithHighlights(currentGame, playerColor, position, validMoves);
        System.out.println();

        return "";
    }

    private ChessPosition parsePosition(String positionStr) {
        try {
            if (positionStr.length() != 2) {
                return null;
            }

            char colChar = positionStr.charAt(0);
            char rowChar = positionStr.charAt(1);

            int col = colChar - 'a' + 1;

            int row = Character.getNumericValue(rowChar);

            if (col < 1 || col > 8 || row < 1 || row > 8) {
                return null;
            }

            return new ChessPosition(row, col);
        } catch (Exception e) {
            return null;
        }
    }

    private ChessPiece.PieceType parsePieceType(String pieceString) {
        if (pieceString == null) {
            return null;
        }
        try {
            return ChessPiece.PieceType.valueOf(pieceString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String helpGameplay() {
        return String.format(
                "%s  redraw%s %s: Redraws the chess board%n" +
                        "%s  leave%s %s: Removes you from the game%n" +
                        "%s  move <START> <END> [PROMOTION]%s %s: Make a move (e.g., 'move e2 e4')%n" +
                        "%s  resign%s %s: Forfeit the game%n" +
                        "%s  highlight%s %s: Highlight legal moves for a piece%n" +
                        "%s  help%s %s: Show this message",
                CMD_COLOR, RESET, DESC_COLOR,
                CMD_COLOR, RESET, DESC_COLOR,
                CMD_COLOR, RESET, DESC_COLOR,
                CMD_COLOR, RESET, DESC_COLOR,
                CMD_COLOR, RESET, DESC_COLOR,
                CMD_COLOR, RESET, DESC_COLOR
        );
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
        if (!isLoggedIn) {
            System.out.print("[LOGGED_OUT] >>> ");
        } else if (currentGameID != null) {
            System.out.print("[GAMEPLAY] >>> ");
        } else {
            System.out.print("[LOGGED_IN] >>> ");
        }
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage loadMsg = (LoadGameMessage) message;

                this.currentGame = loadMsg.getGame();
                System.out.println();

                ChessGame.TeamColor perspective = (this.playerColor != null) ? this.playerColor : ChessGame.TeamColor.WHITE;
                boardPrinter.printBoard(this.currentGame, perspective);
                printPrompt();
            }
            case NOTIFICATION -> {
                NotificationMessage notification = (NotificationMessage) message;
                System.out.println();
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + notification.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
                printPrompt();
            }
            case ERROR -> {
                ErrorMessage error = (ErrorMessage) message;
                System.out.println();
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + error.getErrorMessage() + EscapeSequences.RESET_TEXT_COLOR);
                printPrompt();
            }
        }
    }
}