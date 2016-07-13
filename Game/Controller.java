package Game;

import Shared.Color;
import Shared.Value;
import javafx.util.Pair;

import java.util.*;

/**
 * this class will control the flow of the game
 * */

public class Controller {
    private Board alpha;
    private Board beta;
    private Board gamma;
    private EnumMap<Color, PieceCollection> prisons;
    private EnumMap<Color, PieceCollection> airfields;
    private Color turnPlayer = Color.WHITE;
    private LinkedList<Move> moveHistory = new LinkedList<>();
    private int currentPly = 0;

    public Controller() {
        resetGame();
    }

    public Color getCurrentPlayer(){
        return turnPlayer;
    }

    public Move getLatestMove(){
        return moveHistory.getLast();
    }

    public Move[] getMoves(Color player){
        LinkedList<Move> result = new LinkedList<>();
        for (int i=(player == Color.WHITE? 0: 1); i<moveHistory.size(); i+=2){
            result.add(moveHistory.get(i));
        }
        return result.toArray(new Move[result.size()]);
    }

    private Character getBoardName(Board board){
        if (board == alpha) return 'A';
        if (board == beta) return 'B';
        if (board == gamma) return 'C';
        return null;
    }

    public boolean inCheck(Color player){
        Board board = getBoard(player);
        return board.inCheck();
    }

    public boolean inCheck(){
        return inCheck(turnPlayer);
    }

    public Move[] getMoves(MoveType moveType){
        return moveHistory
                .stream()
                .filter(move -> MoveType.of(move) == moveType)
                .toArray(size -> new Move[size]);
    }

    public boolean hasCastled(Color player, Value side){
        return hasCastled(player, side.name);
    }

    public boolean hasCastled(Color player, char side){
        return moveHistory
                .stream()
                .anyMatch(move -> MoveType.of(move) == MoveType.CASTLE &&
                                  move.player == player &&
                                  move.pieceNames[0] == side);
    }

    //TOO GENERAL - REALLY NEEDED?
/*    public Move[] getMoves(int sourceRow, int sourceColumn,
                           int destinationRow, int destinationColumn,
                           Character[] pieceNames, Character[] boardNames){
        return getMoves(Board.getSquareName(sourceRow, sourceColumn),
                Board.getSquareName(destinationRow, destinationColumn),
                pieceNames, boardNames);
    }

    // doesn't work for swaps
    public Move[] getMoves(String sourceSquareName, String destinationSquareName,
                           Character[] pieceNames, Character[] boardNames){
        LinkedList<Move> result = new LinkedList<>();
        for (Move move: moveHistory){
            if (boardNames != null && !Arrays.equals(move.boardNames, boardNames)) continue;
            if (pieceNames != null && !Arrays.equals(move.pieceNames, pieceNames)) continue;

            Pair<Character[], String> source = move.getSource();
            Pair<Character[], String> destination = move.getDestination();

            if (sourceSquareName != null &&
                    !source.getValue().equals(sourceSquareName)) continue;
            if (destinationSquareName != null &&
                    !destination.getValue().equals(destinationSquareName)) continue;

                switch (MoveType.of(move)){
                    case CASTLE:
                    case SWAP2:

                    case SWAP3:

                    case CAPTURE:
                    case EN_PASSANT:
                    case TRANSLATE:
                    case PROMOTION:

                    case STEAL:

                    case HOSTAGE_EXCHANGE:
                    case DROP:
                }
        }
    }*/

    /**
     * Check which sides a player can castle to.
     * @param player The color of the player that is to be checked.
     * @return An array containing KING, QUEEN, neither or both, depending on which side the player can castle into
     * */
    public Value[] canCastleTo(Color player) {
        ArrayList<Value> result = new ArrayList<>(0);
        Board board = getBoard(player);
        Character boardName = board.name;
        int row = 0;
        if (player == Color.BLACK) row = 7;
        String rookSquareLeft = Board.getSquareName(row, 0);
        String rookSquareRight = Board.getSquareName(row, 7);


        // castled before
        if (moveHistory.stream().anyMatch(move ->
                move.player == player &&
                MoveType.of(move) == MoveType.CASTLE)) return new Value[0];

        // king moved
        if (moveHistory.stream().anyMatch(move ->
                move.player == player &&
                Arrays.asList(move.pieceNames).contains('K'))) return new Value[0];

        // queen rook hasn't moved
        if (!moveHistory.stream().anyMatch(move ->
                move.player == player &&
                Arrays.asList(move.pieceNames).contains('R') &&
                move.getSource() != null &&
                move.getSource().getValue().equals(rookSquareLeft))) result.add(Value.QUEEN);

        // king rook hasn't moved
        if (!moveHistory.stream().anyMatch(move ->
                move.player == player &&
                Arrays.asList(move.pieceNames).contains('R') &&
                move.getSource() != null &&
                move.getSource().getValue().equals(rookSquareRight))) result.add(Value.KING);

        return result.toArray(new Value[result.size()]);
    }

    public Value[] canCastleTo(){
        return canCastleTo(turnPlayer);
    }

    private void undoLastMove(){
        undoMove(getLatestMove());
    }

