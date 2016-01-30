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
                square.color = currentColor;
                addSquareAt(coordinate, square);

                if (currentColor == Colors.WHITE) {
                    currentColor = Colors.BLACK;
                } else {
                    currentColor = Colors.WHITE;
                }
            }
        }
    }

    /**
     * Search all directions for pieces of color 'color' that could attack this square
     * */
    public boolean isUnderAttack(Square square, Colors color){
        Board board = square.board;
        int[] coordinates = board.getCoordinateOf(square);
        int row = coordinates[0];
        int column = coordinates[1];
        int searchRow;
        int searchColumn;
        int result;

        //Pawn
        int direction = +1;
        if (color == Colors.BLACK) direction = -1;
        result = testSquare(row+direction, column-1, board, color, PieceValue.PAWN);
        if (result == 1) return true;
        result = testSquare(row+direction, column+1, board, color, PieceValue.PAWN);
        if (result == 1) return true;

        //Bishop
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && ++searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && --searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && ++searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && --searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Rook
        searchRow = row;
        searchColumn = column;
        while (++searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column){
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column){
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, PieceValue.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Knight
        if (testSquare(row+1, column+2, board, color, PieceValue.KNIGHT)==1) return true;
        if (testSquare(row+1, column-2, board, color, PieceValue.KNIGHT)==1) return true;
        if (testSquare(row+2, column+1, board, color, PieceValue.KNIGHT)==1) return true;
        if (testSquare(row+2, column-1, board, color, PieceValue.KNIGHT)==1) return true;
        if (testSquare(row-1, column+2, board, color, PieceValue.KNIGHT)==1) return true;
        if (testSquare(row-1, column-2, board, color, PieceValue.KNIGHT)==1) return true;
        if (testSquare(row-2, column+1, board, color, PieceValue.KNIGHT)==1) return true;
        if (testSquare(row-2, column-1, board, color, PieceValue.KNIGHT)==1) return true;

        return false;
    }

    /**
     * Check if square matches arguments
     * return 1 if so
     * return 0 if not
     * return -1 if the is no such square
     * return 2 if there is no piece on the square*/
    private int testSquare(int row, int column, Board board, Colors color, PieceValue value){
        Square square = board.getSquareAt(new int[] {row, column});
        if (square == null) return -1;
        Piece piece = board.getPieceOn(square);
        if (piece == null) return 2;
        if (piece.color != color) return 0;
        if (piece.value != value) return 0;
        return 1;
    }
}
