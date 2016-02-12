package Game;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * this class will control the flow of the game
 * */

public class Controller {
    private StandardBoard alpha;
    private StandardBoard beta;
    private SpecialBoard gamma;
    private HashMap<Color, Prison> prisons;
    private HashMap<Color, Airfield> airfields;
    private Color turnPlayer = Color.WHITE;
    private LinkedList<String> moves;
    private int currentPly = 0;
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
    public Controller() {
        alpha = new StandardBoard(Color.WHITE);
        beta = new StandardBoard(Color.BLACK);
        gamma = new SpecialBoard();
        prisons = new HashMap<>(2);
        prisons.put(Color.WHITE, new Prison(Color.WHITE));
        prisons.put(Color.BLACK, new Prison(Color.BLACK));
        airfields = new HashMap<>(2);
        airfields.put(Color.WHITE, new Airfield(Color.WHITE));
        airfields.put(Color.BLACK, new Airfield(Color.BLACK));
    }

    public void newGame(){
    }

    public PlayBoard getBoard(Character name){
        switch (name){
            case 'A': return  alpha;
            case 'B': return  beta;
            case 'C': return  gamma;
        }
        return null;
    }

    private void removeMovesAfter(int turn){
        moves.subList(moves.size() - turn, moves.size()).clear();
    }

    private void addMove(Move move){
        String moveString = encodeMove(move);
        removeMovesAfter(currentPly);
        moves.add(moveString);
        incrementPly();
    }

    private boolean incrementPly(){
        if (currentPly < moves.size()-1){
            currentPly ++;
            return true;
        }
        return false;
    }

    private boolean decrementPly(){
        if (currentPly > 0){
            currentPly --;
            return true;
        }
        return false;
    }

    public int getCurrentPly(){
        return currentPly;
    }

    public LinkedList<String> getMoves(){
        return moves;
    }

