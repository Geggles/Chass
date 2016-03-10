import Game.*;

import java.io.IOException;
import java.util.*;

public class ConsoleController implements Controller {
    Game.Controller gameController = new Game.Controller();
    boolean unicode;
    private static final EnumMap<Color, String> squares = new EnumMap<>(Color.class);
    static {
        squares.put(Color.WHITE, "\u25FB");
        squares.put(Color.BLACK, "\u25FC");
        squares.put(Color.NONE, "\u0020");  // normal space
    }
    private static final EnumMap<Value, EnumMap<Color, Character>>
            symbols = new EnumMap<>(Game.Value.class);
    //private static final String
    static {
        //White Black
        char[][] s = new char[][]{
                new char[]{'\u2659', '\u265F'}, //Pawn
                new char[]{'\u2657', '\u265D'}, //Bishop
                new char[]{'\u2658', '\u265E'}, //Knight
                new char[]{'\u2656', '\u265C'}, //Rook
                new char[]{'\u2655', '\u265B'}, //Queen
                new char[]{'\u2654', '\u265A'} //King
        };
        EnumMap<Color, Character> putInto;
        int i = 0;
        for (Value value:
             Value.values()) {
            putInto = new EnumMap<>(Color.class);
            putInto.put(Color.WHITE, s[i][0]);
            putInto.put(Color.BLACK, s[i][1]);
            symbols.put(value, putInto);
            i ++;
        }
    }

    public ConsoleController() {
        new ConsoleController(true);
    }
    public ConsoleController(boolean unicode) {
        this.unicode = unicode;
    }
    public void startGame() {
        //doGameLoop();
        //-----------------------------------------------------------------------------------
        // provisorisch


        Scanner input = new Scanner(System.in);
        while (gameController.getCurrentPly()==0 ||
                gameController.getLatestMove().state == null) {
            printBoards();
            System.out.println("Enter move String:");
            String in = input.nextLine();
            if (in.equals("save")){
                Persistence.saveMoves(gameController.getMoves());
                continue;
            }
            if (in.startsWith("load ")){
                String[] moves = Persistence.loadMoves(in.substring(5));
                Arrays.stream(moves).forEach(
                        s -> gameController.doMove(gameController.decodeMove(s)));
                //s -> System.out.println(s));
                continue;
            }
            gameController.doMove(gameController.decodeMove(in));
        }
        //-----------------------------------------------------------------------------------
    }

