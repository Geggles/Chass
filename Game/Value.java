package Game;

public enum Value {
    PAWN(1, 'P'),
    BISHOP(3, 'B'),
    KNIGHT(3, 'N'),
    ROOK(5, 'R'),
    QUEEN(9, 'Q'),
    KING(-1, 'K');

    public final int value;
    public final Character name;

    Value(int value, Character name){
        this.value = value;
        this.name = name;
    }

    public static Value getValueFromName(Character name){
        switch (name){
            case 'P': return PAWN;
            case 'B': return BISHOP;
            case 'N': return KNIGHT;
            case 'R': return ROOK;
            case 'Q': return QUEEN;
            case 'K': return KING;
            default: return null;
        }
    }

/*    public int getValue(){
        return this.value;
    }*/
}
