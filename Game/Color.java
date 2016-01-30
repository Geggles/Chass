package Game;

public enum Color {
    BLACK,
    WHITE;
    static Color switchColors(Color color){
        if (color == WHITE) return BLACK;
        return WHITE;
    }
}
