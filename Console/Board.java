package Console;

import Game.Color;

import java.util.List;

public abstract class Board {
    protected char[] layout;
    protected final char wSS = '\u2B1C';
    protected final char bSS = '\u2B1B';
    private static final char[] squareColor = new char[] {'\u2B1C', '\u2B1B'};
    public Board(){

    }

    protected abstract int getSquareIndex(int row, int column);

    protected int getSquareIndex(List<Integer> coordinates){
        int row = coordinates.get(0);
        int column = coordinates.get(1);
        return getSquareIndex(row, column);
    }

    protected int getSquareIndex(String squareName){
        return getSquareIndex(Game.Board.getCoordinates(squareName));
    }

    /**
     * If character is null, square is set to empty
     * */
    protected void setSquare(int index, Character character){
        if (character == null){
            if (getSquareColor(index) == Color.WHITE) layout[index] = wSS;
            else layout[index] = bSS;
            return;
        }
        layout[index] = character;
    }

    protected void setSquare(int row, int column, Character character) {
        setSquare(getSquareIndex(row, column), character);
    }
    public char getSquare(int index){
        return layout[index];
    }

    public char[] getLayout(){
        return layout;
    }

    public Color getSquareColor(int index){
        int row = index / 9;
        int column = index % 9;
        int res = row%2 ^ column%2;
        if (res == 1){
            return Color.WHITE;
        }
        return Color.BLACK;
    }
}
