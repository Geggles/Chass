package Game;

import java.util.HashMap;

/**
 * this class will control the flow of the game
 * */
public class Controller {
    StandardBoard alpha;
    StandardBoard beta;
    SpecialBoard gamma;
    HashMap<Color, Prison> prisons;
    HashMap<Color, Airfield> airfields;
    Color turnPlayer = Color.WHITE;
    Notator notator;

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
        notator = new Notator();
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

    //belongs into gui/console controllers
/*    *//**
     * Check all move types to see whether under given parameters a move can be made.
     * Return Move object that contains the parameters and move type.
     * Return null if no move is possible.
     * *//*

    public boolean validMove(Square source, Square destination){
        PlayBoard board = (PlayBoard) source.board;
        Piece piece = board.getPiece(source);
        if (piece == null) return false;
        if (source == destination && !piece.canNullMove) return false;
        if (board.getPiece(destination) != null) return false;
        try { // for alpha and beta
            StandardBoard standardBoard = (StandardBoard)board;
            if (piece.value == Value.KING &&
                    (standardBoard.isUnderAttack(destination, Color.oppositeColor(piece.color)))) {
                return false;
            }
            return true;
        }catch (ClassCastException exception){
            return true;
        }
    }

    public boolean validCapture(Square source, Square destination){
        StandardBoard board = (StandardBoard) source.board;
        Piece sourcePiece = board.getPiece(source);
        Piece destinationPiece = board.getPiece(destination);
        if (sourcePiece == null) return false;
        if (destinationPiece == null) return false;
        if (destinationPiece.color == sourcePiece.color) return false;

        return true;
    }

    public boolean validCastle(Color color, Value side){
        Piece king;
        Piece rook;
        PlayBoard board;
        Square[] squaresInBetween;
        if (color == Color.WHITE){
            board = alpha;
            king = board.getPiece(new int[] {0, 4});
            if (side == Value.QUEEN){
                rook = board.getPiece(new int[] {0, 0});
                squaresInBetween = new Square[] {
                        board.getSquare(new int[] {0, 1}),
                        board.getSquare(new int[] {0, 2}),
                        board.getSquare(new int[] {0, 3})
                };
            }else {
                rook = board.getPiece(new int[] {0, 7});
                squaresInBetween = new Square[] {
                        board.getSquare(new int[] {0, 5}),
                        board.getSquare(new int[] {0, 6})
                };
            }
        }else {
            board = beta;
            king = board.getPiece(new int[]{7, 4});
            if (side == Value.QUEEN) {
                rook = board.getPiece(new int[]{7, 0});
                squaresInBetween = new Square[] {
                        board.getSquare(new int[] {0, 1}),
                        board.getSquare(new int[] {0, 2}),
                        board.getSquare(new int[] {0, 3})
                };
            }else {
                rook = board.getPiece(new int[]{7, 7});
                squaresInBetween = new Square[]{
                        board.getSquare(new int[]{0, 5}),
                        board.getSquare(new int[]{0, 6}),
                };
            }
        }
        if (king==null || rook==null) return false;
        if (king.value!=Value.KING || rook.value!=Value.ROOK) return false;
        if (!king.canCastle || !rook.canCastle) return false;
        for (Square square :
                squaresInBetween) {
            if (board.isUnderAttack(square, Color.oppositeColor(color))) return false;
        }
        return true;
    }

    public boolean validStealing(Square source,
                                 Square destination,
                                 PlayBoard destinationBoardPlayer,
                                 PlayBoard destinationBoardOpponent){
        if (source.board != gamma) return false;
        if (destination.board != gamma) return false;
        Piece sourcePiece = gamma.getPiece(source);
        if (sourcePiece == null) return false;
        Piece destinationPiece = gamma.getPiece(destination);
        if (destinationPiece == null) return false;
        if (destinationBoardOpponent.getPiece(gamma.getCoordinates(destination)) != null){
            return false;
        }
        StandardBoard otherBoard = alpha;
        if (destinationBoardOpponent==alpha) otherBoard = beta;
        if (otherBoard.getPiece(gamma.getCoordinates(destination)) == null &&
            destinationBoardPlayer != otherBoard){
            return false;
        }

        return true;
    }

    public boolean validEnPassant(Square source,
                                  Square destination){
        StandardBoard board = (StandardBoard) source.board;
        if (board.getPiece(source) == null) return false;
        int sourceRow = board.getCoordinates(source)[0];
        int sourceColumn = board.getCoordinates(source)[1];

        int column = board.getCoordinates(destination)[1];
        int direction = 1;
        int rightStartRow = 6;
        if (turnPlayer == Color.WHITE){
            direction = -1;
            rightStartRow = 1;
        }

        Move latestMove = decodeMove(notator.currentTurn());
        if (latestMove.sourcePiece.value != Value.PAWN) return false;
        if (latestMove.source.board != board) return false;
        if (board.getCoordinates(latestMove.source)[0] != rightStartRow) return false;
        if (board.getCoordinates(latestMove.source)[1] != column) return false;
        if (board.getCoordinates(latestMove.destination)[0] != rightStartRow+2*direction){
            return false;
        }
        if (sourceRow != rightStartRow+2*direction) return false;
        if (Math.abs(column - sourceColumn) != 1) return false;

        return true;
    }

    public boolean validSwap(){

    }*/

