package chess.calculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor myColor = board.getPiece(myPosition).getTeamColor();
        int forwardDirection;
        int startRow;
        int promotionRow;

        // determine moves
        if (myColor == ChessGame.TeamColor.WHITE) {
            forwardDirection = 1;  // White moves "up" the board (row increases)
            startRow = 2;
            promotionRow = 8;
        } else { // BLACK
            forwardDirection = -1; // Black moves "down" the board (row decreases)
            startRow = 7;
            promotionRow = 1;
        }

        // One-Step Moves:
        int oneStepRow = myPosition.getRow() + forwardDirection;
        int oneStepCol = myPosition.getColumn();

        if (oneStepRow >= 1 && oneStepRow <= 8) {
            ChessPosition oneStepPos = new ChessPosition(oneStepRow, oneStepCol);
            if (board.getPiece(oneStepPos) == null) {
                // Square is empty, so it's a valid move.
                // Check for promotion
                if (oneStepRow == promotionRow) {
                    moves.add(new ChessMove(myPosition, oneStepPos, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, oneStepPos, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, oneStepPos, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, oneStepPos, ChessPiece.PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(myPosition, oneStepPos, null));
                }
            }
        }


        // Two-Step moves
        if (myPosition.getRow() == startRow) {
            int twoStepsRow = myPosition.getRow() + 2 * forwardDirection;
            ChessPosition oneStepPos = new ChessPosition(oneStepRow, oneStepCol); // From single-step
            ChessPosition twoStepsPos = new ChessPosition(twoStepsRow, oneStepCol);

            // Check that both squares are empty
            if (board.getPiece(oneStepPos) == null && board.getPiece(twoStepsPos) == null) {
                moves.add(new ChessMove(myPosition, twoStepsPos, null));
            }
        }

        // Captures
        int[] captureCols = {myPosition.getColumn() - 1, myPosition.getColumn() + 1 };
        for (int captureCol : captureCols) {
            if (captureCol >= 1 && captureCol <= 8) {
                ChessPosition capturePos = new ChessPosition(oneStepRow, captureCol);
                ChessPiece pieceToCapture = board.getPiece(capturePos);

                // Check for an enemy piece on the capture square
                if (pieceToCapture != null && pieceToCapture.getTeamColor() != myColor) {
                    // Check for promotion on capture
                    if (oneStepRow == promotionRow) {
                        moves.add(new ChessMove(myPosition, capturePos, ChessPiece.PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, capturePos, ChessPiece.PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, capturePos, ChessPiece.PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, capturePos, ChessPiece.PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, capturePos, null));
                    }
                }
            }
        }

        return moves;
    }
}
