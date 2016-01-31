package Game;

public class Piece {
    public Color color;
    public boolean canNullMove = true;
    public boolean canCastle = true;
    public final Value value;

    public Piece(Value value){
        this.value = value;
        if (value == Value.KING){
            this.canNullMove = false;
        }
    }
}
