package Game;

public class Piece {
    private Colors color;
    private PieceValue value;

    public Piece(PieceValue value){
        this.value = value;
    }

    public void setColor(Colors color){
        this.color = color;
    }

    public Colors getColor(){
        return color;
    }
}
