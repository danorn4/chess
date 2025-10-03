package chess;

import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currentPiece = board.getPiece(startPosition);

        if(currentPiece == null){
            return List.of();
        }

        Collection<ChessMove> allMoves = currentPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for(ChessMove move : allMoves) {
            ChessBoard boardCopy = new ChessBoard(board);

            ChessPiece pieceToMove = boardCopy.getPiece(move.getStartPosition());

            boardCopy.addPiece(move.getEndPosition(), pieceToMove);
            boardCopy.addPiece(move.getStartPosition(), null);

            if (!isKingInCheckOnBoard(boardCopy, pieceToMove.getTeamColor())) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    /** Helper for isInCheck() & validMoves()
     * allows for the deep copy board to be used
     *
     * @param currentBoard current board working in, whether copy/original
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    private boolean isKingInCheckOnBoard(ChessBoard currentBoard, TeamColor teamColor) {
        // find king's position
        ChessPosition kingPos = findKing(currentBoard, teamColor);

        // iterate through every piece, looking for possible enemies
        for(int row = 1; row <= 8; row++) {
            for(int col = 1; col <= 8; col++) {
                ChessPosition currentPos = new ChessPosition(row, col);
                ChessPiece currentPiece = currentBoard.getPiece(currentPos);
                if(currentPiece != null && currentPiece.getTeamColor() != teamColor) {
                    if(enemyAttacksKing(currentBoard, currentPos, currentPiece, kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isKingInCheckOnBoard(this.board, teamColor);
    }

    /**
     * Custom helper function that checks if an enemy piece has the king in its path
     * Helps isInCheck()
     *
     * @param currentPos position of enemy piece, we will iterate through the moves of this piece
     * @param currentPiece enemy piece which is being checked
     * @param kingPos position of current team king. if this king is in the enemy's path, return true
     */
    private boolean enemyAttacksKing(ChessBoard currentBoard, ChessPosition currentPos, ChessPiece currentPiece, ChessPosition kingPos) {
        for(ChessMove move : currentPiece.pieceMoves(currentBoard, currentPos)) {
            ChessPosition endPos = move.getEndPosition();
            if(endPos.equals(kingPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Custom helper function to find the position of the king
     * Helps isInCheck()
     *
     * @param teamColor which team to find the king for
     * @return ChessPosition position for the given team's king
     */
    private ChessPosition findKing(ChessBoard currentBoard, TeamColor teamColor) {
        for(int row = 1; row <= 8; row++) {
            for(int col = 1; col <= 8; col++) {
                ChessPosition currentPos = new ChessPosition(row, col);
                ChessPiece currentPiece = currentBoard.getPiece(currentPos);
                if((currentPiece != null) &&
                        (currentPiece.getTeamColor() == teamColor) &&
                        (currentPiece.getPieceType() == ChessPiece.PieceType.KING)) {
                    return currentPos;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }
}