    private void undoMove(Move move){
        switch (MoveType.of(move)){
            case CASTLE:
                Board board = getBoard(turnPlayer);
                int row = 0;
                if (turnPlayer == Color.BLACK) row = 7;
                Square newRookSquare = board.getSquare(row, 0);
                Square newKingSquare = board.getSquare(row, 5);
                Square oldRookSquare = board.getSquare(row, 3);
                Square oldKingSquare = board.getSquare(row, 2);
                if (move.pieceNames[0] == 'K') {
                    oldRookSquare = board.getSquare(row, 7);
                    newRookSquare = board.getSquare(row, 5);
                    newKingSquare = board.getSquare(row, 6);
                }
                Piece king = board.popPiece(oldKingSquare);
                Piece rook = board.popPiece(oldRookSquare);
                board.setPiece(newKingSquare, king);
                board.setPiece(newRookSquare, rook);
                rook.setCanSwitchBoards(true);
                break;

            case DROP:
                Value value = Value.of(move.pieceNames[0]);
                getAirfield(turnPlayer).addPiece(value);
                gamma.removePiece(move.squareNames[0]);
                break;

            case HOSTAGE_EXCHANGE:
                Value dropValue = Value.of(move.pieceNames[0]);
                for (int i=1; i<move.pieceNames.length; i++){
                    value = Value.of(move.pieceNames[i]);
                    getPrison(turnPlayer).addPiece(value);
                    getAirfield(turnPlayer.opposite()).removePiece(value);
                }
                gamma.removePiece(move.squareNames[0]);
                getPrison(turnPlayer.opposite()).addPiece(dropValue);
                break;

            case SWAP:
                Board sourceBoard = getBoard(move.boardNames[0]);
                Board destinationBoard = getBoard(move.boardNames[1]);
                String squareName = move.squareNames[0];
                Piece sourcePiece = sourceBoard.popPiece(squareName);
                Piece destinationPiece = destinationBoard.popPiece(squareName);
                sourceBoard.setPiece(squareName, destinationPiece);
                destinationBoard.setPiece(squareName, sourcePiece);
                break;

            case CAPTURE:
                sourceBoard = getBoard(move.boardNames[0]);
                destinationBoard = getBoard(move.boardNames[1]);

                destinationPiece = new Piece(Value.of(move.pieceNames[1]), turnPlayer.opposite());
                getPrison(turnPlayer).removePiece(destinationPiece.value);

                // undo promotion
                if (move.promotion != null){
                    destinationBoard.setPiece(move.squareNames[0],
                            new Piece(Value.PAWN, turnPlayer));
                }
                else {
                    sourcePiece = destinationBoard.popPiece(move.squareNames[1]);
                    sourceBoard.setPiece(move.squareNames[0], sourcePiece);
                }

                sourceBoard.setPiece(move.squareNames[1], destinationPiece);
                break;

            case STEAL:
                Board destinationBoardPlayer = getBoard(move.boardNames[1]);
                Board destinationBoardOpponent = getBoard(move.boardNames[2]);
                sourcePiece = destinationBoardPlayer.popPiece(move.squareNames[1]);
                destinationPiece = destinationBoardOpponent.popPiece(move.squareNames[1]);
                destinationPiece.switchColor();
                gamma.setPiece(move.squareNames[0], sourcePiece);
                gamma.setPiece(move.squareNames[1], destinationPiece);
                break;

            case EN_PASSANT:
                int direction = turnPlayer == Color.WHITE? -1: 1;

                // coords where the piece was after it captured
                int[] sourceCoordinates = Board.getCoordinates(move.squareNames[1]);
                // coords where the piece was before it captured
                int[] destinationCoordinates = Board.getCoordinates(move.squareNames[0]);

                sourceBoard = getBoard(move.boardNames[1]);
                destinationBoard = getBoard(move.boardNames[0]);

                // piece that captured
                destinationPiece = sourceBoard.popPiece(sourceCoordinates);
                destinationBoard.setPiece(destinationCoordinates, destinationPiece);

                // piece that was captured
                getPrison(turnPlayer).removePiece(Value.PAWN);
                sourceCoordinates[0] += direction;
                sourceBoard.setPiece(sourceCoordinates,
                        new Piece(Value.PAWN, turnPlayer.opposite()));

                break;

            case TRANSLATE:
                sourceBoard = getBoard(move.boardNames[0]);
                destinationBoard = getBoard(move.boardNames[1]);
                // undo promotion
                if (move.promotion != null) {
                    destinationBoard.removePiece(move.squareNames[1]);
                    sourceBoard.setPiece(move.squareNames[0], new Piece(Value.PAWN, turnPlayer));
                }
                else {
                    Piece piece = destinationBoard.popPiece(move.squareNames[1]);
                    sourceBoard.setPiece(move.squareNames[0], piece);
                }

                break;
        }
        return;
    }

