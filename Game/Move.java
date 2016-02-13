package Game;
/**
 * Specification for move types:
 *   <All>:
 *      state: Either null if move did not lead to a special game state or
 *               '+' if move lead to a check or
 *               '#' if move lead to a checkmate or
 *               '@' if move lead to a stalemate
 *      promotion: either null if not promotion happened or
 *                   name of piece that has been promoted to
 *   Castle:
 *      pieceNames: Either 'Q' for castle queenside or 'K' for kingside
 *      boardNames: Empty
 *      squareNames: Empty
 *   Drop:
 *      pieceNames The dropped piece
 *      boardNames: Empty
 *      squareNames: square the piece was dropped to
 *   Hostage Exchange:
 *      pieceNames: The piece that was given to the opponent
 *                  The piece that was dropped
 *      boardNames: Empty
 *      squareNames: Square the piece was dropped to
 *   Swap:
 *      pieceNames: First piece to be swapped
 *                  Second piece to be swapped
 *                  [Third piece to be swapped]
 *      boardNames: Board first piece was on before swap
 *                  Board second piece was on before swap
 *                  [Board third piece was on before swap]
 *                  Board first piece is on after swap
 *                  Board second piece is on after swap
 *                  [Board third piece is on after swap]
 *      squareNames: Square the swap happens on
 *   Capture:
 *      pieceNames: Piece that has moved
 *                  Piece that was captured
 *      boardNames: Board the capture happened on
 *                  Board the piece is on after capture
 *      squareNames: Square the piece started out on
 *                   Square the capture happened on
 *   Steal:
 *      pieceNames: Piece that has moved
 *                  Piece that was captured
 *      boardNames: C  // to distinguish it from a normal capture
 *                  Board the own piece is on after the capture
 *                  Board the captured piece is on after the capture
 *      squareNames: Square the piece started out on
 *                   Square the capture happened on
 *   En Passant:
 *      pieceNames: Empty
 *      boardNames: Board the capture happened on
 *                  Board the Piece is on after the capture
 *      squareNames: Square the capturing pawn started out on
 *                   Square the capturing pawn is on after the capture
 *   Translate:
 *      pieceNames: Piece that has moved
 *      boardNames: Board the translation happened on
 *                  Board the piece is on after translation
 *      squareNames: Square the piece started out on
 *                   Square the piece ended up on
 */
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
