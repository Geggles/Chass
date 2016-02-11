package Game;

import java.util.HashMap;

/**
 * Used for boards Alpha and Beta
 * */
public class StandardBoard extends PlayBoard {
    private static final HashMap<Color, String> names;
    static
    {
        names = new HashMap<>();
        names.put(Color.WHITE, "A");
        names.put(Color.BLACK, "B");
    }
    /**
     * Game.Color corresponds to the color of the army that starts out on the board
     */
    public StandardBoard(Color color) {
        super(names.get(color), color);
        setupPieces();
    }

    public Square getSquare(int[] coordinates) {
        int height = coordinates[0];
        int width = coordinates[1] % 8;
        return super.getSquare(new int[]{height, width});
    }

    private void setupPieces() {
        //pawns
        int row; // row that pawns start out on

        if (color == Color.BLACK) {
            row = 6;
        } else {
            row = 1;
        }

        for (int column = 0; column < 8; column++) {
            setPiece(new int[]{row, column}, new Piece(Value.PAWN));
        }

        //other pieces
        if (color == Color.BLACK) {
            row = 7;
        } else {
            row = 0;
        }

        setPiece(new int[]{row, 0}, new Piece(Value.ROOK));
        setPiece(new int[]{row, 1}, new Piece(Value.KNIGHT));
        setPiece(new int[]{row, 2}, new Piece(Value.BISHOP));
        setPiece(new int[]{row, 3}, new Piece(Value.QUEEN));
        setPiece(new int[]{row, 4}, new Piece(Value.KING));
        setPiece(new int[]{row, 5}, new Piece(Value.BISHOP));
        setPiece(new int[]{row, 6}, new Piece(Value.KNIGHT));
        setPiece(new int[]{row, 7}, new Piece(Value.ROOK));

    }

    /**
     * Check whether the piece on 'square' is pinned or not.
     */
    public boolean isPinned(Square square) {
        StandardBoard board = (StandardBoard) square.board;
        Piece piece = board.getPiece(square);
        if (piece == null || piece.color == board.color) return false;
        for (Piece p :
                board.getAllPieces()) {
            if (p.value == Value.KING && p.color != piece.color) {
                Square kingSquare = board.getSquare(p);
                board.removePiece(piece);
                boolean result = board.isUnderAttack(kingSquare, piece.color);
                board.setPiece(square, piece);
                return result;
            }
        }
        throw new IllegalArgumentException("King has fallen over board!"); //<- should never be able to
        // be thrown, but IDEA bugs me.
    }
    public boolean isPinned(Piece piece, StandardBoard board) {
        return isPinned(board.getSquare(piece));
    }
    /**
     * Check whether king is in check
     * */
    public boolean check(){
        Square kingSquare = null;
        for (Piece piece :
                getAllPieces()) {
            if (piece.value==Value.KING){
                kingSquare = getSquare(piece);
                break;
            }
        }
        return isUnderAttack(kingSquare, Color.oppositeColor(color));
    }
}