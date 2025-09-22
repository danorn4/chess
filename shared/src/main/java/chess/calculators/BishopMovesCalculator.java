package chess.calculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        // check moves top-right:
        int row = myPosition.getRow() + 1;
        int col = myPosition.getColumn() + 1;
        while(row <= 8 && col <= 8) {
            ChessPosition newPosition = new ChessPosition(row, col);
            moves.add(new ChessMove(myPosition, newPosition, null));
            row++;
            col++;
        }

        // check moves top-left:
        row = myPosition.getRow() + 1;
        col = myPosition.getColumn() - 1;
        while(row <= 8 && col > 0) {
            ChessPosition newPosition = new ChessPosition(row, col);
            moves.add(new ChessMove(myPosition, newPosition, null));
            row++;
            col--;
        }

        // check moves bottom-left:
        row = myPosition.getRow() - 1;
        col = myPosition.getColumn() - 1;
        while(row > 0 && col > 0) {
            ChessPosition newPosition = new ChessPosition(row, col);
            moves.add(new ChessMove(myPosition, newPosition, null));
            row--;
            col--;
        }

        // check moves bottom-right:
        row = myPosition.getRow() - 1;
        col = myPosition.getColumn() + 1;
        while(row > 0 && col <= 8) {
            ChessPosition newPosition = new ChessPosition(row, col);
            moves.add(new ChessMove(myPosition, newPosition, null));
            row--;
            col++;
        }

        return moves;
    }
}
