package Game;

import com.google.common.collect.HashBiMap;

import java.util.ArrayList;

public abstract class Board {
    private HashBiMap<Square, Piece> state = HashBiMap.create(32);
    private HashBiMap<Square, int[]> squares;
    public Color color;
    protected abstract void setupSquares();

    protected void initializeSquares(int size){
        squares = HashBiMap.create(size);
    }

    public Piece getPieceOn(Square square){
        return state.get(square);
    }

    public Piece getPieceOn(int[] coordinates){
        return getPieceOn(getSquareAt(coordinates));
    }

    public Square getSquareOf(Piece piece){
        return state.inverse().get(piece);
    }

    protected void addSquareAt(int[] coordinates, Square square){
        squares.inverse().put(coordinates, square);
    }

    public Square getSquareAt(int[] coordinates){
        return squares.inverse().get(coordinates);
    }

    protected void addCoordinateOf(int[] coordinates, Square square){
        squares.put(square, coordinates);
    }

    public int[] getCoordinateOf(Square square){
        return squares.get(square);
    }

    public int[] getCoordinateOf(Piece piece){
        return getCoordinateOf(state.inverse().get(piece));
    }

    public void setPieceAt(Piece piece, Square square){
        state.put(square, piece);
    }

    public void setPieceAt(Piece piece, int[] coordinates){
        setPieceAt(piece, getSquareAt(coordinates));
    }

    public void removePiece(Piece piece){
        removePieceFrom(getSquareOf(piece));
    }

    public void removePieceFrom(Square square){
        state.put(square, null);
    }

    public void removePieceFrom(int[] coordinates){
        removePieceFrom(getSquareAt(coordinates));
    }

    public ArrayList<Piece> getAllPieces() {
        ArrayList<Piece> res = new ArrayList<>();
        state.forEach((square, piece) -> res.add(piece));
        return res;
    }
}
