package Game;

import Shared.Color;
import Shared.Value;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * this class will control the flow of the game
 * */

public class Controller {
    private Board alpha;
    private Board beta;
    private Board gamma;
    private HashMap<Color, PieceCollection> prisons;
    private HashMap<Color, PieceCollection> airfields;
    private Color turnPlayer = Color.WHITE;
    private LinkedList<String> moves = new LinkedList<>();
    private int currentPly = 0;

    public Controller() {
        alpha = new Board(Color.WHITE, 'A');
        beta = new Board(Color.BLACK, 'B');
        gamma = new Board(Color.NONE, 'C');
        prisons = new HashMap<>(2);
        prisons.put(Color.WHITE, new PieceCollection());
        prisons.put(Color.BLACK, new PieceCollection());
        airfields = new HashMap<>(2);
        airfields.put(Color.WHITE, new PieceCollection());
        airfields.put(Color.BLACK, new PieceCollection());
    }

    public Color getCurrentPlayer(){
        return turnPlayer;
    }

    public Move getLatestMove(){
        return decodeMove(moves.getLast());
    }

    public String[] getMoves(Color player){
        int parity = 0;
        String[] result = new String[(int)Math.ceil((moves.size()/2.0))];
        if (player == Color.BLACK) {
            parity = 1;
            result = new String[(int)Math.floor((moves.size()/2.0))];
        }
        for (int ply = 0; ply < result.length; ply++){
            result[ply] = moves.get(2*ply+parity);
        }
        return result;
    }

    public String[] searchMoves(String regex){
        return Arrays.stream(getMoves()).filter(move -> move.matches(regex))
                .toArray(size -> new String[size]);
    }
    public String[] searchMoves(String regex, Color player){
        return Arrays.stream(getMoves(player)).filter(move -> move.matches(regex))
                .toArray(size -> new String[size]);
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
        String kingSquare = Board.getSquareName(row, 4);
        String rookSquareLeft = Board.getSquareName(row, 0);
        String rookSquareRight = Board.getSquareName(row, 7);

        if (searchMoves("O-O(-O)?", player).length != 0) return new Value[0];  // castled
        // king moved
        if (searchMoves(String.format(".*K%s.*_%s", kingSquare, boardName)).length != 0) {
            return new Value[0];
        }
        if ((searchMoves(String.format(".*R%s.*_%s",
                rookSquareLeft, boardName))
                .length == 0) &&  // left rook moved
                (searchMoves(String.format("[PBNRQ]%s-R_[A-C]%s.*",
                        rookSquareLeft, boardName))
                        .length == 0) &&  // left rook swapped
                (searchMoves(String.format("[PBNRQ]%s-R-[PBNRQ]_[A-C]%s.*",
                        rookSquareLeft, boardName))
                        .length == 0) &&  // left rook swapped
                (searchMoves(String.format("[PBNRQ]%s-[PBNRQ]-R_[A-C][A-C]%s.*",
                        rookSquareLeft, boardName))
                        .length == 0)){  // left rook swapped
            result.add(Value.QUEEN);
        }
        if ((searchMoves(String.format(".*R%s.*_%s",
                rookSquareRight, boardName))
                .length == 0) &&  // right rook moved
                (searchMoves(String.format("[PBNRQ]%s-R_[A-C]%s.*",
                        rookSquareRight, boardName))
                        .length == 0) &&  // right rook swapped
                (searchMoves(String.format("[PBNRQ]%s-R-[PBNRQ]_[A-C]%s.*",
                        rookSquareRight, boardName))
                        .length == 0) &&  // right rook swapped
                (searchMoves(String.format("[PBNRQ]%s-[PBNRQ]-R_[A-C]{2}%s.*",
                        rookSquareRight, boardName))
                        .length == 0)){  // right rook swapped
            result.add(Value.KING);
        }
        return result.toArray(new Value[result.size()]);
    }

    public Value[] canCastleTo(){
        return canCastleTo(turnPlayer);
    }

