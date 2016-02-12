package Game;

import java.util.LinkedList;
import java.util.StringJoiner;

public class Notator {
    private LinkedList<String> moves;
    private int turnCounter = 0;
    /**
     * Notates moves as a list of strings. It is a verbose reversible algebraic notation.
     * It is supposed to also be human readable -> see automaton.
     * ex.:
     * Knight moves from a7 on alpha to c6 and teleports to beta:
     * Na7-c6_A>B
     * Queen moves from b5 on gamma to b8 on gamma, captures a pawn
     * and teleports it self to beta and the pawn to alpha
     * thereby promoting it to another Queen, checking to opposing king:
     * Qb5xPb8=Q_C>B>A+
     * Kingside castling:
     * O-O
     * En Passant (the p indicates the special move):
     * Pd6pe6_A>A
     * Hostage Exchange of giving a Bishop for Knight, dropping the Knight on g4:
     * B>Ng4
     * Queen from airfield is dropped to f6:
     * >Qf6
     * Bishop on c7 on Alpha swaps positions with another Bishop on Beta and a Rook on Gamma,
     * so that first bishop is now on Gamma, second one is on Beta and the Rook on Alpha,
     * thereby checkmating opponent:
     * Bc7-B-R_ABC>CBA#
     * */
    public Notator() {
        moves = new LinkedList<>();
    }

    private void addMove(String move){
        removeMovesAfter(turnCounter++);
        moves.add(move);
    }

    private void removeMovesAfter(int n){
        moves.subList(moves.size() - n, moves.size()).clear();
    }

    public void notateMove(){
        addMove(encodeMove(move));
    }

    public boolean goBack(){
        if (turnCounter < 2) return false;
        turnCounter --;
        return true;
    }

    public boolean goForward(){
        if (turnCounter+1 > moves.size()) return false;
        if (turnCounter < 0) return false;
        turnCounter ++;
        return true;
    }

    public String getMove(){
        return moves.get(turnCounter);
    }

    public int currentTurn(){
        return turnCounter;
    }

    public String encodeMove(Move move){
        /*
        * Check in order:
        * Castling
        * Drop
        * Hostage Exchange
        * Swap
        * Capture
        * Steal
        * En Passant
        * Translate
        * */
        if (move instanceof Castling){
            if(((Castling) move).side == Value.KING) return "O-O";
            return "O-O-O";
        }
        if (move instanceof Drop){
            Drop drop = (Drop) move;
            drop.coordinates
        }
    }

    public LinkedList<String> getMoves(){
        return moves;
    }
}
