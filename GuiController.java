import GUI.*;
import Game.Move;
import Game.MoveType;
import Game.Persistence;
import Shared.Value;
import com.trolltech.qt.QSignalEmitter;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuiController extends QSignalEmitter implements Controller{
    private final Signals signals = Signals.getInstance();
    private final SettingsManager settings = SettingsManager.getInstance();
    private final Game.Controller gameController = new Game.Controller();
    private final MainWindow mainWin;
    private final CentralWidget centralWidget;

    private Move[] validMoves = new Move[0];

    public GuiController(){
        mainWin = new MainWindow();
        signals.mainWinClosed.connect(this, "onMainWinClosed()");
        signals.squareSelected.connect(this, "onSquareSelected(Square)");
        signals.destinationSelected.connect(this, "onDestinationSelected(Square)");
        centralWidget = new CentralWidget(mainWin, "centralWidget");
        mainWin.setCentralWidget(centralWidget);

        setupGui();
        if (settings.allKeys().size()==0) resetSettings();
        else settings.loadIntoBuffer();

/*        System.out.println(QColor.fromRgb(17, 123, 223));
        System.out.println(QColor.fromRgb(17, 123, 223).toHsl());*/
    }

    private void onDestinationSelected(Square destinationSquare){
        Game.Square destinationGameSquare = getGameSquare(destinationSquare);
        Move[] validMoves = gameController.getValidMoves(this.validMoves, destinationGameSquare);
        switch (validMoves.length){
            case 0: cancelMove(); break;
            case 1: doMove(validMoves[0]); break;
            default: decideMove(validMoves); break;
        }
    }

    private void cancelMove(){

    }

    // Called when multiple moves are possible. Resolve choice by asking user.
    private void decideMove(Move[] options){

    }

    private void doMove(Move move){
        gameController.doMove(move);
        Pair<Character[], String> source = move.getSourceBoardsAndSquareNames();
        Pair<Character[], String> destination = move.getDestinationBoardsAndSquareNames();

        switch (MoveType.of(move)){
            case SWAP2:
                Board sourceBoard = centralWidget.getBoard(source.getKey()[0]);
                Board destinationBoard = centralWidget.getBoard(destination.getKey()[0]);
                int[] squareCoordinates = Game.Board.getCoordinates(source.getValue());
                int row = squareCoordinates[0];
                int column = squareCoordinates[1];
                Piece piece0 = sourceBoard.getPiece(row, column);
                Piece piece1 = destinationBoard.getPiece(row, column);
                ((QGridLayout) sourceBoard.layout()).addWidget(piece1, row, column, 1, 1);
                ((QGridLayout) destinationBoard.layout()).addWidget(piece0, row, column, 1, 1);
                break;
            case SWAP3:
                squareCoordinates = Game.Board.getCoordinates(source.getValue());
                row = squareCoordinates[0];
                column = squareCoordinates[1];
                Board sourceBoard0 = centralWidget.getBoard(source.getKey()[0]);
                Board sourceBoard1 = centralWidget.getBoard(source.getKey()[1]);
                Board sourceBoard2 = centralWidget.getBoard(source.getKey()[2]);
                Board destinationBoard0 = centralWidget.getBoard(destination.getKey()[0]);
                Board destinationBoard1 = centralWidget.getBoard(destination.getKey()[1]);
                Board destinationBoard2 = centralWidget.getBoard(destination.getKey()[2]);
                piece0 = sourceBoard0.getPiece(row, column);
                piece1 = sourceBoard1.getPiece(row, column);
                Piece piece2 = sourceBoard2.getPiece(row, column);
                ((QGridLayout) destinationBoard0.layout()).addWidget(piece0, row, column, 1, 1);
                ((QGridLayout) destinationBoard1.layout()).addWidget(piece1, row, column, 1, 1);
                ((QGridLayout) destinationBoard2.layout()).addWidget(piece2, row, column, 1, 1);
        }
    }

/*    private void updateGuiToGame() {
        Character[] boardNames = new Character[]{'A', 'B', 'C'};
        for (Character boardName: boardNames){
            updateBigBoard(boardName);
        }
    }*/

/*    private void updateBigBoard(Character boardName){
        Board guiBoard = centralWidget.getBoard(boardName);
        Game.Board gameBoard = gameController.getBoard(boardName);
        ArrayList<Piece> guiPiecesLeft = new ArrayList<>();
        HashMap<Square, Value> guiPiecesMissing = new HashMap<>();
        Piece guiPiece;
        Game.Piece gamePiece;

        for (int row=0; row<8; row++){
            for (int column=0; column<8; column++){
                guiPiece = guiBoard.getPiece(row, column);
                gamePiece = gameBoard.getPiece(row, column);
                if (guiPiece != null && gamePiece == null)guiPiecesLeft.add(guiPiece);
                else if (guiPiece == null && gamePiece != null) {
                        guiPiecesMissing.put(guiBoard.squares[row][column], gamePiece.value);
                }
            }
        }
        int index;
        for (Map.Entry<Square, Value> entry: guiPiecesMissing.entrySet()){
            guiPiece = null;
            for (Piece piece: guiPiecesLeft){
                if (piece.getValue() == entry.getValue()){
                    guiPiece = piece;
                    break;
                }
            }
            if (guiPiece == null) throw IllegalStateException("Game board ")
            index = guiPiecesLeft.indexOf()
            guiPiece = guiPiecesLeft.remove()
        }
    }*/


    private Game.Square getGameSquare(Square square){
        Board board = (Board) square.parentWidget();
        Game.Board gameBoard = gameController.getBoard(board.name);
        return gameBoard.getSquare(square.row, square.column);
    }

    private Square getGuiSquare(Game.Square square){
        Game.Board gameBoard = square.board;
        Board guiBoard = centralWidget.getBoard(gameBoard.name);
        return guiBoard.squares[square.row][square.column];
    }

    private void onSquareSelected(Square sourceSquare){
        // when hovered
        Board sourceBoard = (Board) sourceSquare.parentWidget();
        Game.Board sourceGameBoard = gameController.getBoard(sourceBoard.name);
        Game.Board destinationGameBoard;
        Game.Square destinationGameSquare;
        Square destinationSquare;

        if (sourceBoard.size == 8) {
           validMoves = gameController.getValidMoves(
                    sourceGameBoard.getSquare(sourceSquare.coordinates));
            if (validMoves.length > 0) {
                sourceSquare.setSelected(true);
                centralWidget.setSelectedSquare(sourceSquare);
            }
            for (Move move: validMoves) {
                Pair<Character[], String> destination = move.getDestinationBoardsAndSquareNames();
                Character[] destinationBoardNames = destination.getKey();
                String destinationSquareName = destination.getValue();
                for (Character boardName: destinationBoardNames) {
                    destinationGameBoard = gameController.getBoard(boardName);
                    destinationGameSquare = destinationGameBoard.getSquare(destinationSquareName);
                    destinationSquare = getGuiSquare(destinationGameSquare);
                    centralWidget.highlightSquare(destinationSquare);
                }
            }
        }
    }

    private void onMainWinClosed(){
        settings.flush();
    }

    private void setupGui(){
        setupMenuBar();
        mainWin.show();
    }

    private void selectSourceSquare(Square sourceSquare, Board sourceBoard){

    }

    private void resetSettings(){
        System.out.println("RESETTING SETTINGS!");
        settings.setValue(centralWidget.alpha.objectName()+"-whitePawn",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhitePawn.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteRook",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteRook.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteBishop",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteBishop.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteQueen",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteQueen.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteKnight",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteKnight.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteKing",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteKing.svg");

        settings.setValue(centralWidget.beta.objectName()+"-blackPawn",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackPawn.svg");
        settings.setValue(centralWidget.beta.objectName()+"-blackRook",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackRook.svg");
        settings.setValue(centralWidget.beta.objectName()+"-blackBishop",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackBishop.svg");
        settings.setValue(centralWidget.beta.objectName()+"-blackQueen",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackQueen.svg");
        settings.setValue(centralWidget.beta.objectName()+"-blackKnight",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackKnight.svg");
        settings.setValue(centralWidget.beta.objectName()+"-blackKing",
                "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackKing.svg");

        settings.setValue(centralWidget.alpha.objectName()+"-lightSquareColor",
                QColor.fromRgb(169, 234, 233));
        settings.setValue(centralWidget.alpha.objectName()+"-darkSquareColor",
                QColor.fromRgb(17, 123, 223));
        settings.setValue(centralWidget.beta.objectName()+"-lightSquareColor",
                QColor.fromRgb(169, 234, 233));
        settings.setValue(centralWidget.beta.objectName()+"-darkSquareColor",
                QColor.fromRgb(17, 123, 223));
        settings.setValue(centralWidget.gamma.objectName()+"-lightSquareColor",
                QColor.fromRgb(169, 234, 233));
        settings.setValue(centralWidget.gamma.objectName()+"-darkSquareColor",
                QColor.fromRgb(17, 123, 223));
        settings.setValue(centralWidget.whitePrison.objectName()+"-lightSquareColor",
                QColor.fromRgb(169, 234, 233));
        settings.setValue(centralWidget.whitePrison.objectName()+"-darkSquareColor",
                QColor.fromRgb(17, 123, 223));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-lightSquareColor",
                QColor.fromRgb(169, 234, 233));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-darkSquareColor",
                QColor.fromRgb(17, 123, 223));
        settings.setValue(centralWidget.blackPrison.objectName()+"-lightSquareColor",
                QColor.fromRgb(169, 234, 233));
        settings.setValue(centralWidget.blackPrison.objectName()+"-darkSquareColor",
                QColor.fromRgb(17, 123, 223));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-lightSquareColor",
                QColor.fromRgb(169, 234, 233));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-darkSquareColor",
                QColor.fromRgb(17, 123, 223));

        settings.setValue(centralWidget.alpha.objectName()+"-lightSquareHighlightColor",
                QColor.fromRgb(233, 234, 169));
        settings.setValue(centralWidget.alpha.objectName()+"-darkSquareHighlightColor",
                QColor.fromRgb(223, 199, 17));
        settings.setValue(centralWidget.beta.objectName()+"-lightSquareHighlightColor",
                QColor.fromRgb(233, 234, 169));
        settings.setValue(centralWidget.beta.objectName()+"-darkSquareHighlightColor",
                QColor.fromRgb(223, 199, 17));
        settings.setValue(centralWidget.gamma.objectName()+"-lightSquareHighlightColor",
                QColor.fromRgb(233, 234, 169));
        settings.setValue(centralWidget.gamma.objectName()+"-darkSquareHighlightColor",
                QColor.fromRgb(223, 199, 17));
        settings.setValue(centralWidget.whitePrison.objectName()+"-lightSquareHighlightColor",
                QColor.fromRgb(233, 234, 169));
        settings.setValue(centralWidget.whitePrison.objectName()+"-darkSquareHighlightColor",
                QColor.fromRgb(223, 199, 17));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-lightSquareHighlightColor",
                QColor.fromRgb(233, 234, 169));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-darkSquareHighlightColor",
                QColor.fromRgb(223, 199, 17));
        settings.setValue(centralWidget.blackPrison.objectName()+"-lightSquareHighlightColor",
                QColor.fromRgb(233, 234, 169));
        settings.setValue(centralWidget.blackPrison.objectName()+"-darkSquareHighlightColor",
                QColor.fromRgb(223, 199, 17));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-lightSquareHighlightColor",
                QColor.fromRgb(233, 234, 169));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-darkSquareHighlightColor",
                QColor.fromRgb(223, 199, 17));

        settings.setValue(centralWidget.alpha.objectName()+"-selectedCursor",
                new QCursor(Qt.CursorShape.OpenHandCursor));
        settings.setValue(centralWidget.alpha.objectName()+"-unselectedCursor",
                new QCursor(Qt.CursorShape.ArrowCursor));
        settings.setValue(centralWidget.beta.objectName()+"-selectedCursor",
                new QCursor(Qt.CursorShape.OpenHandCursor));
        settings.setValue(centralWidget.beta.objectName()+"-unselectedCursor",
                new QCursor(Qt.CursorShape.ArrowCursor));
        settings.setValue(centralWidget.gamma.objectName()+"-selectedCursor",
                new QCursor(Qt.CursorShape.OpenHandCursor));
        settings.setValue(centralWidget.gamma.objectName()+"-unselectedCursor",
                new QCursor(Qt.CursorShape.ArrowCursor));
        settings.setValue(centralWidget.whitePrison.objectName()+"-selectedCursor",
                new QCursor(Qt.CursorShape.OpenHandCursor));
        settings.setValue(centralWidget.whitePrison.objectName()+"-unselectedCursor",
                new QCursor(Qt.CursorShape.ArrowCursor));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-selectedCursor",
                new QCursor(Qt.CursorShape.OpenHandCursor));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-unselectedCursor",
                new QCursor(Qt.CursorShape.ArrowCursor));
        settings.setValue(centralWidget.blackPrison.objectName()+"-selectedCursor",
                new QCursor(Qt.CursorShape.OpenHandCursor));
        settings.setValue(centralWidget.blackPrison.objectName()+"-unselectedCursor",
                new QCursor(Qt.CursorShape.ArrowCursor));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-selectedCursor",
                new QCursor(Qt.CursorShape.OpenHandCursor));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-unselectedCursor",
                new QCursor(Qt.CursorShape.ArrowCursor));
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
}