    private String encodeMove(Move move){
        String moveString = "INVALID";
        String promotionString = "";
        if (move.promotion!=null) promotionString = "=" + promotionString;
        String stateString = "";
        if (move.promotion!=null) stateString = move.state.toString();

        if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 0){  // castle:
            moveString = "O-O";
            if (move.pieceNames[0]=='Q') moveString = "O-O-O";
        }else if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 1){  // drop
            moveString = String.format(">%s%s",
                    move.pieceNames[0],
                    move.squareNames[0]);
        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 1) {  // hostage exchange
            moveString = String.format("%s>%s%s",
                    move.pieceNames[0],
                    move.pieceNames[1],
                    move.squareNames[0]);
        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 4 &&
                move.squareNames.length == 1) {  // swap2
            moveString = String.format("%s%s-%s%s_%s%s>%s%s%s",
                    move.pieceNames[0],
                    move.squareNames[0],
                    move.pieceNames[1],
                    promotionString,
                    move.boardNames[0],
                    move.boardNames[1],
                    move.boardNames[2],
                    move.boardNames[3],
                    stateString);
        }else if (move.pieceNames.length == 3 &&
                move.boardNames.length == 6 &&
                move.squareNames.length == 1) {  // swap3
            moveString = String.format("%s%s-%s-%s%s_%s%s%s>%s%s%s%s",
                    move.pieceNames[0],
                    move.squareNames[0],
                    move.pieceNames[1],
                    move.pieceNames[2],
                    promotionString,
                    move.boardNames[0],
                    move.boardNames[1],
                    move.boardNames[2],
                    move.boardNames[3],
                    move.boardNames[4],
                    move.boardNames[5],
                    stateString);
        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // capture
            moveString = String.format("%s%sx%s%s%s_%s>%s%s",
                    move.pieceNames[0],
                    move.squareNames[0],
                    move.pieceNames[1],
                    move.squareNames[1],
                    promotionString,
                    move.boardNames[0],
                    move.boardNames[1],
                    stateString);
        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 3 &&
                move.squareNames.length == 2) {  // steal
            moveString = String.format("%s%sx%s%s%s_%s>%s>%s%s",
                    move.pieceNames[0],
                    move.squareNames[0],
                    move.pieceNames[1],
                    move.squareNames[1],
                    promotionString,
                    move.boardNames[0],
                    move.boardNames[1],
                    move.boardNames[2],
                    stateString);
        }else if (move.pieceNames.length == 0 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // en passant
            moveString = String.format("P%sp%s_%s>%s%s",
                    move.squareNames[0],
                    move.squareNames[1],
                    move.boardNames[0],
                    move.boardNames[1],
                    stateString);
        }else if (move.pieceNames.length == 1 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // translate
            moveString = String.format("%s%s-%s>%s>%s%s",
                    move.pieceNames[0],
                    move.squareNames[0],
                    move.squareNames[1],
                    move.boardNames[0],
                    move.boardNames[1],
                    stateString);
        }
        return moveString;
    }

    private Move decodeMove(String moveString){
        Move move;
        Character state = null;
        Character promotion = null;
        Character[] pieceNames = null;
        Character[] boardNames = null;
        String[] squareNames = null;

        switch (moveString.charAt(moveString.length() - 1)){  // last char
            case '+':
            case '#':
            case '@':
                state = moveString.charAt(moveString.length() - 1);
                moveString = moveString.substring(0, moveString.length() -1);
        }

        if (moveString.startsWith("O-O")){  // castle
            pieceNames = new Character[1];
            boardNames = new Character[0];
            squareNames = new String[0];
            if (moveString.equals("O-O-O")) pieceNames[0] = 'Q';
            else pieceNames[0] = 'K';
        }else if (moveString.charAt(0) == '>'){  // drop
            pieceNames = new Character[1];
            boardNames = new Character[0];
            squareNames = new String[1];
            pieceNames[0] = moveString.charAt(1);
        }else {

            int startOfConstellation = moveString.indexOf('_');
            if (moveString.charAt(startOfConstellation-2) == '='){
                promotion = moveString.charAt(startOfConstellation-1);
            }

            if (moveString.charAt(3) == '-'){  // swap/translation
                if ("12345678".charAt(moveString.charAt(5))==-1) { // swap
                    squareNames = new String[1];
                    if (moveString.charAt(5) == '_') {  // swap2
                        pieceNames = new Character[2];
                        boardNames = new Character[4];
                        pieceNames[1] = moveString.charAt(4);
                        boardNames[2] = moveString.charAt(startOfConstellation + 4);
                        boardNames[3] = moveString.charAt(startOfConstellation + 5);
                    } else {  // swap3
                        pieceNames = new Character[3];
                        boardNames = new Character[6];
                        pieceNames[1] = moveString.charAt(4);
                        pieceNames[2] = moveString.charAt(6);
                        boardNames[2] = moveString.charAt(startOfConstellation + 3);
                        boardNames[3] = moveString.charAt(startOfConstellation + 5);
                        boardNames[4] = moveString.charAt(startOfConstellation + 6);
                        boardNames[5] = moveString.charAt(startOfConstellation + 7);
                    }
                    boardNames[1] = moveString.charAt(startOfConstellation + 2);
                } else{  // translation
                    squareNames = new String[2];
                    boardNames = new Character[2];
                    pieceNames = new Character[1];
                    squareNames[1] = moveString.substring(4, 6);
                    boardNames[1] = moveString.charAt(startOfConstellation + 3);
                }
                pieceNames[0] = moveString.charAt(0);
                squareNames[0] = moveString.substring(1, 3);
            } else if (moveString.charAt(3)=='x'){  // capture/steal
                squareNames = new String[2];
                squareNames[1] = moveString.substring(5, 7);
                pieceNames = new Character[2];
                pieceNames[0] = moveString.charAt(1);
                pieceNames[1] = moveString.charAt(4);
                if (moveString.charAt(startOfConstellation)=='C') {  // steal
                    boardNames = new Character[3];
                    boardNames[1] = moveString.charAt(startOfConstellation + 3);
                    boardNames[2] = moveString.charAt(startOfConstellation + 5);
                }else{  // capture/en passant
                    boardNames = new Character[2];
                    boardNames[1] = moveString.charAt(startOfConstellation + 3);
                    if ("PBNQR".charAt(moveString.charAt(4))==-1){  // en passant
                        pieceNames = new Character[0];
                        squareNames[1] = moveString.substring(4, 6);
                    }
                }
            }
            boardNames[0] = moveString.charAt(startOfConstellation + 1);
            squareNames[0] = moveString.substring(2, 4);
        }
        move = new Move(
                state,
                promotion,
                pieceNames,
                boardNames,
                squareNames
        );
        return move;
    }
}
