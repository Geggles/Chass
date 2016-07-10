package Game;

import Shared.Color;
import javafx.util.Pair;

/**
 * Specification for move types:
 *   <All>:
 *      state: Either null if move did not lead to a special game state or
 *               '+' if move lead to a check or
 *               '#' if move lead to a checkmate or
 *               '@' if move lead to a stalemate
 *      promotion: either null if not promotion happened or
 *                   name of piece that has been promoted to
 *
 *   Castle:
 *      pieceNames: Either 'Q' for castle queen side or 'K' for king side
 *      boardNames: Empty
 *      squareNames: Empty
 *
 *   Drop:
 *      pieceNames The dropped piece
 *      boardNames: Empty
 *      squareNames: square the piece was dropped to
 *
 *   Hostage Exchange:
 *      pieceNames: The piece that was dropped
 *                  The pieces that were given to the opponent
 *
 *      boardNames: Empty
 *      squareNames: Square the piece was dropped to
 *
 *   Swap:
 *      pieceNames: First piece to be swapped
 *                  Second piece to be swapped
 *      boardNames: Board first piece is on after swap
 *                  Board second piece is on after swap
 *      squareNames: Square the swap happens on
 *
 *   Capture:
 *      pieceNames: Piece that has moved
 *                  Piece that was captured
 *      boardNames: Board the capture happened on
 *                  Board the piece is on after capture
 *      squareNames: Square the piece started out on
 *                   Square the capture happened on
 *   En passant:
 *      pieceNames: Empty
 *      boardNames: Board the capture happened on
 *                  Board the piece is on after capture
 *      squareNames: Square the piece started out on
 *                   Square the piece ended up on
 *
 *
 *   Steal:
 *      pieceNames: Piece that has moved
 *                  Piece that was captured
 *      boardNames: C  // to distinguish it from a normal capture
 *                  Board the own piece is on after the capture
 *                  Board the captured piece is on after the capture
 *      squareNames: Square the piece started out on
 *                   Square the capture happened on
 *
 *   Translate:
 *      pieceNames: Piece that has moved
 *      boardNames: Board the translation happened on
 *                  Board the piece is on after translation
 *      squareNames: Square the piece started out on
 *                   Square the piece ended up on
 *   Promotion:
 *      special type of translation where the promotion field is set to not null
 *
 */
public class Move {
    final public Color player;
    final public Character state;
    final public Character promotion;
    final public Character[] pieceNames;
    final public Character[] boardNames;
    final public String[] squareNames;
    public Move(Color player,
                Character state,
                Character promotion,
                Character[] pieceNames,
                Character[] boardNames,
                String[] squareNames){
        this.player = player;
        this.state = state;
        this.promotion = promotion;
        this.pieceNames = pieceNames;
        this.boardNames = boardNames;
        this.squareNames = squareNames;
    }
    public Pair<Character[], String> getSource(){
        MoveType moveType = MoveType.of(this);
        if (moveType == null) return null;
        switch (moveType) {
            case CASTLE:
                if (player==Color.WHITE) {
                    return new Pair<>(new Character[]{'A'}, "e1");
                }
                return new Pair<>(new Character[]{'B'}, "e8");
            case SWAP:
                return new Pair<>(
                        new Character[]{this.boardNames[0], this.boardNames[1]},
                        this.squareNames[0]);
            case CAPTURE:
            case EN_PASSANT:
            case TRANSLATE:
                return new Pair<>(new Character[]{this.boardNames[0]}, this.squareNames[0]);
            case STEAL:
                return new Pair<>(new Character[]{'C'}, this.squareNames[0]);
        }
        return null;
    }
    public Pair<Character[], String> getDestination(){
        MoveType moveType = MoveType.of(this);
        if (moveType == null) return null;
        switch (moveType) {
            case CASTLE:
                if (player==Color.WHITE) {
                    if(this.pieceNames[0] == 'K') return new Pair<>(new Character[]{'A'}, "g1");
                    return new Pair<>(new Character[]{'A'}, "c1");
                }
                // color is black
                if(this.pieceNames[0] == 'K') return new Pair<>(new Character[]{'B'}, "g8");
                return new Pair<>(new Character[]{'B'}, "c8");
            case SWAP:
                return new Pair<>(
                        new Character[]{this.boardNames[1], this.boardNames[0]},
                        this.squareNames[0]);
            case CAPTURE:
            case EN_PASSANT:
            case TRANSLATE:
                return new Pair<>(new Character[]{this.boardNames[1]}, this.squareNames[1]);
            case STEAL:
                return new Pair<>(new Character[]{'C'}, this.squareNames[1]);
            default:
                return null;
        }
    }
}
