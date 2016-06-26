package Miscellaneous;

public enum Color {
    BLACK,
    WHITE,
    NONE;
    public Color opposite(){
        if (this == WHITE) return BLACK;
        if (this == BLACK) return WHITE;
        return NONE;
    }
}
