package Game;

public enum Color {
    BLACK,
    WHITE;
    static Color oppositeColor(Color color){
        if (color == WHITE) return BLACK;
        return WHITE;
    }
}
