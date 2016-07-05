package Game;

import Shared.Color;

public class Square {
    public final Color color;
    public final Board board;
    public final int row;
    public final int column;
    public final int[] coordinates;

    public Square(Board board, Color color, int row, int column){
        this.color = color;
        this.board = board;
        this.row = row;
        this.column = column;
        this.coordinates = new int[]{row, column};
    }
}
