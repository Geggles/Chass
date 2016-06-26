package Validation;

import Game.*;
import Miscellaneous.Color;

import java.util.List;

public class IncrementalMoveValidator {
    private Controller gameController;
    private Board sourceBoard = null;
    private Square sourceSquare = null;
    private Square destinationSquare1 = null;
    private Square destinationSquare2 = null;
    private Square destinationSquare3 = null;
    private Board destinationBoard1 = null;
    private Board destinationBoard2 = null;
    private Board destinationBoard3 = null;
    private Value promotion = null;
    IncrementalMoveValidator(Controller gameController){
        this.gameController = gameController;
    }
    public void setSourceBoard(Board board){
        sourceBoard = board;
    }
    public void setSourceSquare(Square square){
        sourceSquare = square;
    }
    public void setDestinationSquare1(Square square){
        destinationSquare1 = square;
    }
    public void setDestinationSquare2(Square square){
        destinationSquare2 = square;
    }
    public void setDestinationSquare3(Square square){
        destinationSquare3 = square;
    }
    public void setDestinationBoard1(Board board){
        destinationBoard1 = board;
    }
    public void setDestinationBoard2(Board board){
        destinationBoard2 = board;
    }
    public void setDestinationBoard3(Board board){
        destinationBoard3 = board;
    }

    public void setPromotion(Value value){
        promotion = value;
    }

    public boolean validMove(){
        if (sourceBoard == null) return false;
        if (sourceSquare == null) return false;
        if (destinationSquare1 == null) return false;
        Piece sourcePiece = sourceBoard.getPiece(sourceSquare);

        boolean canGoTo = false;
        for (Square testSquare :
                sourceBoard.canGoTo(sourcePiece)) {
            if (testSquare == sourceSquare) {
                canGoTo = true;
                break;
            }
        }
        if(!canGoTo) return false;

        if(sourcePiece == null) return false;
        if(sourcePiece.getColor() != gameController.getCurrentPlayer()) return false;
        Piece destinationPiece = sourceBoard.getPiece(destinationSquare1);
        if(destinationPiece != null &&
                destinationPiece.getColor() == gameController.getCurrentPlayer()) return false;

        //promotion
        int baseRow = 0;
        List<Integer> sourceCoordinates =
                sourceBoard.getCoordinates(sourceSquare);
        int sourceRow = sourceCoordinates.get(0);
        int sourceColumn = sourceCoordinates.get(1);
        List<Integer> destinationCoordinates =
                sourceBoard.getCoordinates(destinationSquare1);
        int destinationRow = destinationCoordinates.get(0);
        int destinationColumn = destinationCoordinates.get(1);
        if (sourceBoard.color == Color.BLACK) baseRow = 7;
        return !(sourcePiece.value == Value.PAWN && destinationRow == baseRow);
    }
}
