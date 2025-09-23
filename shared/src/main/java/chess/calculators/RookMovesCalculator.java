package chess.calculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        /* check moves top: */
        int row = myPosition.getRow() + 1;
        int col = myPosition.getColumn();
        while(row <= 8) {
            ChessPosition newPosition = new ChessPosition(row, col);
            // piece is in the way:
            if(board.getPiece(newPosition) != null) {
                if (board.getPiece(newPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            } else {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            row++;
        }

        /* check moves left: */
        row = myPosition.getRow();
        col = myPosition.getColumn() - 1;
        while(col >= 1) {
            ChessPosition newPosition = new ChessPosition(row, col);
            // piece is in the way:
            if(board.getPiece(newPosition) != null) {
                if (board.getPiece(newPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            } else {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            col--;
        }

        /* check moves bottom */
        row = myPosition.getRow() - 1;
        col = myPosition.getColumn();
        while(row >= 1) {
            ChessPosition newPosition = new ChessPosition(row, col);
            // piece is in the way:
            if(board.getPiece(newPosition) != null) {
                if (board.getPiece(newPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            } else {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            row--;
        }

        /* check moves right: */
        row = myPosition.getRow();
        col = myPosition.getColumn() + 1;
        while(col <= 8) {
            ChessPosition newPosition = new ChessPosition(row, col);
            // piece is in the way:
            if(board.getPiece(newPosition) != null) {
                if (board.getPiece(newPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            } else {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            col++;
        }


        return moves;
    }
}
