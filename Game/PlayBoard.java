package Game;

/**
* Used for Boards Alpha, Beta and Gamma
* */
public abstract class PlayBoard extends Board{
    public PlayBoard(){
        initializeSquares(64);
        setupSquares();
    }

    protected void setupSquares(){
        Square square;
        int[] coordinate;
        Colors currentColor = Colors.BLACK;
        for (int row=0; row<8; row++) {
            for (int column = 0; column < 8; column++) {
                square = new Square();
                coordinate = new int[] {row, column};
                square.setColor(currentColor);
                addSquareAt(coordinate, square);

                if (currentColor == Colors.WHITE) {
                    currentColor = Colors.BLACK;
                } else {
                    currentColor = Colors.WHITE;
                }
            }
        }
    }
}
