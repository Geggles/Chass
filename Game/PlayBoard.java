package Game;

import com.google.common.collect.HashBiMap;

/**
* Used for Boards Alpha, Beta and Gamma
* */
public abstract class PlayBoard extends Board{
    private static final HashBiMap<int[], String> squareNames = HashBiMap.create(64);
    static
    {
        squareNames.put(new int[] {0, 0}, "a1");
        squareNames.put(new int[] {0, 1}, "b1");
        squareNames.put(new int[] {0, 2}, "c1");
        squareNames.put(new int[] {0, 3}, "d1");
        squareNames.put(new int[] {0, 4}, "e1");
        squareNames.put(new int[] {0, 5}, "f1");
        squareNames.put(new int[] {0, 6}, "g1");
        squareNames.put(new int[] {0, 7}, "h1");
        squareNames.put(new int[] {1, 0}, "a2");
        squareNames.put(new int[] {1, 1}, "b2");
        squareNames.put(new int[] {1, 2}, "c2");
        squareNames.put(new int[] {1, 3}, "d2");
        squareNames.put(new int[] {1, 4}, "e2");
        squareNames.put(new int[] {1, 5}, "f2");
        squareNames.put(new int[] {1, 6}, "g2");
        squareNames.put(new int[] {1, 7}, "h2");
        squareNames.put(new int[] {2, 0}, "a3");
        squareNames.put(new int[] {2, 1}, "b3");
        squareNames.put(new int[] {2, 2}, "c3");
        squareNames.put(new int[] {2, 3}, "d3");
        squareNames.put(new int[] {2, 4}, "e3");
        squareNames.put(new int[] {2, 5}, "f3");
        squareNames.put(new int[] {2, 6}, "g3");
        squareNames.put(new int[] {2, 7}, "h3");
        squareNames.put(new int[] {3, 0}, "a4");
        squareNames.put(new int[] {3, 1}, "b4");
        squareNames.put(new int[] {3, 2}, "c4");
        squareNames.put(new int[] {3, 3}, "d4");
        squareNames.put(new int[] {3, 4}, "e4");
        squareNames.put(new int[] {3, 5}, "f4");
        squareNames.put(new int[] {3, 6}, "g4");
        squareNames.put(new int[] {3, 7}, "h4");
        squareNames.put(new int[] {4, 0}, "a5");
        squareNames.put(new int[] {4, 1}, "b5");
        squareNames.put(new int[] {4, 2}, "c5");
        squareNames.put(new int[] {4, 3}, "d5");
        squareNames.put(new int[] {4, 4}, "e5");
        squareNames.put(new int[] {4, 5}, "f5");
        squareNames.put(new int[] {4, 6}, "g5");
        squareNames.put(new int[] {4, 7}, "h5");
        squareNames.put(new int[] {5, 0}, "a6");
        squareNames.put(new int[] {5, 1}, "b6");
        squareNames.put(new int[] {5, 2}, "c6");
        squareNames.put(new int[] {5, 3}, "d6");
        squareNames.put(new int[] {5, 4}, "e6");
        squareNames.put(new int[] {5, 5}, "f6");
        squareNames.put(new int[] {5, 6}, "g6");
        squareNames.put(new int[] {5, 7}, "h6");
        squareNames.put(new int[] {6, 0}, "a7");
        squareNames.put(new int[] {6, 1}, "b7");
        squareNames.put(new int[] {6, 2}, "c7");
        squareNames.put(new int[] {6, 3}, "d7");
        squareNames.put(new int[] {6, 4}, "e7");
        squareNames.put(new int[] {6, 5}, "f7");
        squareNames.put(new int[] {6, 6}, "g7");
        squareNames.put(new int[] {6, 7}, "h7");
        squareNames.put(new int[] {7, 0}, "a8");
        squareNames.put(new int[] {7, 1}, "b8");
        squareNames.put(new int[] {7, 2}, "c8");
        squareNames.put(new int[] {7, 3}, "d8");
        squareNames.put(new int[] {7, 4}, "e8");
        squareNames.put(new int[] {7, 5}, "f8");
        squareNames.put(new int[] {7, 6}, "g8");
        squareNames.put(new int[] {7, 7}, "h8");
    }

    public PlayBoard(String name, Color color){
        super(name, color);
        initializeSquares(64);
        setupSquares();
    }

    protected void setupSquares(){
        Square square;
        int[] coordinate;
        Color currentColor = Color.BLACK;
        for (int row=0; row<8; row++) {
            for (int column = 0; column < 8; column++) {
                square = new Square(currentColor, this);
                coordinate = new int[] {row, column};
                addSquareAt(coordinate, square);
                currentColor = Color.oppositeColor(currentColor);
            }
        }
    }

    /**
     * Search all directions for pieces of color 'color' that could attack this square
     * */
    public boolean isUnderAttack(Square square, Color color){
        Board board = square.board;
        int[] coordinates = board.getCoordinatesOf(square);
        int row = coordinates[0];
        int column = coordinates[1];
        int searchRow;
        int searchColumn;
        int result;

        //Pawn
        int direction = +1;
        if (color == Color.BLACK) direction = -1;
        result = testSquare(row+direction, column-1, board, color, Value.PAWN);
        if (result == 1) return true;
        result = testSquare(row+direction, column+1, board, color, Value.PAWN);
        if (result == 1) return true;

        //Bishop
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && ++searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && --searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && ++searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && --searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Rook
        searchRow = row;
        searchColumn = column;
        while (++searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchRow != row){
            result = testSquare(searchRow, searchColumn, board, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column){
            result = testSquare(searchRow, searchColumn, board, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column){
            result = testSquare(searchRow, searchColumn, board, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Knight
        if (testSquare(row+1, column+2, board, color, Value.KNIGHT)==1) return true;
        if (testSquare(row+1, column-2, board, color, Value.KNIGHT)==1) return true;
        if (testSquare(row+2, column+1, board, color, Value.KNIGHT)==1) return true;
        if (testSquare(row+2, column-1, board, color, Value.KNIGHT)==1) return true;
        if (testSquare(row-1, column+2, board, color, Value.KNIGHT)==1) return true;
        if (testSquare(row-1, column-2, board, color, Value.KNIGHT)==1) return true;
        if (testSquare(row-2, column+1, board, color, Value.KNIGHT)==1) return true;
        if (testSquare(row-2, column-1, board, color, Value.KNIGHT)==1) return true;

        return false;
    }

    /**
     * Check if square matches arguments
     * return 1 if so
     * return 0 if not
     * return -1 if the is no such square
     * return 2 if there is no piece on the square*/
    private int testSquare(int row, int column, Board board, Color color, Value value){
        Square square = board.getSquareAt(new int[] {row, column});
        if (square == null) return -1;
        Piece piece = board.getPieceOn(square);
        if (piece == null) return 2;
        if (piece.color != color) return 0;
        if (piece.value != value) return 0;
        return 1;
    }

    /**
     * (0,0) -> "a1"
     * */
    public String getSquareName(int[] coordinates){
        return squareNames.get(coordinates);
    }

    public String getSquareName(Square square){
        return getSquareName(getCoordinatesOf(square));
    }

    public Square getSquareAt(String squareName){
        return getSquareAt(getCoordinatesOf(squareName));
    }

    public int[] getCoordinatesOf(String squareName){
        return squareNames.inverse().get(squareName);
    }
}
