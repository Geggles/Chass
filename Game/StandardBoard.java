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
     * Color corresponds to the color of the army that starts out on the board
     */
    public StandardBoard(Color color) {
        super(names.get(color), color);
        setupPieces();
    }

    public Square getSquareAt(int[] coordinates) {
        int height = coordinates[0];
        int width = coordinates[1] % 8;
        return super.getSquareAt(new int[]{height, width});
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
            setPieceAt(new Piece(Value.PAWN), new int[]{row, column});
        }

        //other pieces
        if (color == Color.BLACK) {
            row = 7;
        } else {
            row = 0;
        }

        setPieceAt(new Piece(Value.ROOK), new int[]{row, 0});
        setPieceAt(new Piece(Value.KNIGHT), new int[]{row, 1});
        setPieceAt(new Piece(Value.BISHOP), new int[]{row, 2});
        setPieceAt(new Piece(Value.QUEEN), new int[]{row, 3});
        setPieceAt(new Piece(Value.KING), new int[]{row, 4});
        setPieceAt(new Piece(Value.BISHOP), new int[]{row, 5});
        setPieceAt(new Piece(Value.KNIGHT), new int[]{row, 6});
        setPieceAt(new Piece(Value.ROOK), new int[]{row, 7});

    }

    /**
     * Check whether the piece on 'square' is pinned or not.
     */
    public boolean isPinned(Square square) {
        StandardBoard board = (StandardBoard) square.board;
        Piece piece = board.getPieceOn(square);
        if (piece == null || piece.color == board.color) return false;
        for (Piece p :
                board.getAllPieces()) {
            if (p.value == Value.KING && p.color != piece.color) {
                Square kingSquare = board.getSquareOf(p);
                board.removePiece(piece);
                boolean result = board.isUnderAttack(kingSquare, piece.color);
                board.setPieceAt(piece, square);
                return result;
            }
        }
        throw new IllegalArgumentException("King has fallen over board!"); //<- should never be able to
        // be thrown, but IDE bugs me.
    }
    public boolean isPinned(Piece piece, StandardBoard board) {
        return isPinned(board.getSquareOf(piece));
    }
    /**
     * Check whether king is in check
     * */
    public boolean check(){
        Square kingSquare = null;
        for (Piece piece :
                getAllPieces()) {
            if (piece.value==Value.KING){
                kingSquare = getSquareOf(piece);
                break;
            }
        }
        return isUnderAttack(kingSquare, Color.oppositeColor(color));
    }
}