package Game;

import Shared.Value;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

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

    public int size(){
        return pieces.size();
    }

    public Value getPieceAt(int index) {
        return pieces.get(index);
    }

/*    public Pair<Integer, Value>[] getPiecePairs() {
        ListIterator<Value> iterator = pieces.listIterator();
        ArrayList<Pair<Integer, Value>> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(new Pair<Integer, Value>(iterator.nextIndex(), iterator.next()));
        }
        return result.toArray(new Pair[result.size()]);
    }*/
}
