package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class BoardPrinter {
    private final PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

    public void printBoard(ChessGame game, ChessGame.TeamColor perspective) {
        ChessBoard board = game.getBoard();

        if (perspective == ChessGame.TeamColor.BLACK) {
            drawHeaders(false);
            drawRows(board, 8, -1);
            drawHeaders(false);
        } else {
            drawHeaders(true);
            drawRows(board, 1, 1);
            drawHeaders(true);
        }
        out.print(RESET_BG_COLOR);
    }

    private void drawHeaders(boolean isWhitePerspective) {
        out.print(RESET_BG_COLOR);
        out.print(SET_TEXT_COLOR_WHITE);
        out.print(SET_BG_COLOR_LIGHT_GREY);

        String[] headers = isWhitePerspective ?
                new String[]{"  a  ", "  b  ", "  c  ", "  d  ", "  e  ", "  f  ", "  g  ", "  h  "} :
                new String[]{"  h  ", "  g  ", "  f  ", "  e  ", "  d  ", "  c  ", "  b  ", "  a  "};

        out.print(EMPTY);
        for (String header : headers) {
            out.print(header);
        }
        out.print(EMPTY);
        out.println(RESET_BG_COLOR);
    }

    private void drawRows(ChessBoard board, int startRow, int increment) {
        for (int row = startRow; row >= 1 && row <= 8; row += increment) {
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.printf(" %d ", row);

            for (int col = 1; col <= 8; col++) {
                boolean isLightSquare = (row + col) % 2 != 0;
                if (isLightSquare) {
                    out.print(SET_BG_COLOR_WHITE);
                } else {
                    out.print(SET_BG_COLOR_DARK_GREY);
                }

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                out.print(getPieceString(piece));
            }

            out.print(SET_TEXT_COLOR_WHITE);
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.printf(" %d ", row);

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