    public void undoMove(Move move){
        if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 0){  // castle:
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

        }else if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 1){  // drop
            Value value = Value.getValueFromName(move.pieceNames[0]);
            getAirfield(turnPlayer).addPiece(value);
            gamma.removePiece(Board.getCoordinates(move.squareNames[0]));

        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 1) {  // hostage exchange
            Value exchangeValue = Value.getValueFromName(move.pieceNames[0]);
            Value dropValue = Value.getValueFromName(move.pieceNames[1]);
            getPrison(turnPlayer).addPiece(exchangeValue);
            getAirfield(turnPlayer.opposite()).removePiece(exchangeValue);
            getPrison(turnPlayer.opposite()).addPiece(dropValue);
            gamma.removePiece(move.squareNames[0]);

        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 4 &&
                move.squareNames.length == 1) {  // swap2
            Board sourceBoard1 = getBoard(move.boardNames[2]);
            Board sourceBoard2 = getBoard(move.boardNames[3]);
            Board destinationBoard1 = getBoard(move.boardNames[0]);
            Board destinationBoard2 = getBoard(move.boardNames[1]);
            String squareName = move.squareNames[0];
            Piece piece1 = sourceBoard1.popPiece(squareName);
            Piece piece2 = sourceBoard2.popPiece(squareName);
            destinationBoard1.setPiece(squareName, piece1);
            destinationBoard2.setPiece(squareName, piece2);

        }else if (move.pieceNames.length == 3 &&
                move.boardNames.length == 6 &&
                move.squareNames.length == 1) {  // swap3
            Board sourceBoard1 = getBoard(move.boardNames[3]);
            Board sourceBoard2 = getBoard(move.boardNames[4]);
            Board sourceBoard3 = getBoard(move.boardNames[5]);
            Board destinationBoard1 = getBoard(move.boardNames[0]);
            Board destinationBoard2 = getBoard(move.boardNames[1]);
            Board destinationBoard3 = getBoard(move.boardNames[2]);
            String squareName = move.squareNames[0];
            Piece piece1 = sourceBoard1.popPiece(squareName);
            Piece piece2 = sourceBoard2.popPiece(squareName);
            Piece piece3 = sourceBoard3.popPiece(squareName);
            destinationBoard1.setPiece(squareName, piece1);
            destinationBoard2.setPiece(squareName, piece2);
            destinationBoard3.setPiece(squareName, piece3);

        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // capture
            Board sourceBoard = getBoard(move.boardNames[1]);
            Board destinationBoard = getBoard(move.boardNames[0]);
            Piece sourcePiece = sourceBoard.popPiece(move.squareNames[1]);
            Piece destinationPiece = sourceBoard.popPiece(move.squareNames[0]);
            destinationBoard.setPiece(move.squareNames[1], sourcePiece);
            getPrison(turnPlayer).removePiece(destinationPiece.value);
            if (move.pieceNames[0] == 'P' &&
                    Board.getCoordinates(move.squareNames[1])[0] % 7 == 0) {
                destinationBoard.setPiece(move.squareNames[1], new Piece(Value.PAWN, turnPlayer));
            }

        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 3 &&
                move.squareNames.length == 2) {  // steal
            Board destinationPlayer = getBoard(move.boardNames[2]);
            Board destinationOpponent = getBoard(move.boardNames[1]);
            Piece sourcePiece = destinationOpponent.popPiece(move.squareNames[0]);
            Piece destinationPiece = destinationPlayer.popPiece(move.squareNames[1]);
            destinationPlayer.setPiece(move.squareNames[1], sourcePiece);
            destinationOpponent.setPiece(move.squareNames[1], destinationPiece);

        }else if (move.pieceNames.length == 0 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // en passant
            int pawnRow = 3;
            if (turnPlayer == Color.BLACK) pawnRow = 4;
            int destinationColumn = Board.getCoordinates(move.squareNames[1])[1];
            Board sourceBoard = getBoard(move.boardNames[1]);
            Board destinationBoard = getBoard(move.boardNames[0]);
            Piece sourcePiece = sourceBoard.popPiece(move.squareNames[0]);
            sourceBoard.setPiece(pawnRow, destinationColumn, sourcePiece);  // capture
            destinationBoard.removePiece(move.squareNames[1]);
            getPrison(turnPlayer).removePiece(Value.PAWN);

        }else if (move.pieceNames.length == 1 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // translate
            Board sourceBoard = getBoard(move.boardNames[1]);
            Board destinationBoard = getBoard(move.boardNames[0]);
            Piece sourcePiece = sourceBoard.popPiece(move.squareNames[1]);
            destinationBoard.setPiece(move.squareNames[0], sourcePiece);
            if (move.pieceNames[0] == 'P' &&
                    Board.getCoordinates(move.squareNames[1])[0] % 7 == 0) {
                destinationBoard.setPiece(move.squareNames[1], new Piece(Value.PAWN, turnPlayer));
            }
        }
    }

    /**
     * Does no checking whatsoever; simply does the move. Duh. Check for validity elsewhere.
     * */
    public void doMove(Move move){
        if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 0){  // castle:
            Board board = getBoard(turnPlayer);
            int row = 0;
            if (turnPlayer == Color.BLACK) row = 7;
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

        }else if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 1){  // drop
            Value value = Value.getValueFromName(move.pieceNames[0]);
            Piece piece = new Piece(value, turnPlayer);
            getAirfield(turnPlayer).removePiece(value);
            gamma.setPiece(move.squareNames[0], piece);

        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 1) {  // hostage exchange
            Value exchangeValue = Value.getValueFromName(move.pieceNames[0]);
            Value dropValue = Value.getValueFromName(move.pieceNames[1]);
            Piece piece = new Piece(dropValue, turnPlayer);
            getPrison(turnPlayer).removePiece(exchangeValue);
            getAirfield(turnPlayer.opposite()).addPiece(exchangeValue);
            getPrison(turnPlayer.opposite()).removePiece(dropValue);
            gamma.setPiece(move.squareNames[0], piece);

        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 4 &&
                move.squareNames.length == 1) {  // swap2
            Board sourceBoard1 = getBoard(move.boardNames[0]);
            Board sourceBoard2 = getBoard(move.boardNames[1]);
            Board destinationBoard1 = getBoard(move.boardNames[2]);
            Board destinationBoard2 = getBoard(move.boardNames[3]);
            String squareName = move.squareNames[0];
            Piece piece1 = sourceBoard1.popPiece(squareName);
            Piece piece2 = sourceBoard2.popPiece(squareName);
            destinationBoard1.setPiece(squareName, piece1);
            destinationBoard2.setPiece(squareName, piece2);

        }else if (move.pieceNames.length == 3 &&
                move.boardNames.length == 6 &&
                move.squareNames.length == 1) {  // swap3
            Board sourceBoard1 = getBoard(move.boardNames[0]);
            Board sourceBoard2 = getBoard(move.boardNames[1]);
            Board sourceBoard3 = getBoard(move.boardNames[2]);
            Board destinationBoard1 = getBoard(move.boardNames[3]);
            Board destinationBoard2 = getBoard(move.boardNames[4]);
            Board destinationBoard3 = getBoard(move.boardNames[5]);
            String squareName = move.squareNames[0];
            Piece piece1 = sourceBoard1.popPiece(squareName);
            Piece piece2 = sourceBoard2.popPiece(squareName);
            Piece piece3 = sourceBoard3.popPiece(squareName);
            destinationBoard1.setPiece(squareName, piece1);
            destinationBoard2.setPiece(squareName, piece2);
            destinationBoard3.setPiece(squareName, piece3);

        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // capture
            Board sourceBoard = getBoard(move.boardNames[0]);
            Board destinationBoard = getBoard(move.boardNames[1]);
            Piece sourcePiece = sourceBoard.popPiece(move.squareNames[0]);
            Piece destinationPiece = sourceBoard.popPiece(move.squareNames[1]);
            destinationBoard.setPiece(move.squareNames[1], sourcePiece);
            getPrison(turnPlayer).addPiece(destinationPiece.value);
            if (move.pieceNames[0] == 'P' &&
                    Board.getCoordinates(move.squareNames[1])[0] % 7 == 0) {
                promote(sourceBoard.getSquare(move.squareNames[1]),
                        Value.getValueFromName(move.promotion));
            }

        }else if (move.pieceNames.length == 2 &&
                move.boardNames.length == 3 &&
                move.squareNames.length == 2) {  // steal
            Board destinationPlayer = getBoard(move.boardNames[1]);
            Board destinationOpponent = getBoard(move.boardNames[2]);
            Piece sourcePiece = gamma.popPiece(move.squareNames[0]);
            Piece destinationPiece = gamma.popPiece(move.squareNames[1]);
            destinationPlayer.setPiece(move.squareNames[1], sourcePiece);
            destinationOpponent.setPiece(move.squareNames[1], destinationPiece);

        }else if (move.pieceNames.length == 0 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // en passant
            int pawnRow = 3;
            if (turnPlayer == Color.BLACK) pawnRow = 4;
            int destinationColumn = Board.getCoordinates(move.squareNames[1])[1];
            Board sourceBoard = getBoard(move.boardNames[0]);
            Board destinationBoard = getBoard(move.boardNames[1]);
            Piece sourcePiece = sourceBoard.popPiece(move.squareNames[0]);
            sourceBoard.removePiece(pawnRow, destinationColumn);  // capture
            destinationBoard.setPiece(move.squareNames[1], sourcePiece);
            getPrison(turnPlayer).addPiece(Value.PAWN);

        }else if (move.pieceNames.length == 1 &&
                move.boardNames.length == 2 &&
                move.squareNames.length == 2) {  // translate
            Board sourceBoard = getBoard(move.boardNames[0]);
            Board destinationBoard = getBoard(move.boardNames[1]);
            Piece sourcePiece = sourceBoard.popPiece(move.squareNames[0]);
            destinationBoard.setPiece(move.squareNames[1], sourcePiece);
            if (move.pieceNames[0] == 'P' &&
                    Board.getCoordinates(move.squareNames[1])[0] % 7 == 0){
                promote(sourceBoard.getSquare(move.squareNames[1]),
                        Value.getValueFromName(move.promotion));
            }
        }
        addMove(move);
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
        moves.subList(turn, moves.size()).clear();
    }

    private void addMove(Move move){
        String moveString = encodeMove(move);
        removeMovesAfter(currentPly);
        moves.add(moveString);
        incrementPly();
    }

    private void rewindMove(String moveString){
        Move move = decodeMove(moveString);
        decrementPly();
        removeMovesAfter(currentPly);
        undoMove(move);
    }

    private boolean incrementPly(){
        if (currentPly < moves.size()){
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

    public String[] getMoves(){
        return moves.toArray(new String[moves.size()]);
    }

    public String encodeMove(Move move) throws IllegalArgumentException{
        if (move == null || MoveType.of(move) == null){
            throw new IllegalArgumentException("Invalid Move");
        }

        String moveString = "";
        String promotionString = "";
        if (move.promotion!=null) promotionString = "=" + promotionString;
        String stateString = "";
        if (move.promotion!=null) stateString = move.state.toString();

        switch (MoveType.of(move)) {
            case CASTLE:
                moveString = "O-O";
                if (move.pieceNames[0] == 'Q') moveString = "O-O-O";
                break;
            case DROP:
                moveString = String.format(">%s%s",
                        move.pieceNames[0],
                        move.squareNames[0]);
                break;
            case HOSTAGE_EXCHANGE:
                moveString = String.format("%s>%s%s",
                        move.pieceNames[0],
                        move.pieceNames[1],
                        move.squareNames[0]);
                break;
            case SWAP2:
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
                break;
            case SWAP3:
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
            case PROMOTION:
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
            squareNames[0] = moveString.substring(1, 3);
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
    }

    public boolean validMove(Move move){
        if (move == null || MoveType.of(move) == null){
            return false;
        }
        return true;
/*        Color turnPlayer = getCurrentPlayer();
        switch (MoveType.of(move)){
            case PROMOTION: return true;
        }
        return true;*/
    }

    private static boolean validCastle(){
        return true;
    }

    public Move[] validSwaps(Square sourceSquare){
        if (sourceSquare == null) return new Move[0];
        Board sourceBoard = sourceSquare.board;
        if (sourceBoard == null) return new Move[0];
        Piece sourcePiece = sourceBoard.getPiece(sourceSquare);
        if (sourcePiece == null) return new Move[0];
        String squareName = Board.getSquareName(sourceSquare);
        ArrayList<Character> boardNames = new ArrayList<>(3);
        ArrayList<Move> validSwaps = new ArrayList<>(8);
        if (alpha.getPiece(squareName) != null) boardNames.add('A');
        if (beta.getPiece(squareName) != null) boardNames.add('B');
        if (gamma.getPiece(squareName) != null) boardNames.add('C');
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
                        Board board0 = getBoard(boardName0);
                        Board board1 = getBoard(boardName1);
                        Board board2 = getBoard(boardName2);
                        validSwaps.add(new Move(
                                getCurrentPlayer(), null, null,
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
                    Board board0 = getBoard(boardName0);
                    Board board1 = getBoard(boardName1);
                    validSwaps.add(new Move(
                            getCurrentPlayer(), null, null,
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
                                Board.getSquareName(square)});
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
        moves.addAll(Arrays.asList(validSwaps(sourceSquare)));
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
        Pair destination = move.getDestinationBoardsAndSquareNames();
        Board destinationBoard = destinationSquare.board;
        Character[] destinationBoardNames = (Character[]) destination.getKey();
        String destinationSquareName = (String) destination.getValue();
        if (destinationSquareName.equals(Board.getSquareName(destinationSquare))){
            return false;
        }
        boolean found = false;
        for (Character boardName :
                destinationBoardNames) {
            if (boardName == destinationBoard.name){
                found = true;
                break;
            }
        }
        return found;
    }
}
