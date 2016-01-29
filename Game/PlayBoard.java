package Game;

/**
* Used for Boards Alpha, Beta and Gamma
* */
public abstract class PlayBoard extends Board{
    abstract void move(Square source, Square destination);
    abstract boolean validMove(Square source, Square destination);
    abstract void capture(Square source, Square destination);

    public PlayBoard(){
        squares = new Square[8][8];
        Colors currentColor = Colors.WHITE;
        for (int row=0; row<8; row++){
            for (int column=0; column<8; column++) {
                squares[row][column].setColor(currentColor);
                if (currentColor == Colors.WHITE) {
                    currentColor = Colors.BLACK;
                } else {
                    currentColor = Colors.WHITE;
                }
            }
        }
    }
}
