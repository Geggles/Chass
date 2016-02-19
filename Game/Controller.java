package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * this class will control the flow of the game
 * */

public class Controller {
    private StandardBoard alpha;
    private StandardBoard beta;
    private SpecialBoard gamma;
    private HashMap<Color, PieceCollection> prisons;
    private HashMap<Color, PieceCollection> airfields;
    private Color turnPlayer = Color.WHITE;
    private LinkedList<String> moves;
    private int currentPly = 0;

    public Controller() {
        alpha = new StandardBoard(Color.WHITE);
        beta = new StandardBoard(Color.BLACK);
        gamma = new SpecialBoard();
        prisons = new HashMap<>(2);
        prisons.put(Color.WHITE, new PieceCollection());
        prisons.put(Color.BLACK, new PieceCollection());
        airfields = new HashMap<>(2);
        airfields.put(Color.WHITE, new PieceCollection());
        airfields.put(Color.BLACK, new PieceCollection());
    }

    public void newGame(){
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
        Square kingSquare = board.getSquare(board.getPieces(Value.KING)[0]);
        return board.isUnderAttack(kingSquare, player.opposite());
    }

    /**
     * Return side that player can castle to (King/Queen/None)
     * */
    public Value[] canCastleTo(Color player) {
        ArrayList<Value> result = new ArrayList<>(0);
        Board board = getBoard(player);
        Character boardName = getBoardName(board);
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

    public void undoMove(Move move){
        if (move.pieceNames.length == 1 &&
                move.boardNames.length == 0 &&
                move.squareNames.length == 0){  // castle:
            StandardBoard board = (StandardBoard) getBoard(turnPlayer);
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
            StandardBoard board = (StandardBoard) getBoard(turnPlayer);
            int row = 0;
            if (turnPlayer == Color.BLACK) row = 7;
            Square oldRookSquare = board.getSquare(row, 0);
            Square oldKingSquare = board.getSquare(row, 5);
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
    }

    private void promote(Square square, Value value){
        StandardBoard board = (StandardBoard) square.board;
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
        moves.subList(moves.size() - turn, moves.size()).clear();
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

    public String[] getMoves(){
        return moves.toArray(new String[moves.size()]);
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
