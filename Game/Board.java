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

    protected void addSquareAt(int[] coordinates, Square square){
        squares.inverse().put(coordinates, square);
    }

    protected void addCoordinatesOf(int[] coordinates, Square square){
        squares.put(square, coordinates);
    }



    public Square getSquare(Piece piece){
        return state.inverse().get(piece);
    }

    public Square getSquare(int[] coordinates){
        return squares.inverse().get(coordinates);
    }

    public Square getSquare(int row, int column){
        return getSquare(new int[] {row, column});
    }


    public Piece getPiece(Square square){
        return state.get(square);
    }

    public Piece getPiece(int[] coordinates){
        return getPiece(getSquare(coordinates));
    }

    public Piece getPiece(int row, int column){
        return getPiece(getSquare(row, column));
    }


    public int[] getCoordinates(Square square){
        return squares.get(square);
    }

    public int[] getCoordinates(Piece piece){
        return getCoordinates(state.inverse().get(piece));
    }

    public int[] getCoordinates(int[] coordinates){
        return getCoordinates(getSquare(coordinates));
    }

    public int[] getCoordinates(int row, int column){
        return getCoordinates(getSquare(row, column));
    }


    public void setPiece(Square square, Piece piece){
        state.put(square, piece);
    }

    public void setPiece(int[] coordinates, Piece piece){
        setPiece(getSquare(coordinates), piece);
    }

    public void setPiece(int row, int column, Piece piece){
        setPiece(getSquare(row, column), piece);
    }


    public void removePiece(Square square){
        state.remove(square);
    }

    public void removePiece(Piece piece){
        removePiece(getSquare(piece));
    }

    public void removePiece(int[] coordinates){
        removePiece(getSquare(coordinates));
    }

    public void removePiece(int row, int column){
        removePiece(getSquare(row, column));
    }


    public Piece popPiece(Square square){
        Piece popped = state.get(square);
        removePiece(square);
        return popped;
    }

    public Piece popPiece(Piece piece){
        return popPiece(getSquare(piece));
    }

    public Piece popPiece(int[] coordinates){
        return popPiece(getSquare(coordinates));
    }

    public Piece popPiece(int row, int column){
        return popPiece(getSquare(row, column));
    }


    public Piece replacePiece(Square replace, Piece with){
        Piece popped = popPiece(replace);
        setPiece(replace, with);
        return popped;
    }

    public Piece replacePiece(Piece replace, Piece with){
        return replacePiece(getSquare(replace), with);
    }

    public Piece replacePiece(int[] coordinates, Piece with){
        return replacePiece(getSquare(coordinates), with);
    }

    public Piece replacePiece(int row, int column, Piece with){
        return replacePiece(getSquare(row, column), with);
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
