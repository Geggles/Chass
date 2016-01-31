package Game;

import com.google.common.collect.HashBiMap;

import java.util.ArrayList;

public abstract class Board {
    public final String name;
    private HashBiMap<Square, Piece> state = HashBiMap.create(32);
    private HashBiMap<Square, int[]> squares;
    public final Color color;

    public Board(String name, Color color){
        this.name = name;
        this.color = color;
    }

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

    protected void addCoordinatesOf(int[] coordinates, Square square){
        squares.put(square, coordinates);
    }

    public int[] getCoordinatesOf(Square square){
        return squares.get(square);
    }

    public int[] getCoordinatesOf(Piece piece){
        return getCoordinatesOf(state.inverse().get(piece));
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

    public ArrayList<Piece> getPieces(Value value){
        ArrayList<Piece> result = new ArrayList<>(2);
        getAllPieces().forEach(piece -> {if (piece.value == value) result.add(piece);});
        return result;
    }
}
