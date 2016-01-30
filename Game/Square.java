package Game;

public class Square {
    private Colors color;
    private Board board;

    public Square(){

    }

    public void setColor(Colors color){
        this.color = color;
    }
    public Colors getColor(){
        return color;
    }

    public void setBoard(Board board){
        this.board = board;
    }
    public Board getBoard(){
        return board;
    }
}
