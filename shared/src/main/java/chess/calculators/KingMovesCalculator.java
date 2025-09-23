package chess.calculators;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        ChessGame.TeamColor myColor = board.getPiece(myPosition).getTeamColor();
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        // loop through all directions
        for(int[] dir: directions) {
            int newRow = myPosition.getRow() + dir[0];
            int newCol = myPosition.getColumn() + dir[1];

            // check that new move is on the board
            if((newRow >= 1 && newCol >= 1) && (newRow <= 8 && newCol <= 8)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);

                //if place isn't occupied
                if(board.getPiece(newPosition) == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    // if occupied by opponent
                    if (myColor != board.getPiece(newPosition).getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }
        return moves;

    }
}