    /**
     * Does no checking whatsoever; simply does the move. Duh. Check for validity using validMove().
     * */
    public void doMove(Move move){
        switch (MoveType.of(move)){
            case CASTLE:
                Board board = getBoard(turnPlayer);
                int row = (turnPlayer == Color.WHITE)? 0: 7;
                Square oldRookSquare = board.getSquare(row, 0);
                Square oldKingSquare = board.getSquare(row, 4);
                Square newRookSquare = board.getSquare(row, 3);
                Square newKingSquare = board.getSquare(row, 2);
                if (move.pieceNames[0] == 'K') {
                    oldRookSquare = board.getSquare(row, 7);
                    newRookSquare = board.getSquare(row, 5);
                    newKingSquare = board.getSquare(row, 6);
                }
                Piece king = board.popPiece(oldKingSquare);
                Piece rook = board.popPiece(oldRookSquare);
                board.setPiece(newKingSquare, king);
                board.setPiece(newRookSquare, rook);
                rook.setCanSwitchBoards(false);
                break;

            case DROP:
                Value value = Value.of(move.pieceNames[0]);
                Piece piece = new Piece(value, turnPlayer);
                getAirfield(turnPlayer).removePiece(value);
                gamma.setPiece(move.squareNames[0], piece);
                break;

            case HOSTAGE_EXCHANGE:
                Value dropValue = Value.of(move.pieceNames[0]);
                piece = new Piece(dropValue, turnPlayer);
                for (int i=1; i<move.pieceNames.length; i++){
                    value = Value.of(move.pieceNames[i]);
                    getPrison(turnPlayer).removePiece(value);
                    getAirfield(turnPlayer.opposite()).addPiece(value);
                }
                getPrison(turnPlayer.opposite()).removePiece(dropValue);
                gamma.setPiece(move.squareNames[0], piece);
                break;

            case SWAP:
                Board sourceBoard = getBoard(move.boardNames[0]);
                Board destinationBoard = getBoard(move.boardNames[1]);
                String squareName = move.squareNames[0];
                Piece sourcePiece = sourceBoard.popPiece(squareName);
                Piece destinationPiece = destinationBoard.popPiece(squareName);
                sourceBoard.setPiece(squareName, destinationPiece);
                destinationBoard.setPiece(squareName, sourcePiece);
                break;

            case CAPTURE:
                sourceBoard = getBoard(move.boardNames[0]);
                destinationBoard = getBoard(move.boardNames[1]);
                sourcePiece = sourceBoard.popPiece(move.squareNames[0]);
                destinationPiece = sourceBoard.popPiece(move.squareNames[1]);
                destinationBoard.setPiece(move.squareNames[1], sourcePiece);
                getPrison(turnPlayer).addPiece(destinationPiece.value);
                if (move.pieceNames[0] == 'P' &&
                        Board.getCoordinates(move.squareNames[1])[0] % 7 == 0) {
                    promote(sourceBoard.getSquare(move.squareNames[1]),
                            Value.of(move.promotion));
                }
                break;

            case STEAL:
                Board destinationBoardPlayer = getBoard(move.boardNames[1]);
                Board destinationBoardOpponent = getBoard(move.boardNames[2]);
                sourcePiece = gamma.popPiece(move.squareNames[0]);
                destinationPiece = gamma.popPiece(move.squareNames[1]);
                destinationPiece.switchColor();
                destinationBoardPlayer.setPiece(move.squareNames[1], sourcePiece);
                destinationBoardOpponent.setPiece(move.squareNames[1], destinationPiece);
                break;

            case EN_PASSANT:
                int pawnRow = 3;
                if (turnPlayer == Color.BLACK) pawnRow = 4;
                int destinationColumn = Board.getCoordinates(move.squareNames[1])[1];
                sourceBoard = getBoard(move.boardNames[0]);
                destinationBoard = getBoard(move.boardNames[1]);
                sourcePiece = sourceBoard.popPiece(move.squareNames[0]);
                sourceBoard.removePiece(pawnRow, destinationColumn);  // capture
                destinationBoard.setPiece(move.squareNames[1], sourcePiece);
                getPrison(turnPlayer).addPiece(Value.PAWN);
                break;

            case TRANSLATE:
                sourceBoard = getBoard(move.boardNames[0]);
                destinationBoard = getBoard(move.boardNames[1]);
                sourcePiece = sourceBoard.popPiece(move.squareNames[0]);
                destinationBoard.setPiece(move.squareNames[1], sourcePiece);
                if (move.pieceNames[0] == 'P' &&
                        Board.getCoordinates(move.squareNames[1])[0] % 7 == 0){
                    promote(sourceBoard.getSquare(move.squareNames[1]),
                            Value.of(move.promotion));
                }
                break;
        }

    }

    private void promote(Square square, Value value){
        Board board = square.board;
        board.removePiece(square);
        board.setPiece(square, new Piece(value, turnPlayer));
    }

    public Board getBoard(Character name){
        switch (name){
            case 'A': return  alpha;
            case 'B': return  beta;
            case 'C': return  gamma;
        }
        return null;
    }

    public Board getBoard(Color color){
        switch (color){
            case WHITE: return  alpha;
            case BLACK: return  beta;
            case NONE: return  gamma;
        }
        return null;
    }

    public PieceCollection getPrison(Color color){
        return prisons.get(color);
    }

    public PieceCollection getAirfield(Color color){
        return airfields.get(color);
    }

    private void removeMovesAfter(int turn){
        moveHistory.subList(turn, moveHistory.size()).clear();
    }

    public void addMove(Move move){
        doMove(move);
        turnPlayer = turnPlayer.opposite();  // necessary for getGameState
        Move moveWithState = new Move(
                move.player, getGameState(), move.promotion,
                move.pieceNames, move.boardNames, move.squareNames
        );
        turnPlayer = turnPlayer.opposite();
        removeMovesAfter(currentPly);
        moveHistory.add(moveWithState);
        incrementPly();
    }

    public void rewindMove(){
        if (decrementPly()) undoMove(moveHistory.get(currentPly));
    }

    public void repeatMove(){
        if (currentPly < moveHistory.size() - 1) {
            doMove(moveHistory.get(currentPly));
            incrementPly();
        }
    }

    private boolean incrementPly(){
        if (currentPly < moveHistory.size()){
            currentPly ++;
            turnPlayer = turnPlayer.opposite();
            return true;
        }
        return false;
    }

    private boolean decrementPly(){
        if (currentPly > 0){
            currentPly --;
            turnPlayer = turnPlayer.opposite();
            return true;
        }
        return false;
    }

    public int getCurrentPly(){
        return currentPly;
    }

