package Game;

import java.util.HashMap;

/**
 * this class will control the flow of the game
 * */
public class Controller {
    StandardBoard alpha;
    StandardBoard beta;
    SpecialBoard gamma;
    HashMap<Colors, Prison> prisons;
    HashMap<Colors, Airfield> airfields;
    Colors turnPlayer = Colors.WHITE;

    public Controller() {
        alpha = new StandardBoard(Colors.WHITE);
        beta = new StandardBoard(Colors.BLACK);
        gamma = new SpecialBoard();
        prisons = new HashMap<>(2);
        airfields = new HashMap<>(2);
    }

    public void newGame(){
        setupPieces();
    }

    private void setupPieces(){

    }

    public void move(Square source, Square destination){

    }

    /**
     * Sufficient for Alpha and Beta
     * */
    public boolean validMove(Square source, Square destination){
        Board board = source.board;
        Piece piece = board.getPieceOn(source);
        Piece destinationPiece = board.getPieceOn(destination);
        if (piece == null) return false; // no piece to move
        if (source == destination && !piece.canNullMove) return false; // can't null move
        if (destinationPiece != null && destinationPiece.color == piece.color) return false;
        try { // for alpha and beta
            StandardBoard standardBoard = (StandardBoard)board;
            if (standardBoard.isPinned(source)) return false;
            if (piece.value == PieceValue.KING &&
                (standardBoard.isUnderAttack(destination, Colors.switchColors(piece.color)))) {
                    return false;
            }
            return true;
        }catch (ClassCastException exception){
            return true;
        }
    }

    /**
     * Only for Gamma
     * Non-capture move
     * */
    public boolean validMove(Square source,
                             Square destination,
                             PlayBoard destinationBoard) {

        if (!validMove(source, destination)) return false;
        if (destinationBoard.getPieceOn(source.board.getCoordinateOf(destination))!=null){
            return false;
        }
        return true;
    }

    /**
     * Only for Gamma
     * Different destination boards are for Pieces after Capture
     * */
    public boolean validMove(Square source,
                             Square destination,
                             PlayBoard destinationBoardOpponent,
                             PlayBoard destinationBoardPlayer){
        if (!validMove(source, destination, destinationBoardOpponent)) return false;
        if (!validMove(source, destination, destinationBoardPlayer)) return false;

        return true;
    }
}
