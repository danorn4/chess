package chess.calculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Queens perform the same moves as a bishop and a rook
 * Returns the lists of moves from both a bishop and a rook
 * Combined into one whole list.
 */
public class QueenMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        PieceMovesCalculator rookCalculator = new RookMovesCalculator();
        moves.addAll(rookCalculator.pieceMoves(board, myPosition));

        PieceMovesCalculator bishopCalculator = new BishopMovesCalculator();
        moves.addAll(bishopCalculator.pieceMoves(board, myPosition));

        return moves;
    }
}
