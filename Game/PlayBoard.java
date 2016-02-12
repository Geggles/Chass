package Game;
import com.google.common.collect.HashBiMap;

/**
* Used for Boards Alpha, Beta and Gamma
* */
public abstract class PlayBoard extends Board{
    public final String name;
    private static final HashBiMap<int[], String> squareNames = HashBiMap.create(64);
    static
    {
        String names = "abcdefgh";
        for (int row=0; row<8; row++){
            for (int column=0; column<8; column++){
                squareNames.put(
                        new int[] {row, column},
                        names.charAt(column) + Integer.toString(row+1));
            }
        }
    }

    public PlayBoard(String name, Color color){
        super(color);
        this.name = name;
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
                currentColor = currentColor.opposite();
            }
        }
    }

    /**
     * Search all directions for pieces of color 'color' that could attack this square
     * */
    public boolean isUnderAttack(Square square, Color color){
        Board board = square.board;
        int[] coordinates = board.getCoordinates(square);
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
        Square square = board.getSquare(new int[] {row, column});
        if (square == null) return -1;
        Piece piece = board.getPiece(square);
        if (piece == null) return 2;
        if (piece.color != color) return 0;
        if (piece.value != value) return 0;
        return 1;
    }

    public String getSquareName(Square square){
        return getSquareName(getCoordinates(square));
    }

    public static String getSquareName(int[] coordinates){
        return squareNames.get(coordinates);
    }

    public static String getSquareName(int row, int column){
        return squareNames.get(new int[] {row, column});
    }

    public Piece getPiece(String squareName){
        return getPiece(getSquare(squareName));
    }

    public Square getSquare(String squareName){
        return getSquare(getCoordinates(squareName));
    }

    public static int[] getCoordinates(String squareName){
        return squareNames.inverse().get(squareName);
    }
}
