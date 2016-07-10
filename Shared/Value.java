package Shared;

public enum Value {
    PAWN(1, 'P', "Pawn", '\u2659', '\u265F'),
    BISHOP(3, 'B', "Bishop", '\u2657', '\u265D'),
    KNIGHT(3, 'N', "Knight", '\u2658', '\u265E'),
    ROOK(5, 'R', "Rook", '\u2656', '\u265C'),
    QUEEN(9, 'Q', "Queen", '\u2655', '\u265B'),
    KING(-1, 'K', "King", '\u2654', '\u265A');

    public final int value;
    public final Character name;
    public final String fullName;
    private final Character whiteSymbol;
    private final Character blackSymbol;

    Value(int value, Character name, String fullName, Character whiteSymbol, Character blackSymbol){
        this.value = value;
        this.name = name;
        this.fullName = fullName;
        this.whiteSymbol = whiteSymbol;
        this.blackSymbol = blackSymbol;
    }

    public char getSymbol(Color color){
        if (color == Color.WHITE) return whiteSymbol;
        return this.blackSymbol;
    }

    public static Value of(Character name){
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