    public String encodeMove(Move move) throws IllegalArgumentException{
        if (move == null || MoveType.of(move) == null){
            throw new IllegalArgumentException("Invalid Move");
        }

        String moveString = "";
        String promotionString = move.promotion==null? "": "="+move.promotion;
        String stateString = move.state==null? "": move.state.toString();

        switch (MoveType.of(move)) {
            case CASTLE:
                moveString = "O-O";
                if (move.pieceNames[0] == 'Q') moveString = "O-O-O";
                break;

            case DROP:
            case HOSTAGE_EXCHANGE:
                String hostagesGiven = "";
                for (int i=1; i<move.pieceNames.length; i++){
                    hostagesGiven += move.pieceNames[i];
                }
                moveString = String.format("%s>%s%s",
                        hostagesGiven,
                        move.pieceNames[0],
                        move.squareNames[0]);
                break;
            case SWAP:
                moveString = String.format("%s%s-%s%s_%s%s%s",
                        move.pieceNames[0],
                        move.squareNames[0],
                        move.pieceNames[1],
                        promotionString,
                        move.boardNames[0],
                        move.boardNames[1],
                        stateString);
                break;
            case CAPTURE:
                moveString = String.format("%s%sx%s%s%s_%s>%s%s",
                        move.pieceNames[0],
                        move.squareNames[0],
                        move.pieceNames[1],
                        move.squareNames[1],
                        promotionString,
                        move.boardNames[0],
                        move.boardNames[1],
                        stateString);
                break;
            case STEAL:
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
                break;
            case EN_PASSANT:
                moveString = String.format("P%sxP%s_%s>%s%s",
                        move.squareNames[0],
                        move.squareNames[1],
                        move.boardNames[0],
                        move.boardNames[1],
                        stateString);
                break;
            case TRANSLATE:
                moveString = String.format("%s%s-%s_%s>%s%s",
                        move.pieceNames[0],
                        move.squareNames[0],
                        move.squareNames[1],
                        move.boardNames[0],
                        move.boardNames[1],
                        stateString);
                break;
            }
        return moveString;
    }

    public Move decodeMove(String moveString){
        Character state = null;
        Character promotion = null;
        Character[] pieceNames = null;
        Character[] boardNames = null;
        String[] squareNames = null;

        int dropIndex = moveString.indexOf('>');
        int constellationIndex = moveString.indexOf('_');

        switch (moveString.charAt(moveString.length() - 1)){  // last char
            case '+':
            case '#':
            case '@':
                state = moveString.charAt(moveString.length() - 1);
                moveString = moveString.substring(0, moveString.length() - 1);
        }

        if (moveString.startsWith("O-O")) {  // castle
            pieceNames = new Character[1];
            boardNames = new Character[0];
            squareNames = new String[0];
            if (moveString.equals("O-O-O")) pieceNames[0] = 'Q';
            else pieceNames[0] = 'K';
        } else if (constellationIndex == -1 && dropIndex != -1){  // drop, exchange
            String chars = moveString.substring(dropIndex+1, dropIndex+2);
            chars += moveString.substring(0, dropIndex);
            pieceNames = chars.chars()
                    .mapToObj(c -> (char)c)
                    .toArray(Character[]::new);
            boardNames = new Character[0];
            squareNames = new String[]{moveString.substring(dropIndex+2, dropIndex+4)};
        } else {
            if (moveString.charAt(constellationIndex - 2) == '=') {
                promotion = moveString.charAt(constellationIndex - 1);
                moveString = moveString.substring(0, constellationIndex-2) +
                             moveString.substring(constellationIndex);
            }
            switch (moveString.charAt(3)) {
                case '-': {  // swap, translation
                    if ("PBNRQK".indexOf(moveString.charAt(4)) != -1) {  // swap
                        pieceNames = new Character[]{moveString.charAt(0), moveString.charAt(4)};
                        boardNames = new Character[]{moveString.charAt(6), moveString.charAt(7)};
                        squareNames = new String[]{moveString.substring(1, 3)};
                    } else {  // translation
                        pieceNames = new Character[]{
                                moveString.charAt(0)
                        };
                        boardNames = new Character[]{
                                moveString.charAt(7),
                                moveString.charAt(9)
                        };
                        squareNames = new String[]{
                                moveString.substring(1, 3),
                                moveString.substring(4, 6),
                        };
                    }
                    break;
                }
                case 'x':{  // capture, steal
                    if ("PBNRQK".indexOf(moveString.charAt(4)) == -1){  // en passant
                        pieceNames = new Character[0];
                        boardNames = new Character[]{
                                moveString.charAt(7),
                                moveString.charAt(9)
                        };
                        squareNames = new String[]{
                                moveString.substring(1, 3),
                                moveString.substring(4, 6)
                        };
                    }
                    else if (moveString.charAt(8) == 'C'){  // steal
                        pieceNames = new Character[]{
                                moveString.charAt(0),
                                moveString.charAt(4)
                        };
                        boardNames = new Character[]{
                                'C',
                                moveString.charAt(10),
                                moveString.charAt(12)
                        };
                        squareNames = new String[]{
                                moveString.substring(1, 3),
                                moveString.substring(5, 7)
                        };
                    } else {  // capture
                        pieceNames = new Character[]{
                                moveString.charAt(0),
                                moveString.charAt(4)
                        };
                        boardNames = new Character[]{
                                moveString.charAt(8),
                                moveString.charAt(10)
                        };
                        squareNames = new String[]{
                                moveString.substring(1, 3),
                                moveString.substring(5, 7)
                        };
                    }
                }
            }
        }

        return new Move(
                turnPlayer,
                state,
                promotion,
                pieceNames,
                boardNames,
                squareNames
        );
    }

/*    public Move decodeMove(String moveString){
        Move move;
        Character state = null;
        Character promotion = null;
        Character[] pieceNames = null;
        Character[] boardNames = null;
        String[] squareNames = null;

        int dropIndex = moveString.indexOf('>');

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
        }else if (dropIndex != -1){  // drop, hostage exchange
            String pieces = moveString.substring(dropIndex+1, dropIndex+2);
            pieces += moveString.substring(0, dropIndex);
            pieceNames = pieces.chars()
                    .mapToObj(c -> (char)c)
                    .toArray(Character[]::new);
            boardNames = new Character[0];
            System.out.println(moveString);
            squareNames = new String[]{moveString.substring(dropIndex+2, dropIndex+4)};
            pieceNames[0] = moveString.charAt(1);
        }else {

            int startOfConstellation = moveString.indexOf('_');
            if (moveString.charAt(startOfConstellation-2) == '='){
                promotion = moveString.charAt(startOfConstellation-1);
            }

            if (moveString.charAt(3) == '-'){  // swap, translation
                if ("012345678".charAt(Character.getNumericValue(moveString.charAt(5)))==-1) { // swap
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
                pieceNames[0] = moveString.charAt(0);
                pieceNames[1] = moveString.charAt(4);
                if (moveString.charAt(startOfConstellation)=='C') {  // steal
                    boardNames = new Character[3];
                    boardNames[1] = moveString.charAt(startOfConstellation + 3);
                    boardNames[2] = moveString.charAt(startOfConstellation + 5);
                }else{  // capture/en passant
                    boardNames = new Character[2];
                    boardNames[1] = moveString.charAt(startOfConstellation + 3);
                    if ("PBNQR".indexOf(moveString.charAt(4))==-1){  // en passant
                        pieceNames = new Character[0];
                        squareNames[1] = moveString.substring(4, 6);
                    }
                }

            }
            squareNames[0] = moveString.substring(1, 3);
            boardNames[0] = moveString.charAt(startOfConstellation + 1);
        }
        move = new Move(
                turnPlayer,
                state,
                promotion,
                pieceNames,
                boardNames,
                squareNames
        );
        return move;
    }*/

