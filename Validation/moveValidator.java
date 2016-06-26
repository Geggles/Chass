package Validation;

import Game.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Has to validate in context. Therefore always needs a Game.controller object. To not pass around
 * the controller object everywhere, this is a singleton.
 */
public class MoveValidator {
    private static MoveValidator instance = new MoveValidator();
    private static Controller controller;
    private MoveValidator() {

    }
    public static MoveValidator getInstance(){
        return instance;
    }

    public void setController(Controller controller_){
        controller = controller_;
    }

    public static boolean validMove(Move move){
        if (move == null || MoveType.of(move) == null){
            return false;
        }
        Color turnPlayer = controller.getCurrentPlayer();
        switch (MoveType.of(move)){
            case PROMOTION: return true;
        }
        return true;
    }

    private static boolean validCastle(){
        return true;
    }

    /**
     * Calculate all valid swaps for a square.
     * <p>
     *      This method is necessary, because callers of validMove may not know what swaps are
     *      possible, because they only have access to a single board. The MoveValidator object
     *      however has access to all boards through the controller field. Therefore this is a
     *      better way than to construct all combinations of letters and try every swap with the
     *      validMove function.
     * </p>
     * @param sourceSquare The square the piece is on that should conduct the swap.
     * @return A Move Array containing valid swaps.
     */
    public static Move[] validSwaps(Square sourceSquare){
        if (sourceSquare == null) return new Move[0];
        Board sourceBoard = sourceSquare.board;
        if (sourceBoard == null) return new Move[0];
        Piece sourcePiece = sourceBoard.getPiece(sourceSquare);
        if (sourcePiece == null) return new Move[0];
        String squareName = sourceBoard.getSquareName(sourceSquare);
        ArrayList<Character> boardNames = new ArrayList<>(3);
        ArrayList<Move> validSwaps = new ArrayList<>(8);
        if (controller.getBoard('A').getPiece(squareName) != null) boardNames.add('A');
        if (controller.getBoard('B').getPiece(squareName) != null) boardNames.add('B');
        if (controller.getBoard('C').getPiece(squareName) != null) boardNames.add('C');
        if (boardNames.size() == 1) return new Move[0];
        boolean firstOne = true;  // so that original composition doesn't get counted as a swap
        for (Character boardName0 :
                boardNames) {
            for (Character boardName1 :
                    boardNames) {
                if (boardName1 == boardName0) continue;
                if (boardNames.size() == 3){
                    for (Character boardName2 :
                            boardNames) {
                        if (firstOne){
                            firstOne = false;
                            continue;
                        }
                        if (boardName2 == boardName0) continue;
                        if (boardName2 == boardName1) continue;
                        Board board0 = controller.getBoard(boardName0);
                        Board board1 = controller.getBoard(boardName1);
                        Board board2 = controller.getBoard(boardName2);
                        validSwaps.add(new Move(
                                null, null,
                                new Character[]{board0.getPiece(squareName).value.name,
                                                board1.getPiece(squareName).value.name,
                                                board2.getPiece(squareName).value.name},
                                new Character[]{boardNames.get(0), boardNames.get(1),
                                                boardNames.get(2),
                                                boardName0, boardName1, boardName2},
                                new String[]{squareName}
                        ));
                    }
                } else {
                    if (firstOne){
                        firstOne = false;
                        continue;
                    }
                    if (boardName0 != sourceBoard.name &&
                        boardName1 != sourceBoard.name) continue;
                    Board board0 = controller.getBoard(boardName0);
                    Board board1 = controller.getBoard(boardName1);
                    validSwaps.add(new Move(
                            null, null,
                            new Character[]{board0.getPiece(squareName).value.name,
                                            board1.getPiece(squareName).value.name},
                            new Character[]{boardNames.get(0), boardNames.get(1),
                                            boardName0, boardName1},
                            new String[]{squareName}
                    ));
                }
            }
        }
        return validSwaps.toArray(new Move[validSwaps.size()]);
    }
}