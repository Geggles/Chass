package Game;

import java.util.ArrayList;

public class Notator {
    private ArrayList<String> moves;
    /**
     * Notates moves as a list of strings. It is a verbose reversible algebraic notation.
     * It is not designed to be human readable but designed to be easily handled by this class.
     * ex.:
     * Knight moves from a7 on alpha to c6 and teleports to beta:
     * Na7-Nc6_A>B
     * Queen moves from b5 on gamma to b8 on gamma, captures a pawn
     * and teleports it self to beta and the pawn to alpha
     * thereby promoting it to another Queen, checking to opposing king:
     * Qb5xPb8=Q_C>B>A+
     * Kingside castling:
     * O-O
     * En Passant (the p indicates the special move):
     * Pd6pPe6_A>B
     * Hostage Exchange of giving a Bishop for Knight, dropping the Knight on g4:
     * B>Ng4
     * */
    public Notator() {
        moves = new ArrayList<>();
    }
    public String notateMove(Square sourceSquare, Piece sourcePiece,
                             Square destinationSquare){
        return "";
    }
}
