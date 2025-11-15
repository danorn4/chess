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

        // --- FIX 1 ---
        String[] headers = isWhitePerspective ?
                new String[]{"\u2003a\u2003", "\u2003b\u2003", "\u2003c\u2003", "\u2003d\u2003", "\u2003e\u2003", "\u2003f\u2003", "\u2003g\u2003", "\u2003h\u2003"} :
                new String[]{"\u2003h\u2003", "\u2003g\u2003", "\u2003f\u2003", "\u2003e\u2003", "\u2003d\u2003", "\u2003c\u2003", "\u2003b\u2003", "\u2003a\u2003"};

        // --- FIX 2 ---
        out.print(EMPTY); // Use wide corner
        for (String header : headers) {
            out.print(header);
        }
        out.print(EMPTY); // Use wide corner

        out.println(RESET_BG_COLOR);
    }

    private void drawRows(ChessBoard board, int startRow, int increment) {
        for (int row = startRow; row >= 1 && row <= 8; row += increment) {
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(SET_BG_COLOR_LIGHT_GREY);

            // --- FIX 3 ---
            out.printf("\u2003%d\u2003", row); // Use wide em-space for padding

            for (int col = 1; col <= 8; col++) {
                // ... (your square/piece logic is correct)
                boolean isLightSquare = (row + col) % 2 != 0;
                if (isLightSquare) {
                    out.print(SET_BG_COLOR_WHITE);
                } else {
                    out.print(SET_BG_COLOR_DARK_GREY);
                }
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if(piece != null) {
                    out.print(getPieceString(piece));
                } else {
                    out.print(EMPTY);
                }
                // out.print(getPieceString(piece));
            }

            out.print(SET_TEXT_COLOR_WHITE);
            out.print(SET_BG_COLOR_LIGHT_GREY);

            // --- FIX 4 ---
            out.printf("\u2003%d\u2003", row); // Use wide em-space for padding

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
