package Game;

import Miscellaneous.Color;

public class Square {
    public final Color color;
    public final Board board;

    public Square(Color color, Board board){
        this.color = color;
        this.board = board;
    }
}