    public void castle(Color color, Value side){
        Piece king;
        Piece rook;
        if (color == Color.WHITE){
            king = alpha.getPiece(new int[] {0, 4});
            alpha.removePiece(king);
            if (side == Value.QUEEN){
                rook = alpha.getPiece(new int[] {0, 0});
                alpha.removePiece(rook);
                alpha.setPiece(new int[] {0, 2}, king);
                alpha.setPiece(new int[] {0, 4}, rook);
            }else {
            rook = alpha.getPiece(new int[] {0, 7});
            alpha.removePiece(rook);
            alpha.setPiece(new int[] {0, 6}, king);
            alpha.setPiece(new int[] {0, 5}, rook);
            }

        }else {
            king = beta.getPiece(new int[]{7, 4});
            beta.removePiece(king);
            if (side == Value.QUEEN) {
                rook = beta.getPiece(new int[]{7, 0});
                beta.removePiece(rook);
                beta.setPiece(new int[]{7, 2}, king);
                beta.setPiece(new int[]{7, 4}, rook);
            }else {
                rook = beta.getPiece(new int[]{7, 7});
                beta.removePiece(rook);
                beta.setPiece(new int[]{7, 6}, king);
                beta.setPiece(new int[]{7, 5}, rook);
            }
        }
        king.canCastle = false;
        rook.canCastle = false;
    }

    public Move decodeMove(){
        String encoded = notator.getMove();
        /*
        * Check in order:
        * Castling
        * Drop
        * Hostage Exchange
        * Swap
        * Capture
        * Steal
        * En Passant
        * Translate
        * */

        State state = State.NONE;
        Move move;
        PlayBoard board;
        Color player = Color.WHITE;
        if (notator.currentTurn() % 2 == 0) player = Color.BLACK;
        if (encoded.endsWith("+")){
            state = State.CHECK;
            encoded = encoded.substring(0, encoded.length()-1);
        }
        else if (encoded.endsWith("#")){
            state = State.CHECKMATE;
            encoded = encoded.substring(0, encoded.length()-1);
        }

        if (encoded.startsWith("O-O")){  // Check for Castling
            Value side = Value.KING;
            if (encoded.length()>4) side = Value.QUEEN; // O-O-O
            board = alpha;
            if (player == Color.BLACK) board = beta;
            return new Castling(
                    player,
                    state,
                    side);
        }
        if(encoded.charAt(0) == '>'){  // Check for drop from airfield
            return new Drop(
                    player,
                    state,
                    SpecialBoard.getCoordinates(encoded.substring(2, 4)));
        }
        if (encoded.charAt(1) == '>') {  // Check for Hostage Exchange
            return new HostageExchange(
                    player,
                    state,
                    Value.getValueFromName(encoded.charAt(0)),
                    SpecialBoard.getCoordinates(encoded.substring(3, 5)));
        }
        if(encoded.charAt(3)=='-' &&
                "abcdefgh".indexOf(encoded.charAt(5)) == -1) {  // check for swap
            PlayBoard[] constellationBefore = new PlayBoard[3];
            PlayBoard[] constellationAfter = new PlayBoard[3];

            for (int i = 0; i < 3; i++) {
                constellationBefore[i] = getBoard(encoded.charAt(8 + i));
                constellationAfter[i] = getBoard(encoded.charAt(12 + i));
            }
            return new Swap(
                    player,
                    state,
                    PlayBoard.getCoordinates(encoded.substring(1, 3)),
                    constellationBefore,
                    constellationAfter
            );
        }
        int startOfConstellation = encoded.indexOf('_') + 1;
        board = getBoard(encoded.charAt(startOfConstellation));
        if(encoded.charAt(3) == 'x'){  // check for captures
            if (encoded.length() == startOfConstellation+3){  //check for normal capture
                return new Capture(
                        player,
                        state,
                        board.getSquare(encoded.substring(1, 3)),
                        board.getSquare(encoded.substring(4, 6)),
                        getBoard(encoded.charAt(startOfConstellation+2))
                );
            }
            if (encoded.length() == startOfConstellation+5){  // check for steal
                return new Steal(
                        player,
                        state,
                        PlayBoard.getCoordinates(encoded.substring(1, 3)),
                        PlayBoard.getCoordinates(encoded.substring(5, 7)),
                        getBoard(encoded.charAt(startOfConstellation+2)),
                        getBoard(encoded.charAt(startOfConstellation+4))
                );
            }
        }
        if (encoded.charAt(3) == 'p') {  // check for en passant
            return new EnPassant(
                    player,
                    state,
                    (StandardBoard) board,
                    (StandardBoard) getBoard(encoded.charAt(startOfConstellation+2)),
                    "abcdefgh".indexOf(encoded.charAt(4))
            );
        }
        if (encoded.charAt(3) == '-'){
            return new Translate(
                    player,
                    state,
                    board.getSquare(encoded.substring(1, 3)),
                    board.getSquare(encoded.substring(4, 6)),
                    getBoard(encoded.charAt(startOfConstellation+2))
            );
        }
        return null;
    }
}
