package Game;

public enum Color {
    BLACK,
    WHITE;
    public Color opposite(){
        if (this == WHITE) return BLACK;
        return WHITE;
    }
}
