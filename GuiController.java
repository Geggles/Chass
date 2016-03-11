import Game.*;
import com.trolltech.qt.QSignalEmitter;
import GUI.Signals;
import com.trolltech.qt.gui.*;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;

public class GuiController extends QSignalEmitter implements Controller{
    /*Emit when file->save is invoked*/
    private Signals signals = new Signals();
    private Game.Controller gameController = new Game.Controller();
    private QMainWindow mainWin;
    private QTextEdit editField;
    public GuiController(){ String[] args = new String[0];
        QApplication app = new QApplication(args);
        setupGui(); signals.saveGame.connect(this, "saveGame()");
        signals.exitApplication.connect(app, "exit()"); app.exec(); }
    private void setupGui(){
        mainWin = new QMainWindow();
        QWidget centralWidget = new QWidget(mainWin);
        mainWin.setCentralWidget(centralWidget);

        setupMenuBar();
        //------------------------------------------------------------------------------------------
        // provisorisch

        printBoards(false);

        QVBoxLayout layout = new QVBoxLayout(centralWidget);
        centralWidget.setLayout(layout);

        editField = new QTextEdit(centralWidget);
        QPushButton submitButton1 = new QPushButton("submit single move", centralWidget);
        QPushButton submitButton2 = new QPushButton("submit multiple moves at once", centralWidget);

        submitButton1.clicked.connect(this, "submitMove()");
        submitButton2.clicked.connect(this, "submitMoves()");

        centralWidget.layout().addWidget(editField);
        centralWidget.layout().addWidget(submitButton1);
        centralWidget.layout().addWidget(submitButton2);

        //------------------------------------------------------------------------------------------

        mainWin.show();
    }

    private void submitMove(){
        String text = editField.toPlainText();
        System.out.println(text);
        Move move = gameController.decodeMove(text);
        editField.clear();
        gameController.doMove(move);
        printBoards(false);
    }

    private void submitMoves(){
        String[] lines = editField.toPlainText().split("\\r?\\n");
        // aus Persisitence kopiert, provisorisch
        Arrays.stream(lines).forEachOrdered(line -> {
            String[] parts = line.split(",");
            Arrays.stream(parts).forEach(move ->
                    //System.out.println(move)
                    gameController.doMove(gameController.decodeMove(move))
            );
        });
        editField.clear();
        printBoards(false);
    }

    private void setupMenuBar(){
        QMenuBar menuBar = new QMenuBar(mainWin);
        QMenu fileMenu = new QMenu("File");
        menuBar.addMenu(fileMenu);
        fileMenu.addAction("Save", signals.saveGame);
        fileMenu.addAction("Exit", signals.exitApplication);
        mainWin.setMenuBar(menuBar);
    }
    @Override
    public void startGame(){
    }

    public void saveGame(){
        Persistence.saveMoves(gameController.getMoves());
    }

    //----------------------------------------------------------------------------------------------
    //##############################################################################################
    // Von ConsoleController kopiert, provisorisch
    //##############################################################################################
    //----------------------------------------------------------------------------------------------
    private static final EnumMap<Color, String> squares = new EnumMap<>(Color.class);
    static {
        squares.put(Color.WHITE, "\u25FB");
        squares.put(Color.BLACK, "\u25FC");
        squares.put(Color.NONE, "\u0020");  // normal space
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
    //----------------------------------------------------------------------------------------------
    //##############################################################################################
    // /Von ConsoleController kopiert, provisorisch
    //##############################################################################################
    //----------------------------------------------------------------------------------------------
}