    public boolean validMove(Move move){
        if (move == null || MoveType.of(move) == null) return false;
        Color turnPlayer = getCurrentPlayer();
        if (move.player != turnPlayer) return false;
        Pair<Character[], String> source = move.getSource();
        Pair<Character[], String> destination = move.getDestination();

        switch (MoveType.of(move)){
            case CASTLE:
                Board sourceBoard = getBoard(source.getKey()[0]);
                Piece[] pieces = sourceBoard.getPieces(Value.KING, getCurrentPlayer());
                Square kingSquare = sourceBoard.getSquare(pieces[0]);
                if (pieces.length == 0) return false;
                boolean found = false;
                for (Value side: canCastleTo()){
                    if(Value.of(move.pieceNames[0]) == side) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
                Square destinationSquare = sourceBoard.getSquare(destination.getValue());
                found = false;
                for (Square square: sourceBoard.canGoTo(kingSquare)){
                    if(square == destinationSquare) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
                break;

            // some things are already validated by validSwaps()
            case SWAP:
                sourceBoard = getBoard(move.boardNames[0]);
                Board destinationBoard = getBoard(move.boardNames[1]);
                String squareName = move.squareNames[0];
                Piece sourcePiece = sourceBoard.getPiece(squareName);
                Piece destinationPiece = destinationBoard.getPiece(squareName);
                if (sourcePiece == null || destinationPiece == null) return false;
                if (sourcePiece == destinationPiece) return false;
                if (!sourcePiece.getCanSwitchBoards() || !destinationPiece.getCanSwitchBoards())
                    return false;

                // cannot initiate swap to gamma
                if (destinationBoard == gamma) return false;
                // can only initiate swap against lower valued pieces
                if (sourcePiece.value.value < destinationPiece.value.value) return false;
                // same color-value configuration not allowed
                if (sourcePiece.value == destinationPiece.value &&
                    sourcePiece.getColor() == destinationPiece.getColor()) return false;
                break;

            case STEAL:
                Board destinationBoardPlayer = getBoard(move.boardNames[1]);
                Board destinationBoardOpponent = getBoard(move.boardNames[2]);
                sourcePiece = gamma.getPiece(move.squareNames[0]);
                destinationPiece = gamma.getPiece(move.squareNames[1]);
                if (sourcePiece == null || destinationPiece == null) return false;
                if (destinationBoardOpponent == gamma) return false;
                if (destinationBoardOpponent.getPiece(move.squareNames[1]) != null) return false;
                // have to move to other board if possible
                if (destinationBoardPlayer == gamma){
                    if (alpha.getPiece(move.squareNames[1]) == null &&
                            beta.getPiece(move.squareNames[1]) == null) return false;
                } else {
                    if (destinationBoardPlayer.getPiece(move.squareNames[1]) != null) return false;
                }
                break;

            case CAPTURE:
                sourceBoard = getBoard(move.boardNames[0]);
                if (sourceBoard == gamma) return false;  // need to steal instead
                destinationPiece = sourceBoard.getPiece(move.squareNames[1]);
                if (destinationPiece == null) return false;
                if (destinationPiece.getColor() == getCurrentPlayer()) return false;
                if (move.boardNames[0] == move.boardNames[1]) {
                    if (sourceBoard.getPiece(move.squareNames[0]).getCanSwitchBoards()) {
                        switch (move.boardNames[0]) {
                            case 'A':
                                if (beta.getPiece(move.squareNames[1]) == null &&
                                        !isPinned(  // can move to other board without exposing king
                                                new Move(
                                                        turnPlayer,
                                                        move.state,
                                                        move.promotion,
                                                        move.pieceNames,
                                                        new Character[]{'A', 'B'},
                                                        move.squareNames
                                                ), turnPlayer)) return false;
                                break;
                            case 'B':
                                if (alpha.getPiece(move.squareNames[1]) == null &&
                                        !isPinned(
                                                new Move(
                                                        turnPlayer,
                                                        move.state,
                                                        move.promotion,
                                                        move.pieceNames,
                                                        new Character[]{'B', 'A'},
                                                        move.squareNames
                                                ), turnPlayer)) return false;
                                break;
                            default:
                                break;
                            case 'C':
                                if (beta.getPiece(move.squareNames[1]) == null ||
                                        alpha.getPiece(move.squareNames[1]) == null) return false;
                        }
                    }
                } else {  // switching boards
                    // can't move to gamma
                    if (move.boardNames[1] == 'C') return false;
                    if (!sourceBoard.getPiece(move.squareNames[0]).getCanSwitchBoards()){
                        return false;
                    }
                    if (getBoard(move.boardNames[1]).getPiece(move.squareNames[1]) != null){
                        return false;  // can't move to an occupied square
                    }
                }
                break;

            case TRANSLATE:
                sourceBoard = getBoard(move.boardNames[0]);

                // would be capture
                if (!move.squareNames[0].equals(move.squareNames[1]) && // not null move
                        sourceBoard.getPiece(move.squareNames[1]) != null) return false;

                // staying on same board
                if (move.boardNames[0] == move.boardNames[1]){
                    // can't null move and stay on same board
                    if (move.squareNames[0].equals(move.squareNames[1])) return false;
                    // have to switch boards if possible
                    if (sourceBoard.getPiece(move.squareNames[0]).getCanSwitchBoards()) {
                        switch (move.boardNames[0]) {
                            case 'A':
                                if (beta.getPiece(move.squareNames[1]) == null &&
                                        !isPinned(  // can move to other board without exposing king
                                                new Move(
                                                        turnPlayer,
                                                        move.state,
                                                        move.promotion,
                                                        move.pieceNames,
                                                        new Character[]{'A', 'B'},
                                                        move.squareNames
                                                ), turnPlayer)) return false;
                                break;
                            case 'B':
                                if (alpha.getPiece(move.squareNames[1]) == null &&
                                        !isPinned(
                                                new Move(
                                                    turnPlayer,
                                                    move.state,
                                                    move.promotion,
                                                    move.pieceNames,
                                                    new Character[]{'B', 'A'},
                                                    move.squareNames
                                                ), turnPlayer)) return false;
                                break;
                            default:break;
                            case 'C':
                                if (beta.getPiece(move.squareNames[1]) == null ||
                                        alpha.getPiece(move.squareNames[1]) == null) return false;
                        }
                    }
                } else{  // switching boards
                    // can't move to gamma
                    if (move.boardNames[1] == 'C') return false;
                    if (!sourceBoard.getPiece(move.squareNames[0]).getCanSwitchBoards()){
                        return false;
                    }
                    if (getBoard(move.boardNames[1]).getPiece(move.squareNames[1]) != null){
                        return false;  // can't move to an occupied square
                    }
                }
                break;

            case EN_PASSANT:
                int pawnRow = move.player == Color.WHITE? 1: 6;
                int enPassantRow = move.player == Color.WHITE? 2: 5;
                int capturingColumn = Board.getCoordinates(move.squareNames[0])[1];
                int capturedColumn = Board.getCoordinates(move.squareNames[1])[1];
                if (move.boardNames[0] != 'A' && move.boardNames[0] != 'B') return false;
                if (Board.getCoordinates(move.squareNames[0])[0] != enPassantRow) return false;
                Move latestMove = getLatestMove();
                if (MoveType.of(latestMove) != MoveType.TRANSLATE) return false;
                int[] lastSource = Board.getCoordinates(latestMove.getSource().getValue());
                int[] lastDest = Board.getCoordinates(latestMove.getDestination().getValue());
                if (lastSource[0] != pawnRow || lastSource[1] != capturedColumn) return false;
                if (lastDest[0] != enPassantRow || lastDest[1] != capturedColumn) return false;
                break;

            case HOSTAGE_EXCHANGE:
            case DROP:
                throw new IllegalArgumentException("Can't validate Hostage Exchange or Drop" +
                        " with this function.");
        }
        if (isPinned(move, turnPlayer)) return false;
        return true;
    }

    private boolean isPinned(Move move, Color turnPlayer){
        boolean result = false;
        doMove(move);
        if (inCheck(turnPlayer)) result = true;
        undoMove(move);
        return result;
    }

    private boolean breakingCheck(Move move, Color turnPlayer){
        boolean result = false;
        if (inCheck(turnPlayer)){
            doMove(move);
            if (!inCheck(turnPlayer)) result = true;
            undoMove(move);
        }
        return result;
    }

    public Move[] getValidSwaps(Square sourceSquare){
        if (sourceSquare == null) return new Move[0];
        Board sourceBoard = sourceSquare.board;
        if (sourceBoard == null) return new Move[0];
        Piece sourcePiece = sourceBoard.getPiece(sourceSquare);
        if (sourcePiece == null) return new Move[0];

        if (!sourcePiece.getCanSwitchBoards()) return new Move[0];

        String squareName = Board.getSquareName(sourceSquare);
        ArrayList<Character> boardNames = new ArrayList<>(3);
        ArrayList<Move> validSwaps = new ArrayList<>(8);

        Piece alphaPiece = alpha.getPiece(squareName);
        Piece betaPiece = beta.getPiece(squareName);
        Piece gammaPiece = gamma.getPiece(squareName);

        if (alphaPiece != null && alphaPiece != sourcePiece &&
            !(alphaPiece.value == Value.ROOK &&
            alphaPiece.getCanSwitchBoards())) boardNames.add('A');

        if (betaPiece != null && betaPiece != sourcePiece &&
                !(betaPiece.value == Value.ROOK &&
                betaPiece.getCanSwitchBoards())) boardNames.add('B');

        if (gammaPiece != null && gammaPiece != sourcePiece &&
                !(gammaPiece.value == Value.ROOK &&
                gammaPiece.getCanSwitchBoards())) boardNames.add('C');

        for (Character boardName: boardNames){
            Move move = new Move(
                    turnPlayer, null, null,
                    new Character[]{
                            sourcePiece.value.name,
                            getBoard(boardName).getPiece(squareName).value.name
                    },
                    new Character[]{sourceBoard.name, boardName},
                    new String[]{squareName}
            );
            if (!inCheck() || breakingCheck(move, turnPlayer)) {
                validSwaps.add(move);
            }
        }


        return validSwaps.toArray(new Move[validSwaps.size()]);
    }

    private boolean rookHasAlreadyCastled(Square sourceSquare) {
        Character side = null;
        if (sourceSquare.column == 0) side = 'K';
        else if (sourceSquare.column == 7) side = 'Q';

        return side != null && rookHasAlreadyCastled(sourceSquare.board.color, side);
    }

    private boolean rookHasAlreadyCastled(Color player, char side){
        // queen rook hasn't moved
        return moveHistory.stream().anyMatch(move ->
                move.player == player &&
                MoveType.of(move) == MoveType.CASTLE &&
                move.pieceNames[0] == side
        );
    }

    private void addMoveIfValid(ArrayList<Move> moves,
                                       Color player,
                                       Character[] pieceNames,
                                       Character[] boardNames,
                                       String[] squareNames){
        Move move = new Move(player, null, null, pieceNames, boardNames, squareNames);
        if (validMove(move)){
            moves.add(move);
        }
    }

    public Move[] getValidMoves(Square sourceSquare){
        Piece sourcePiece = sourceSquare.board.getPiece(sourceSquare);
        if (sourcePiece == null || sourcePiece.getColor() != getCurrentPlayer()) return new Move[0];
        ArrayList<Move> moves = new ArrayList<>(10);  // 10 is arbitrary
        Board sourceBoard = sourceSquare.board;
        Square[] canGoTo = sourceBoard.canGoTo(sourcePiece);

        switch (sourcePiece.value){  // special cases
            case KING:
                //castle
                addMoveIfValid(
                        moves,
                        getCurrentPlayer(),
                        new Character[]{'K'},
                        new Character[0],
                        new String[0]);
                addMoveIfValid(
                        moves,
                        getCurrentPlayer(),
                        new Character[]{'Q'},
                        new Character[0],
                        new String[0]);
                break;
            case PAWN:
                // en passants
                Square[] attacks = sourceBoard.attacksSquares(sourcePiece);
                for (Square square:
                        attacks) {
                    addMoveIfValid(
                            moves,
                            getCurrentPlayer(),
                            new Character[0],
                            new Character[]{sourceBoard.name, 'A'},
                            new String[]{Board.getSquareName(sourceSquare),
                                    Board.getSquareName(square)});
                    addMoveIfValid(
                            moves,
                            getCurrentPlayer(),
                            new Character[0],
                            new Character[]{sourceBoard.name, 'B'},
                            new String[]{Board.getSquareName(sourceSquare),
                                    Board.getSquareName(square)});
                }
                break;
        }

        for (Square square:
                canGoTo) {
            Piece destinationPiece = sourceBoard.getPiece(square);
            if (destinationPiece == null){  // translate
                if (sourcePiece.value == Value.PAWN &&
                        square.column != sourceSquare.column) continue;  // pawns can't move diagonally
                addMoveIfValid(
                        moves,
                        getCurrentPlayer(),
                        new Character[]{sourcePiece.value.name},
                        new Character[]{sourceBoard.name, 'A'},
                        new String[]{Board.getSquareName(sourceSquare),
                                Board.getSquareName(square)}
                );
                addMoveIfValid(
                        moves,
                        getCurrentPlayer(),
                        new Character[]{sourcePiece.value.name},
                        new Character[]{sourceBoard.name, 'B'},
                        new String[]{Board.getSquareName(sourceSquare),
                                Board.getSquareName(square)}
                );
                addMoveIfValid(
                        moves,
                        getCurrentPlayer(),
                        new Character[]{sourcePiece.value.name},
                        new Character[]{sourceBoard.name, 'C'},
                        new String[]{Board.getSquareName(sourceSquare),
                                Board.getSquareName(square)}
                );
            } else{
                if(destinationPiece.getColor() == sourcePiece.getColor()) continue;
                if(sourceBoard.name == 'C'){  // steal
                    addMoveIfValid(
                            moves,
                            getCurrentPlayer(),
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{'C', 'A', 'B'},
                            new String[]{Board.getSquareName(sourceSquare),
                                    Board.getSquareName(square)});
                    addMoveIfValid(
                            moves,
                            getCurrentPlayer(),
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{'C', 'B', 'A'},
                            new String[]{Board.getSquareName(sourceSquare),
                                    Board.getSquareName(square)});
                    addMoveIfValid(
                            moves,
                            getCurrentPlayer(),
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{'C', 'C', 'A'},
                            new String[]{Board.getSquareName(sourceSquare),
                                    Board.getSquareName(square)});
                    addMoveIfValid(
                            moves,
                            getCurrentPlayer(),
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{'C', 'C', 'B'},
                            new String[]{Board.getSquareName(sourceSquare),
                                    Board.getSquareName(square)});
                } else{  // capture
                    addMoveIfValid(
                            moves,
                            getCurrentPlayer(),
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{sourceBoard.name, 'A'},
                            new String[]{Board.getSquareName(sourceSquare),
                                    Board.getSquareName(square)});
                    addMoveIfValid(
                            moves,
                            getCurrentPlayer(),
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{sourceBoard.name, 'B'},
                            new String[]{Board.getSquareName(sourceSquare),
                                    Board.getSquareName(square)});
                }
            }
        }
        //swap
        //moves.addAll(Arrays.asList(getValidSwaps(sourceSquare)));
        String squareName = Board.getSquareName(sourceSquare);
        Piece alphaPiece = alpha.getPiece(squareName);
        Piece betaPiece = beta.getPiece(squareName);
        if (alphaPiece != null) {
            addMoveIfValid(
                    moves,
                    getCurrentPlayer(),
                    new Character[]{
                            sourcePiece.value.name,
                            alphaPiece.value.name},
                    new Character[]{sourceBoard.name, 'A'},
                    new String[]{squareName}
            );
        }
        if (betaPiece != null) {
            addMoveIfValid(
                    moves,
                    getCurrentPlayer(),
                    new Character[]{
                            sourcePiece.value.name,
                            betaPiece.value.name},
                    new Character[]{sourceBoard.name, 'B'},
                    new String[]{squareName}
            );
        }
        // null moves
        addMoveIfValid(
                moves,
                getCurrentPlayer(),
                new Character[]{sourcePiece.value.name},
                new Character[]{sourceBoard.name, 'A'},
                new String[]{Board.getSquareName(sourceSquare),
                        Board.getSquareName(sourceSquare)}
        );
        addMoveIfValid(
                moves,
                getCurrentPlayer(),
                new Character[]{sourcePiece.value.name},
                new Character[]{sourceBoard.name, 'B'},
                new String[]{Board.getSquareName(sourceSquare),
                        Board.getSquareName(sourceSquare)}
        );
        return moves.toArray(new Move[moves.size()]);
    }

    public Move[] getValidMoves(Square sourceSquare, Square destinationSquare){
        return getValidMoves(getValidMoves(sourceSquare), destinationSquare);
    }

    public Move[] getValidMoves(Move[] moves, Square destinationSquare){
        return Arrays
                .stream(moves)
                .filter(move -> hasDestination(move, destinationSquare))
                .toArray(size -> new Move[size]);
    }

    public boolean hasDestination(Move move, Square destinationSquare){
        Pair destination = move.getDestination();
        String destinationSquareName = (String) destination.getValue();
        return destinationSquareName.equals(Board.getSquareName(destinationSquare));
    }

    public String[] getMoveHistoryStrings() {
        LinkedList<String> result = new LinkedList<>();
        moveHistory.stream().forEachOrdered(move -> result.add(encodeMove(move)));
        return result.toArray(new String[result.size()]);
    }

    public void loadMoves(String[] moveStrings, int upToPly){
        LinkedList<Move> moves = new LinkedList<>();
        for (String moveString: moveStrings){
            moves.add(decodeMove(moveString));
        }
        loadMoves(moves.toArray(new Move[moves.size()]), upToPly);
    }

    public void loadMoves(Move[] moves, int upToPly) {
        resetGame();
        moveHistory.addAll(Arrays.asList(moves));
        if (upToPly == -1 || upToPly < 0 || upToPly >= moves.length){
            upToPly = moves.length;
        }
        for (int i=0; i<upToPly; i++){
            doMove(moveHistory.get(i));
            incrementPly();
        }
    }

    public void resetGame(){
        moveHistory.clear();
        currentPly = 0;
        turnPlayer = Color.WHITE;

        alpha = new Board(Color.WHITE, 'A');
        beta = new Board(Color.BLACK, 'B');
        gamma = new Board(Color.NONE, 'C');
        prisons = new EnumMap<>(Color.class);
        prisons.put(Color.WHITE, new PieceCollection());
        prisons.put(Color.BLACK, new PieceCollection());
        airfields = new EnumMap<>(Color.class);
        airfields.put(Color.WHITE, new PieceCollection());
        airfields.put(Color.BLACK, new PieceCollection());
    }

    public Character getGameState() {
        if (inCheck()){
            if (canDoAMove(turnPlayer)) return '+';
            return '#';
        }
        if (!canDoAMove(turnPlayer)) return '@';  // stalemate
        return null;
    }

    private boolean canDoAMove(Color player){
        Piece[] allPieces;
        Board[] boards = {alpha, beta, gamma};
        for (Board board: boards) {
            allPieces = board.getPieces(null, player);
            for (Piece piece: allPieces){
                if (getValidMoves(board.getSquare(piece)).length != 0){
                    return true;
                }
            }
        }
        return false;
    }

    public Move[] getAllCheskBreakingMoves(Color player){
        // assume in check, player is player in check
        ArrayList<Move> result = new ArrayList<>();
        Piece[] allPieces;
        Board[] boards = {alpha, beta, gamma};
        Move[] validMoves;
        for (Board board: boards) {
            allPieces = board.getPieces(null, player);
            for (Piece piece: allPieces){
                validMoves = getValidMoves(board.getSquare(piece));
                if (validMoves.length != 0){
                    result.addAll(Arrays.asList(validMoves));
                }
            }
        }
        return result.toArray(new Move[result.size()]);
    }
}
