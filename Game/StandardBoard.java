package Game;

/**
 * Used for boards Alpha and Beta
 * */
public class StandardBoard extends PlayBoard{
    /**
     * Color corresponds to the color of the army that starts out on the board
     * */
    public StandardBoard(Colors color){
        setupPieces(color);
    }

    private void setupPieces(Colors color){
        //pawns
        int row; // row that pawns start out on
        Square square;
        Piece piece;

        if (color == Colors.BLACK){
            row = 6;
        }else{
            row = 1;
        }

        for (int column=0; column<8; column++){
            setPieceAt(new Piece(PieceValue.PAWN), new int[] {row, column});
        }

        //other pieces
        if (color == Colors.BLACK){
            row = 7;
        }else{
            row = 0;
        }

        setPieceAt(new Piece(PieceValue.ROOK), new int[] {row, 0});
        setPieceAt(new Piece(PieceValue.KNIGHT), new int[] {row, 1});
        setPieceAt(new Piece(PieceValue.BISHOP), new int[] {row, 2});
        setPieceAt(new Piece(PieceValue.QUEEN), new int[] {row, 3});
        setPieceAt(new Piece(PieceValue.KING), new int[] {row, 4});
        setPieceAt(new Piece(PieceValue.BISHOP), new int[] {row, 5});
        setPieceAt(new Piece(PieceValue.KNIGHT), new int[] {row, 6});
        setPieceAt(new Piece(PieceValue.ROOK), new int[] {row, 7});

    }
}
