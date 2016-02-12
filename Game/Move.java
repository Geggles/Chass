package Game;

public class Move {
    final Character state;
    final Character promotion;
    final Character[] pieceNames;
    final Character[] boardNames;
    final String[] squareNames;
    Move(Character state,
         Character promotion,
         Character[] pieceNames,
         Character[] boardNames,
         String[] squareNames){
        this.state = state;
        this.promotion = promotion;
        this.pieceNames = pieceNames;
        this.boardNames = boardNames;
        this.squareNames = squareNames;
    }
}
