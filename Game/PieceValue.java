package Game;

public enum PieceValue {
    PAWN(1),
    BISHOP(3),
    KNIGHT(3),
    ROOK(5),
    QUEEN(9),
    KING(-1);

    public final int value;

    PieceValue(int value){
        this.value = value;
    }

/*    public int getValue(){
        return this.value;
    }*/
}
