package Game;

public class Piece {
    public Colors color;
    public boolean canNullMove = true;
    public int pinned = 0;
    public final PieceValue value;

    public Piece(PieceValue value){
        this.value = value;
        if (value == PieceValue.KING){
            this.canNullMove = false;
        }
    }
}