    private void doGameLoop(){
        final String operatingSystem = System.getProperty("os.name");

        try {
            if (operatingSystem .contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            }
            else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (IOException e){
            for(int clear = 0; clear < 1000; clear++) {
                System.out.println("\b") ;
            }
        }

        boolean valid;
        Move move = null;
        Color turnPlayer;
        Scanner input = new Scanner(System.in);
        gameLoop:
        while (gameController.getCurrentPly()==0 || gameController.getLatestMove().state == null) {
            valid = false;
            printBoards();
            System.out.println("Input <exit> to exit the application");
            System.out.println("Input <Undo> or <U> to undo a move");
            System.out.println("Input <Redo> or <R> to redo an undone move");
            System.out.println("Input <Move> or <M> to move a piece");
            System.out.println("Input <Exchange> or <E> to exchange a hostage");
            System.out.println("Input <Drop> or <D> to drop a piece from your airfield");
            String inString = input.nextLine();

            if (inString.equals("exit")) break gameLoop;
            switch (inString){
                case "Move":
                case "M":
                    printBoards();
                    System.out.println("Input <exit> to exit the application");
                    System.out.println("Input <cancel> to cancel this move");
                    System.out.println("Move piece from board: <A> or <B> or <C>");
                    inString = input.nextLine();
                    if (inString.equals("exit")) break gameLoop;
                    if (inString.equals("cancel")) break;
                    Board sourceBoard = gameController.getBoard(inString.charAt(0));
                    if (sourceBoard.getPieces().length == 0) break;

                    printBoards();
                    System.out.println("Input <exit> to exit the application");
                    System.out.println("Input <cancel> to cancel this move");
                    System.out.println("Input the name of the square " +
                            "you want to move from, e.g. <e4>");
                    inString = input.nextLine();
                    if (inString.equals("exit")) break gameLoop;
                    if (inString.equals("cancel")) {
                        System.out.println("That board is empty");
                        break;
                    }
                    Square sourceSquare = sourceBoard.getSquare(inString);
                    Piece sourcePiece = sourceBoard.getPiece(sourceSquare);
                    if (sourcePiece == null) {
                        System.out.println("There is no piece on that square");
                        break;
                    }
                    if (sourceBoard.canGoTo(sourcePiece).length == 0){
                        System.out.println("That piece cannot move");
                        break;
                    }

                    printBoards();
                    System.out.println("Input <exit> to exit the application");
                    System.out.println("Input <cancel> to cancel this move");
                    System.out.println("Input the name of the square " +
                            "you want to move to e.g. <a6>");
                    inString = input.nextLine();
                    if (inString.equals("exit")) break gameLoop;
                    if (inString.equals("cancel")) break;
                    Square destinationSquare = sourceBoard.getSquare(inString);
                    if (sourceBoard.getPiece(destinationSquare) != null &&
                            sourceBoard.getPiece(destinationSquare).getColor() ==
                            gameController.getCurrentPlayer()){
                        System.out.println("That Square is occupied");
                        break;
                    }

                    boolean contained = false;
                    for (Square square :
                            sourceBoard.canGoTo(sourcePiece)) {
                        if (square == destinationSquare) {
                            contained = true;
                            break;
                        }
                    }
                    if (!contained){
                        System.out.println("Can't move there");
                        break;
                    }

                    //-------------------
                    // find out move type
                    //-------------------

                    // Castle
                    List<Integer> sourceCoordinates =
                            sourceBoard.getCoordinates(sourceSquare);
                    int sourceRow = sourceCoordinates.get(0);
                    int sourceColumn = sourceCoordinates.get(1);
                    List<Integer> destinationCoordinates =
                            sourceBoard.getCoordinates(destinationSquare);
                    int destinationRow = destinationCoordinates.get(0);
                    int destinationColumn = destinationCoordinates.get(1);
                    int baseRow = 0;
                    if (sourceBoard.color == Color.BLACK) baseRow = 7;
                    if (sourcePiece.value == Value.KING &&
                            sourceRow == baseRow &&
                            sourceColumn == 4 &&
                            destinationRow == baseRow &&
                            Math.abs(sourceColumn-destinationColumn) == 2){
                        Value castleDirection = Value.QUEEN;
                        if (destinationColumn == 6) castleDirection = Value.KING;
                        contained = false;
                        for (Value dir: gameController.canCastleTo()) {
                            if (dir == castleDirection) {
                                contained = true;
                                break;
                            }
                        }
                        if (!contained) {
                            System.out.println("Can't castle (into that direction)");
                        }
                        move = new Move(
                                null,
                                null,
                                new Character[]{castleDirection.name},
                                new Character[]{},
                                new String[]{}
                        );
                    }

                    //en passant
                    int direction = 1;
                    Move latestMove = gameController.getLatestMove();
                    if (sourcePiece.getColor() == Color.BLACK) direction = -1;
                    if (sourcePiece.value == Value.PAWN &&
                            destinationRow == 7 - baseRow - 2*direction &&
                            latestMove.pieceNames[0] == 'P' //&&
                            )

                    // promote
                    if (sourcePiece.value == Value.PAWN &&
                            destinationRow == 7-baseRow &&
                            sourceBoard.color != Color.NONE
                            )

                    if (gameController.inCheck()){
                        move = new Move(
                                null,
                                null,
                                new Character[]{},
                                new Character[]{},
                                new String[]{}
                                );
                    }
                    // check for pinned / king moving into check
                    break;
                case "Exchange":
                case "E":
                    break;
                case "Drop":
                case "D":
                    break;
            }
            if (valid){
                gameController.doMove(move);
            }
        }
    }

    private void printBoards(boolean printCoords){
        Value[] whitePrison = gameController.getPrison(Color.WHITE).getPieces();
        Value[] blackPrison = gameController.getPrison(Color.BLACK).getPieces();
        Value[] whiteAirfield = gameController.getAirfield(Color.WHITE).getPieces();
        Value[] blackAirfield = gameController.getAirfield(Color.BLACK).getPieces();
        Board alpha = gameController.getBoard(Color.WHITE);
        Board beta = gameController.getBoard(Color.BLACK);
        Board gamma = gameController.getBoard(Color.NONE);

        LinkedList<String> lines = new LinkedList<>();
        String line;
        int row;
        int column;
        for (row=0; row<18; row++){
            line = "";
            for (column=0; column<18; column++){
                if (row <= 3) {  // upper boards
                    if (column <= 3) {  // white Prison
                        if (row * 4 + column < whitePrison.length) {
                            line += whitePrison[row * 4 + column];
                        } else {
                            line += squares.get(getSquareColor(row, column));
                        }
                    } else if (column == 4 || column == 13) {  // margin
                        line += squares.get(Color.NONE);
                    } else if (column <= 12) {  // gamma
                        line += getPiece(gamma, row, column-5);
                    } else {  // black Prison
                        if (row * 4 + column < blackPrison.length) {
                            line += blackPrison[row * 4 + column];
                        } else {
                            line += squares.get(getSquareColor(row, column-14));
                        }
                    }
                }else if (row == 4){  // line between Prisons and Airfields
                    if (column <= 4 || column >= 13){  // margin
                        line += squares.get(Color.NONE);
                    }else{  // gamma
                        line += getPiece(gamma, row, column-5);
                    }
                }else if (row <= 7){  // until lower border of gamma
                    if (column <= 3) {  // white Airfield
                        if ((row-5) * 4 + column < whiteAirfield.length) {
                            line += whiteAirfield[(row-5) * 4 + column];
                        } else {
                            line += squares.get(getSquareColor(row-5, column));
                        }
                    } else if (column == 4 || column == 13) {  // margin
                        line += squares.get(Color.NONE);
                    } else if (column <= 12) {  // gamma
                        line += getPiece(gamma, row-5, column-5);
                    } else {  // black Airfield
                        if ((row-5) * 4 + column < blackAirfield.length) {
                            line += blackAirfield[(row-5) * 4 + column];
                        } else {
                            line += squares.get(getSquareColor(row-5, column-14));
                        }
                    }
                }else if (row == 8){  // last bit of Prisons and Airfields
                    if (column <= 3) {  // white Airfield
                        if ((row-6) * 4 + column < whiteAirfield.length) {
                            line += whiteAirfield[(row-6) * 4 + column];
                        } else {
                            line += squares.get(getSquareColor(row-6, column));
                        }
                    } else if (column <= 13) {  // margin
                        line += squares.get(Color.NONE);
                    } else {  // black Airfield
                        if ((row-6) * 4 + column < blackAirfield.length) {
                            line += blackAirfield[(row-6) * 4 + column];
                        } else {
                            line += squares.get(getSquareColor(row-6, column-14));
                        }
                    }
                }else if (row == 9){  // margin
                    line += squares.get(Color.NONE);
                }else{
                    if (column <= 7){  // alpha
                        line += getPiece(alpha, row-10, column);
                    } else if (column <= 9){  // margin
                        line += squares.get(Color.NONE);
                    } else{  // beta
                        line += getPiece(beta, row-10, column-10);
                    }
                }
            }
            lines.add(line);
        }
        lines.stream().forEachOrdered(str -> System.out.println(str));
    }

    private Color getSquareColor(int row, int column){
        int res = row%2 ^ column%2;
        if (res == 0){
            return Color.BLACK;
        }
        return Color.WHITE;
    }

    private Character getPiece(Board board, int row, int column){
        Piece result = board.getPiece(7-row, column);
        if (result == null){
            return squares.get(getSquareColor(7-row, column)).charAt(0);
        }
        if (board.color==Color.BLACK){
        }
        return symbols.get(result.value).get(result.getColor());
    }

    private void printBoards(){
        printBoards(false);
    }
}