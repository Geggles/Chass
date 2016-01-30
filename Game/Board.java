package Game;

import com.google.common.collect.HashBiMap;

public abstract class Board {
    private HashBiMap<Square, Piece> state = HashBiMap.create(32);
    private HashBiMap<Square, int[]> squares;
    public abstract int[] getCoordinate(Square square);

    public void setSquareAt(int[] coordinates, Square square){
        squares.inverse().put(coordinates, square);
    }

    public Square getSquareAt(int[] coordinates){
        return squares.inverse().get(coordinates);
    }

    public void setCoordinateOf(int[] coordinates, Square square){
        squares.put(square, coordinates);
    }

    public int[] getCoordinateOf(Square square){
        return squares.get(square);
    }
}
