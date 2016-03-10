package Console;

import Game.Color;
import Game.Value;

public class LargeBoard extends Board{
    public final Color color;
    private char[] layout = new char[] {
            wSS,bSS,wSS,bSS,wSS,bSS,wSS,bSS,'\n',
            bSS,wSS,bSS,wSS,bSS,wSS,bSS,wSS,'\n',
            wSS,bSS,wSS,bSS,wSS,bSS,wSS,bSS,'\n',
            bSS,wSS,bSS,wSS,bSS,wSS,bSS,wSS,'\n',
            wSS,bSS,wSS,bSS,wSS,bSS,wSS,bSS,'\n',
            bSS,wSS,bSS,wSS,bSS,wSS,bSS,wSS,'\n',
            wSS,bSS,wSS,bSS,wSS,bSS,wSS,bSS,'\n',
            bSS,wSS,bSS,wSS,bSS,wSS,bSS,wSS,'\n'};

    public LargeBoard(Color color){
        this.color = color;
        setupPieces(color);
    }
    private void setupPieces(Color color){
        if (color == Color.NONE) return;
        int index;

        //pawns
        int row; // row that pawns start out on

        if (color == Color.BLACK) {
            row = 6;
        } else {
            row = 1;
        }

        for (int column = 0; column < 8; column++) {
            index = getSquareIndex(row, column);
            setSquare(index, Value.PAWN.getSymbol(color));
        }

        //other pieces
        if (color == Color.BLACK) {
            row = 7;
        } else {
            row = 0;
        }

        setSquare(row, 0, Value.ROOK.getSymbol(color));
        setSquare(row, 1, Value.KNIGHT.getSymbol(color));
        setSquare(row, 2, Value.BISHOP.getSymbol(color));
        setSquare(row, 3, Value.QUEEN.getSymbol(color));
        setSquare(row, 4, Value.KING.getSymbol(color));
        setSquare(row, 5, Value.BISHOP.getSymbol(color));
        setSquare(row, 6, Value.KNIGHT.getSymbol(color));
        setSquare(row, 7, Value.ROOK.getSymbol(color));

    }

    protected int getSquareIndex(int row, int column){
        column %= 8;
        if (this.color == Color.NONE) row %= 8;
        return 9*row + column;
    }

}
