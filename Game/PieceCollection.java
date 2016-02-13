package Game;

import java.util.ArrayList;
import java.util.Collections;

public class PieceCollection {
    private ArrayList<Value> pieces = new ArrayList<>(16);
    public PieceCollection(){

    }

    public ArrayList<Value> getPieces(){
        return pieces;
    }

    public int countPieces(Value value){
        return Collections.frequency(pieces, value);
    }

    public boolean containsPiece(Value value){
        return pieces.contains(value);
    }

    public boolean removePiece(Value value){
        boolean success = getPieces().remove(value);
        sortPieces();
        return success;
    }

    public void addPiece(Value value){
        getPieces().add(value);
        sortPieces();
    }

    private void sortPieces(){
        Collections.sort(getPieces());
    }

}
