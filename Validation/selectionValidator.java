package Validation;

import Game.*;

import java.util.Arrays;

public class SelectionValidator {
    /**
     * Validate a selection (e.g. after mouse hover) by building all possible moves and checking
     * their validity.
     */
    public SelectionValidator() {

    }
    public static boolean selectSourceBoard(Board board, Color turnPlayer){
        Piece[] pieces = board.getPieces(null, turnPlayer);  // all pieces
        if (pieces.length == 0) return false;
        Piece[] movablePieces = Arrays.stream(pieces).filter(
                piece -> board.canGoTo(piece).length > 0
        ).toArray(size -> new Piece[size]);
        return movablePieces.length != 0;
    }
    public static boolean selectSourceSquare(Square square, Color turnPlayer){
        Piece piece = square.board.getPiece(square);
        if (piece == null) return false;
        if (piece.getColor() != turnPlayer) return false;
        return square.board.canGoTo(piece).length != 0;
    }
    public static boolean selectDestinationSquare(Square sourceSquare,
                                                  Square destinationSquare, Color turnPlayer){
        return false;
    }
}
