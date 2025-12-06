package ui;

import chess.ChessGame;
import model.GameData;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;

import java.util.Collection;

import static ui.EscapeSequences.*;

public class ClientDisplay {

    private final BoardPrinter boardPrinter = new BoardPrinter();

    public void printWelcome() {
        System.out.println("Welcome to 240 chess. Type Help to get started.");
    }

    public void printGoodbye() {
        System.out.println("Goodbye!");
    }

    public void printPrompt(boolean isLoggedIn, Integer currentGameID) {
        if (!isLoggedIn) {
            System.out.print("[LOGGED_OUT] >>> ");
        } else if (currentGameID != null) {
            System.out.print("[GAMEPLAY] >>> ");
        } else {
            System.out.print("[LOGGED_IN] >>> ");
        }
    }

    public void printResult(String result) {
        if (result != null && !result.isEmpty()) {
            System.out.println(result);
        }
    }

    public void printHelp(boolean isLoggedIn) {
        if (!isLoggedIn) {
            System.out.printf(
                    "%s  register <USERNAME> <PASSWORD> <EMAIL>%s %s: to create an account%n" +
                            "%s  login <USERNAME> <PASSWORD>%s %s: to play chess%n" +
                            "%s  quit%s %s: playing chess%n" +
                            "%s  help%s %s: with possible commands%s%n",
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA, RESET_TEXT_ITALIC + RESET_TEXT_COLOR
            );
        } else {
            System.out.printf(
                    "%s  create <GAME_NAME>%s %s: a game%n" +
                            "%s  list%s %s: games%n" +
                            "%s  join <GAME_ID> [WHITE|BLACK]%s %s: a game%n" +
                            "%s  observe <GAME_ID>%s %s: a game%n" +
                            "%s  logout%s %s: when you are done%n" +
                            "%s  quit%s %s: playing chess%n" +
                            "%s  help%s %s: with possible commands%s%n",
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                    SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                    SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA, RESET_TEXT_ITALIC + RESET_TEXT_COLOR
            );
        }
    }

    public void printGameplayHelp() {
        System.out.printf(
                "%s  redraw%s %s: Redraws the chess board%n" +
                        "%s  leave%s %s: Removes you from the game%n" +
                        "%s  move <START> <END> [PROMOTION]%s %s: Make a move (e.g., 'move e2 e4')%n" +
                        "%s  resign%s %s: Forfeit the game%n" +
                        "%s  highlight%s %s: Highlight legal moves for a piece%n" +
                        "%s  help%s %s: Show this message%s%n",
                SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE, RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE,
                RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR,
                SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA,
                RESET_TEXT_ITALIC + RESET_TEXT_COLOR
        );
    }

    public void printGameList(Collection<GameData> listGames) {
        if (listGames == null || listGames.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        int listCount = 1;
        String nameColor = SET_TEXT_BOLD + SET_TEXT_COLOR_YELLOW;
        String infoColor = SET_TEXT_COLOR_LIGHT_GREY;
        String playerColor = SET_TEXT_BOLD + SET_TEXT_COLOR_WHITE;
        String reset = RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR;

        for (GameData gameData : listGames) {
            String gameName = gameData.gameName();
            String white = gameData.whiteUsername() != null ? gameData.whiteUsername() : "<empty>";
            String black = gameData.blackUsername() != null ? gameData.blackUsername() : "<empty>";

            System.out.printf("%s%d. %s%s%s (White: %s%s%s, Black: %s%s%s)%s%n",
                    SET_TEXT_COLOR_WHITE, listCount++,
                    nameColor, gameName, infoColor,
                    playerColor, white, infoColor,
                    playerColor, black, infoColor,
                    reset);
        }
    }

    public void printBoard(ChessGame game, ChessGame.TeamColor perspective) {
        System.out.println();
        boardPrinter.printBoard(game, perspective);
        System.out.println();
    }

    public void printBoardWithHighlights(ChessGame game, ChessGame.TeamColor perspective,
                                         chess.ChessPosition pos,
                                         java.util.Collection<chess.ChessMove> moves) {
        System.out.println();
        boardPrinter.printBoardWithHighlights(game, perspective, pos, moves);
        System.out.println();
    }

    public void printNotification(NotificationMessage notification) {
        System.out.println();
        System.out.println(SET_TEXT_COLOR_BLUE + notification.getMessage() + RESET_TEXT_COLOR);
    }

    public void printError(ErrorMessage error) {
        System.out.println();
        System.out.println(SET_TEXT_COLOR_RED + error.getErrorMessage() + RESET_TEXT_COLOR);
    }
}