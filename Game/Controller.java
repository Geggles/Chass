package Game;

import java.util.HashMap;

/**
 * this class will control the flow of the game
 * */
public class Controller {
    StandardBoard alpha;
    StandardBoard beta;
    SpecialBoard gamma;
    HashMap<Color, Prison> prisons;
    HashMap<Color, Airfield> airfields;
    Color turnPlayer = Color.WHITE;

    public Controller() {
        alpha = new StandardBoard(Color.WHITE);
        beta = new StandardBoard(Color.BLACK);
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
        move(source, destination, source.board);
    }

    public void move(Square source, Square destination, Board destinationBoard){
        PlayBoard sourceBoard = (PlayBoard) source.board;
        Piece piece = sourceBoard.getPieceOn(source);

        sourceBoard.removePieceFrom(source);
        if(destinationBoard.getPieceOn(sourceBoard.getCoordinateOf(destination)) == null){
            destinationBoard.setPieceAt(piece, sourceBoard.getCoordinateOf(destination));
        }else {
            sourceBoard.setPieceAt(piece, destination);
        }
    }

    /**
     * Do special moves en passant or casteling as applicable.
     * Return true if special move has been made; false otherwise.
     * */
    public boolean doSpecialMove(Square source, Square destination){
        StandardBoard board = (StandardBoard) source.board;
        return false;
    }

    public void castle(Color color, Value side){
        Piece king;
        Piece rook;
        if (color == Color.WHITE){
            king = alpha.getPieceOn(new int[] {0, 4});
            alpha.removePiece(king);
            if (side == Value.QUEEN){
                rook = alpha.getPieceOn(new int[] {0, 0});
                alpha.removePiece(rook);
                alpha.setPieceAt(king, new int[] {0, 2});
                alpha.setPieceAt(rook, new int[] {0, 4});
            }else {
            rook = alpha.getPieceOn(new int[] {0, 7});
            alpha.removePiece(rook);
            alpha.setPieceAt(king, new int[] {0, 6});
            alpha.setPieceAt(rook, new int[] {0, 5});
            }

        }else {
            king = beta.getPieceOn(new int[]{7, 4});
            beta.removePiece(king);
            if (side == Value.QUEEN) {
                rook = beta.getPieceOn(new int[]{7, 0});
                beta.removePiece(rook);
                beta.setPieceAt(king, new int[]{7, 2});
                beta.setPieceAt(rook, new int[]{7, 4});
            }
            rook = beta.getPieceOn(new int[]{7, 7});
            beta.removePiece(rook);
            beta.setPieceAt(king, new int[]{7, 6});
            beta.setPieceAt(rook, new int[]{7, 5});
        }
        king.canCastle = false;
        rook.canCastle = false;
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
            if (piece.value == Value.KING &&
                (standardBoard.isUnderAttack(destination, Color.switchColors(piece.color)))) {
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
        ExtraBoard sourceBoard = (ExtraBoard)source.board;
        if (destinationBoard.getPieceOn(sourceBoard.getCoordinateOf(destination))!=null){
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
