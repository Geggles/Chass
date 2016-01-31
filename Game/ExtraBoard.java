package Game;

/**
 * Used for Prisons and Airfields
 */
public abstract class ExtraBoard extends Board{
    public ExtraBoard(String name, Color color){
        super(name, color);
    }

    public Piece getPiece(Value value){
        for (Piece piece :
                getAllPieces()) {
            if (piece.value == value) return piece;
        }
        return null;
    }
}
