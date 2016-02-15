package Game;

public class Piece {
    private Color color;
    public final Value value;

    public Piece(Value value, Color color){
        this.value = value;
        this.color = color;
    }

    public Color getColor(){
        return color;
    }

    public void switchColor(){
        color = color.opposite();
    }
}
