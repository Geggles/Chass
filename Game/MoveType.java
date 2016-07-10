package Game;

public enum MoveType {
    CASTLE,
    DROP,
    HOSTAGE_EXCHANGE,
    SWAP2,
    SWAP3,
    CAPTURE,
    STEAL,
    EN_PASSANT,
    TRANSLATE;

    public static MoveType of(Move move) {
        if (move == null) return null;
        if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 0){  // castle:
            return CASTLE;
        }
        if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 1){  // drop
            return DROP;
        }
        if (move.pieceNames.length >= 2 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 1) {  // hostage exchange
            return HOSTAGE_EXCHANGE;
        }
        if (move.pieceNames.length == 2 &&
                move.boardNames.length == 4 &&
                move.squareNames.length == 1) {  // swap2
            return SWAP2;
        }
        if (move.pieceNames.length == 3 &&
                move.boardNames.length == 6 &&
                move.squareNames.length == 1) {  // swap3
            return SWAP3;
        }
        if (move.pieceNames.length == 2 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // capture
            return CAPTURE;
        }
        if (move.pieceNames.length == 2 &&
                move.boardNames.length == 3 &&
                move.squareNames.length == 2) {  // steal
            return STEAL;
        }
        if (move.pieceNames.length == 0 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // en passant
            return EN_PASSANT;
        }
        if (move.pieceNames.length == 1 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // translate
            return TRANSLATE;
        }
        return null;
    }
}
