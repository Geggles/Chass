package Game;

import Shared.Value;

import java.util.ArrayList;
import java.util.Collections;

public class PieceCollection {
    private ArrayList<Value> pieces = new ArrayList<>(16);
    public PieceCollection(){

    }

    public Value[] getPieces(){
        return pieces.toArray(new Value[pieces.size()]);
    }

    public int countPieces(Value value){
        return Collections.frequency(pieces, value);
    }

    public boolean containsPiece(Value value){
        return pieces.contains(value);
    }

    public boolean removePiece(Value value){
        boolean success = pieces.remove(value);
        sortPieces();
        return success;
    }

    public void addPiece(Value value){
        pieces.add(value);
        sortPieces();
    }

    private void sortPieces(){
        Collections.sort(pieces);
    }

}
