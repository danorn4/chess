package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class BoardPrinter {
    private final PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

    public void printBoard(ChessGame game, ChessGame.TeamColor perspective) {
        printBoardWithHighlights(game, perspective, null, null);
    }

    public void printBoardWithHighlights(ChessGame game, ChessGame.TeamColor perspective, ChessPosition source, Collection<ChessMove> moves) {
        ChessBoard board = game.getBoard();

        Set<ChessPosition> validDestinations = new HashSet<>();
        if (moves != null) {
            for (ChessMove move : moves) {
                validDestinations.add(move.getEndPosition());
            }
        }

        if (perspective == ChessGame.TeamColor.BLACK) {
            drawHeaders(false);
            drawRows(board, 1, 1, source, validDestinations);
            drawHeaders(false);
        } else {
            drawHeaders(true);
            drawRows(board, 8, -1, source, validDestinations);
            drawHeaders(true);
        }
        out.print(RESET_BG_COLOR);
    }

    private void drawHeaders(boolean isWhitePerspective) {
        out.print(RESET_BG_COLOR);
        out.print(SET_TEXT_COLOR_WHITE);
        out.print(SET_BG_COLOR_LIGHT_GREY);

        String[] headers = isWhitePerspective ?
                new String[]{"a", "b", "c", "d", "e", "f", "g", "h"} :
                new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};

        out.print("\u200A"+EMPTY);
        for (String header : headers) {
            out.print(HEADER_SPACE + header + HEADER_SPACE);
        }
        out.print(EMPTY+"\u200A");

        out.println(RESET_BG_COLOR);
    }

    private void drawRows(ChessBoard board, int startRow, int increment, ChessPosition source, Set<ChessPosition> highlights) {
        for (int row = startRow; row >= 1 && row <= 8; row += increment) {
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(SET_BG_COLOR_LIGHT_GREY);

            out.printf("\u2003%d\u2003", row);

            int colStart = (startRow == 8) ? 1 : 8;
            int colEnd = (startRow == 8) ? 9 : 0;
            int colIncrement = (startRow == 8) ? 1 : -1;

            for (int col = colStart; col != colEnd; col += colIncrement) {
                ChessPosition currentPos = new ChessPosition(row, col);

                boolean isLightSquare = (row + col) % 2 != 0;
                String bgColor;

                if (source != null && source.equals(currentPos)) {
                    bgColor = SET_BG_COLOR_YELLOW;
                } else if (highlights != null && highlights.contains(currentPos)) {
                    bgColor = isLightSquare ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
                } else {
                    bgColor = isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREY;
                }

                out.print(bgColor);

                ChessPiece piece = board.getPiece(currentPos);
                out.print(getPieceString(piece));
            }

            out.print(SET_TEXT_COLOR_WHITE);
            out.print(SET_BG_COLOR_LIGHT_GREY);

            out.printf("\u2003%d\u2003", row);

            out.println(RESET_BG_COLOR);
        }
    }

    private String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        String pieceStr = "";
        switch (piece.getPieceType()) {
            case KING -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_KING : BLACK_KING;
            case QUEEN -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_PAWN : BLACK_PAWN;
        }
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_BLUE : SET_TEXT_COLOR_RED;
        return color + pieceStr;
    }
}