import GUI.*;
import GUI.Board;
import GUI.Square;
import Game.*;
import Shared.Color;
import Shared.Value;
import com.trolltech.qt.QSignalEmitter;
import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.svg.QGraphicsSvgItem;
import javafx.util.Pair;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class GuiController extends QSignalEmitter implements Controller{
    private final Signals signals = Signals.getInstance();
    private final SettingsManager settings = SettingsManager.getInstance();
    private final Game.Controller gameController = new Game.Controller();
    private final MainWindow mainWin;
    private final CentralWidget centralWidget;

    private Move[] validMoves = new Move[0];
    private Path saveGamePath = Paths.get(new File("").toURI());
    private Path currentSavegame = null;

    private final ArrayList<Value> selectedHostages = new ArrayList<>();
    private int selectedHostageSum = 0;

    private String moveDecisionSquareName = null;

    public GuiController(){
        mainWin = new MainWindow();

        signals.squareSelected.connect(this, "onSquareSelected(Square)");
        signals.destinationSelected.connect(this, "onDestinationSelected(Square)");
        signals.pieceSelected.connect(this, "onPieceSelected(Square)");
        signals.moveCanceled.connect(this, "onCancelMove()");
        signals.saveGameAs.connect(this, "onSaveGameAs()");
        signals.saveGame.connect(this, "onSaveGame()");
        signals.loadGame.connect(this, "onLoadGame()");
        signals.newGame.connect(this, "onNewGame()");
        signals.rewindMove.connect(this, "onRewindMove()");
        signals.repeatMove.connect(this, "onRepeatMove()");
        signals.exitApplication.connect(this, "onExitApplication()");
        signals.boardSelected.connect(this, "onBoardSelected(Board)");

        centralWidget = new CentralWidget(mainWin, "centralWidget");
        mainWin.setCentralWidget(centralWidget);

        setupGui();
        if (settings.allKeys().size()==0) resetSettings();
        else settings.loadIntoBuffer();
        gameController.loadMoves(Persistence.moveChain((String) settings.getValue("lastGame")),
                QVariant.toInt(settings.getValue("lastPly")));
        updateGuiToGame();
    }

    @Override
    public void setSavegamePath(Path path){
        saveGamePath = path;
    }

    private void onBoardSelected(Board board){
        if (moveDecisionSquareName != null){

        }
    }

    private void onRewindMove(){
        gameController.rewindMove();
        updateGuiToGame();
    }

    private void onRepeatMove(){
        gameController.repeatMove();
        updateGuiToGame();
    }

    private void onNewGame(){
        gameController.resetGame();
        currentSavegame = null;
        updateGuiToGame();
    }

    private void hostageSelected(Square square){
        Value value = getValueOn(square, gameController.getCurrentPlayer().opposite());
        if (value == null) return;
        if (centralWidget.isPersistentlyHighlighted(square)){
            selectedHostages.remove(value);
            selectedHostageSum -= value.value;
            centralWidget.unPersistentlyHighlightSquare(square);
            highlightValidHostages();
            return;
        }
        selectedHostages.add(value);
        selectedHostageSum += value.value;
        centralWidget.persistentlyHighlightSquare(square);
        highlightValidHostages();
    }

    private void highlightValidHostages(){
        for (Square[] squareRow: centralWidget.getPrison(
                gameController.getCurrentPlayer().opposite()).squares){
            for (Square square: squareRow){
                Value value = getValueOn(square);
                if (value == null) return;
                if (value.value <= selectedHostageSum){
                    centralWidget.persistentlyHighlightSquare(square);
                } else centralWidget.unPersistentlyHighlightSquare(square);
            }
        }
    }

    private void onLoadGame(){
        Path loadGamePath = Paths.get(QFileDialog.getOpenFileName(
                centralWidget,
                "Load Game",
                saveGamePath.toString(),
                new QFileDialog.Filter("CHASS Game Notation (*.cgn)")));
        if (!loadGamePath.toString().equals("")) onLoadGame(loadGamePath);
    }

    private void onLoadGame(Path path){
        gameController.loadMoves(Persistence.loadMoves(path), -1);
        currentSavegame = path;
        updateGuiToGame();
    }

    private void onSaveGame(){
        if (currentSavegame == null) currentSavegame = saveGamePath;
        currentSavegame = Persistence.saveMoves(
                gameController.getMoveHistoryStrings(), currentSavegame);
    }

    private void onSaveGameAs(){
        String path = QFileDialog.getSaveFileName(
                centralWidget,
                "Save Game As...",
                saveGamePath.toString(),
                new QFileDialog.Filter("CHASS Game Notation (*.cgn)"));
        if (!path.equals("")) currentSavegame = Persistence.saveMoves(
                gameController.getMoveHistoryStrings(),
                Paths.get(path));
    }

    private void onExitApplication(){
        settings.setValue("lastGame",
                Persistence.moveSeries(gameController.getMoveHistoryStrings()));
        settings.setValue("lastPly", gameController.getCurrentPly());
        settings.flush();
        QApplication.exit();
    }

    private void onCancelMove(){
        stopDragging();
        centralWidget.unHighlightAllSquares();
        //centralWidget.unPersistentlyHighlightAllSquares();
    }

    private void onDestinationSelected(Square destinationSquare){
        Board sourceBoard = (Board) centralWidget.getSelectedSquare().parentWidget();
        Board destinationBoard = (Board) destinationSquare.parentWidget();
        switch (destinationBoard.name){
            case 'A':
            case 'B':
            case 'C':
                break;
            default:
                signals.moveCanceled.emit();
                return;
        }
        Game.Square destinationGameSquare = getGameSquare(destinationSquare);
        if (sourceBoard.name == 'P' &&
                sourceBoard.color == gameController.getCurrentPlayer().opposite()){
            // hostage exchange

            if(destinationBoard != centralWidget.gamma ||
                    destinationGameSquare.board.getPiece(destinationGameSquare) != null) {
                signals.moveCanceled.emit();
                return;
            }

            ArrayList<Character> pieceNames = new ArrayList<>();
            pieceNames.add(getValueOn(centralWidget.getSelectedSquare()).name);
            selectedHostages.forEach(value -> pieceNames.add(value.name));

            doMove(new Move(
                    gameController.getCurrentPlayer(),
                    gameController.getGameState(),
                    null,
                    pieceNames.toArray(new Character[pieceNames.size()]),
                    new Character[0],
                    new String[] {Game.Board.getSquareName(destinationGameSquare)}
            ));
            return;
        } else if (sourceBoard.name == 'F' &&
                sourceBoard.color == gameController.getCurrentPlayer()){
            // drop
            if(destinationBoard != centralWidget.gamma ||
                    destinationGameSquare.board.getPiece(destinationGameSquare) != null) {
                signals.moveCanceled.emit();
                return;
            }
            doMove(new Move(
                    gameController.getCurrentPlayer(),
                    gameController.getGameState(),
                    null,
                    new Character[]{
                            getValueOn(centralWidget.getSelectedSquare()).name
                    },
                    new Character[0],
                    new String[]{Game.Board.getSquareName(destinationGameSquare)}
            ));
            return;
        }
        Move[] validMoves = gameController.getValidMoves(this.validMoves, destinationGameSquare);
        System.out.print("num moves: ");
        System.out.println(validMoves.length);
        switch (validMoves.length){
            case 0:
                signals.moveCanceled.emit();
                return;
            case 1: doMove(validMoves[0]); break;
            default: decideMove(validMoves); break;
        }
    }

    private void stopDragging(){
        centralWidget.dragSquare.display(null);
        centralWidget.dragSquare.hide();
        centralWidget.deselectSquare();
        centralWidget.setDragging(false);
        updateGuiToGame();
    }

    // Call when multiple moves are possible. Resolve choice by letting user choose boards.
    private void decideMove(Move[] options){
        Square square;
        int[] coordinates;
        for (Move option: options){
            for (Character boardName: option.boardNames){
                coordinates = Game.Board.getCoordinates(option.squareNames[0]);
                square = centralWidget.getBoard(boardName).squares[coordinates[0]][coordinates[1]];
                centralWidget.persistentlyHighlightSquare(square);
            }
        }
        moveDecisionSquareName = options[0].squareNames[0];
        return;
    }

    private void doMove(Move move){
        gameController.addMove(move);
        centralWidget.unPersistentlyHighlightAllSquares();
        stopDragging();
        updateGuiToGame();
    }

    private void updateGuiToGame() {
        Character[] boardNames = new Character[]{'A', 'B', 'C'};
        Board guiBoard;
        Square guiSquare;
        Game.Board gameBoard;
        QGraphicsSvgItem item;
        for (Character boardName: boardNames){
            guiBoard = centralWidget.getBoard(boardName);
            gameBoard = gameController.getBoard(boardName);
            wipeBoard(guiBoard);
            for (Game.Piece piece: gameBoard.getPieces(null, null)){
                item = getPieceIcon(piece.value.fullName, piece.getColor());
                guiSquare = getGuiSquare(gameBoard.getSquare(piece));
                guiSquare.display(item);
            }
        }
        boardNames = new Character[]{'P', 'F'};
        PieceCollection collection;
        int row, column, index = 0;
        for (Color side: Color.values()){
            if (side == Color.NONE) continue;
            for (char boardName: boardNames) {
                collection = boardName == 'P'?
                        gameController.getPrison(side):
                        gameController.getAirfield(side);
                guiBoard = boardName == 'P'?
                        centralWidget.getPrison(side):
                        centralWidget.getAirfield(side);
                wipeBoard(guiBoard);
                index = 0;
                for (Value value : collection.getPieces()) {
                    column = index % 4;
                    row = index / 4;
                    guiSquare = guiBoard.squares[row][column];
                    guiSquare.display(getPieceIcon(value.fullName,
                            boardName == 'P'? side.opposite(): side));
                    index ++;
                }
            }
        }

    }

    private void wipeBoard(Board guiBoard) {
        for (Square[] row: guiBoard.squares){
            for (Square square: row){
                square.display(null);
                centralWidget.unHighlightSquare(square);
            }
        }
    }

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
        // when hovered, sets square to selected if valid

        Board sourceBoard = (Board) sourceSquare.parentWidget();
        Game.Board sourceGameBoard = gameController.getBoard(sourceBoard.name);
        Game.Board destinationGameBoard;
        Game.Square destinationGameSquare;
        Square destinationSquare;

        if (sourceBoard.name == 'P'){
            if (sourceBoard.color == gameController.getCurrentPlayer()){
                // select hostages to give
                if (getValueOn(sourceSquare, gameController.getCurrentPlayer().opposite())!=null) {
                    sourceSquare.setCursor(new QCursor(Qt.CursorShape.PointingHandCursor));
                    centralWidget.highlightSquare(sourceSquare);
                }
            } else {
                if (centralWidget.isPersistentlyHighlighted(sourceSquare)){
                    centralWidget.selectSquare(sourceSquare);
                }
            }
            return;
            //centralWidget.setSelectedSquare(sourceSquare);
            //sourceSquare.setCursor(new QCursor(Qt.CursorShape.PointingHandCursor));
        } else if (sourceBoard.name == 'F'){
            Value value = getValueOn(sourceSquare, gameController.getCurrentPlayer());
            if (value == null) return;
            centralWidget.highlightSquare(sourceSquare);
            centralWidget.selectSquare(sourceSquare);
            return;
        }

       validMoves = gameController.getValidMoves(
                sourceGameBoard.getSquare(sourceSquare.coordinates));
        if (validMoves.length > 0) {
            centralWidget.selectSquare(sourceSquare);
        }
        for (Move move: validMoves) {
            Pair<Character[], String> destination = move.getDestination();
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

    private Value getValueOn(Square square, Color player){
        Value result = getValueOn(square);
        if (((Board) square.parentWidget()).name == 'P') player = player.opposite();
        return (((Board) square.parentWidget()).color != player)? null: result;
    }

    private Value getValueOn(Square square){
        Board sourceBoard = (Board) square.parentWidget();
        PieceCollection collection = sourceBoard.name == 'P'?
                gameController.getPrison(sourceBoard.color):
                gameController.getAirfield(sourceBoard.color);
        int index = square.row * 4 + square.column;
        if (collection.size() <= index) return null;
        return collection.getPieceAt(index);
    }

    private void onPieceSelected(Square sourceSquare){
        Board sourceBoard = (Board) sourceSquare.parentWidget();
        if (sourceSquare.isSelected()){
            // if it is selected, it has to be valid
            startDragging(sourceSquare);
            return;
        }

        if (sourceBoard.name == 'P'){
            // if selecting hostages to give
            hostageSelected(sourceSquare);
        }
    }

    private void startDragging(Square sourceSquare){
        Board sourceBoard = (Board) sourceSquare.parentWidget();
        Value pieceValue;
        if (sourceBoard.size == 8) {
            Game.Square square = getGameSquare(sourceSquare);
            pieceValue = square.board.getPiece(square).value;
        }else{
            pieceValue = getValueOn(sourceSquare);
        }
        centralWidget.dragSquare.display(
                getPieceIcon(pieceValue.fullName, gameController.getCurrentPlayer()));
        QPoint pos = centralWidget.mapFromGlobal(QCursor.pos());
        int x = pos.x();
        int y = pos.y();
        centralWidget.dragSquare.resize(sourceSquare.size());
        centralWidget.dragSquare.move(
                x-centralWidget.dragSquare.width()/2,
                y-centralWidget.dragSquare.height()/2);
        sourceSquare.display(null);
        centralWidget.dragSquare.show();
        centralWidget.setDragging(true);
    }

    private QGraphicsSvgItem getPieceIcon(String pieceName, Color color){
        String key = centralWidget.objectName() + "-" +
                (color == Color.WHITE? "white": "black") + pieceName;
        return new QGraphicsSvgItem((String) settings.getValue(key));
    }

    private void setupGui(){
        setupMenuBar();

        mainWin.show();
    }

    private void resetSettings(){
        System.out.println("RESETTING SETTINGS!");

        settings.setValue(centralWidget.objectName()+"-whitePawn",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhitePawn.svg");
        settings.setValue(centralWidget.objectName()+"-whiteRook",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteRook.svg");
        settings.setValue(centralWidget.objectName()+"-whiteBishop",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteBishop.svg");
        settings.setValue(centralWidget.objectName()+"-whiteQueen",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteQueen.svg");
        settings.setValue(centralWidget.objectName()+"-whiteKnight",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteKnight.svg");
        settings.setValue(centralWidget.objectName()+"-whiteKing",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\WhiteKing.svg");

        settings.setValue(centralWidget.objectName()+"-blackPawn",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackPawn.svg");
        settings.setValue(centralWidget.objectName()+"-blackRook",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackRook.svg");
        settings.setValue(centralWidget.objectName()+"-blackBishop",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackBishop.svg");
        settings.setValue(centralWidget.objectName()+"-blackQueen",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackQueen.svg");
        settings.setValue(centralWidget.objectName()+"-blackKnight",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackKnight.svg");
        settings.setValue(centralWidget.objectName()+"-blackKing",
                        "D:\\Documents\\Programs\\Chass\\Assets\\Pieces\\BlackKing.svg");

        settings.setValue(centralWidget.alpha.objectName()+"-lightSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(169, 234, 233)));
        settings.setValue(centralWidget.alpha.objectName()+"-darkSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(17, 123, 223)));
        settings.setValue(centralWidget.beta.objectName()+"-lightSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(169, 234, 233)));
        settings.setValue(centralWidget.beta.objectName()+"-darkSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(17, 123, 223)));
        settings.setValue(centralWidget.gamma.objectName()+"-lightSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(169, 234, 233)));
        settings.setValue(centralWidget.gamma.objectName()+"-darkSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(17, 123, 223)));
        settings.setValue(centralWidget.whitePrison.objectName()+"-lightSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(169, 234, 233)));
        settings.setValue(centralWidget.whitePrison.objectName()+"-darkSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(17, 123, 223)));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-lightSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(169, 234, 233)));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-darkSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(17, 123, 223)));
        settings.setValue(centralWidget.blackPrison.objectName()+"-lightSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(169, 234, 233)));
        settings.setValue(centralWidget.blackPrison.objectName()+"-darkSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(17, 123, 223)));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-lightSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(169, 234, 233)));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-darkSquareUnhighlightedBrush",
                new QBrush(QColor.fromRgb(17, 123, 223)));

        settings.setValue(centralWidget.alpha.objectName()+"-lightSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(233, 234, 169)));
        settings.setValue(centralWidget.alpha.objectName()+"-darkSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(223, 199, 17)));
        settings.setValue(centralWidget.beta.objectName()+"-lightSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(233, 234, 169)));
        settings.setValue(centralWidget.beta.objectName()+"-darkSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(223, 199, 17)));
        settings.setValue(centralWidget.gamma.objectName()+"-lightSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(233, 234, 169)));
        settings.setValue(centralWidget.gamma.objectName()+"-darkSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(223, 199, 17)));
        settings.setValue(centralWidget.whitePrison.objectName()+"-lightSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(233, 234, 169)));
        settings.setValue(centralWidget.whitePrison.objectName()+"-darkSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(223, 199, 17)));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-lightSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(233, 234, 169)));
        settings.setValue(centralWidget.whiteAirfield.objectName()+"-darkSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(223, 199, 17)));
        settings.setValue(centralWidget.blackPrison.objectName()+"-lightSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(233, 234, 169)));
        settings.setValue(centralWidget.blackPrison.objectName()+"-darkSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(223, 199, 17)));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-lightSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(233, 234, 169)));
        settings.setValue(centralWidget.blackAirfield.objectName()+"-darkSquareHighlightedBrush",
                new QBrush(QColor.fromRgb(223, 199, 17)));

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

        settings.setValue("lastGame", "");
        settings.setValue("lastPly", -1);
    }

    private void setupMenuBar(){
        QMenuBar menuBar = new QMenuBar(mainWin);
        QMenu fileMenu = new QMenu("File");
        menuBar.addMenu(fileMenu);
        fileMenu.addAction("Save Game", signals.saveGame);
        fileMenu.addAction("Save Game As...", signals.saveGameAs);
        fileMenu.addAction("Load", signals.loadGame);
        fileMenu.addAction("Exit", signals.exitApplication);
        fileMenu.addAction("New Game", signals.newGame);
        fileMenu.addAction("Undo Move", signals.rewindMove);
        fileMenu.addAction("Redo Move", signals.repeatMove);
        mainWin.setMenuBar(menuBar);
    }
}
