import GUI.*;
import GUI.Board;
import GUI.Square;
import Game.*;
import Shared.Color;
import Shared.Value;
import com.trolltech.qt.QSignalEmitter;
import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;
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

    private Square moveDecisionSquare = null;  // destination square when selecting board
    private ArrayList<Board> validBoards = new ArrayList<>();  // boards that can be selected
    private MoveType decisionType = null;  // type of the move that has to be decided

    private int squareShift = 0;  // amount of columns squares are shifted by
    private QParallelAnimationGroup shiftAnimations = new QParallelAnimationGroup();

    public GuiController(){
        mainWin = new MainWindow("mainWinow");

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
        signals.boardSelected.connect(this, "onBoardEnter(Board)");
        signals.boardDeselected.connect(this, "onBoardLeave(Board)");
        signals.boardScrolled.connect(this, "onBoardScrolled(Boolean)");

        centralWidget = new CentralWidget(mainWin, "centralWidget");
        mainWin.setCentralWidget(centralWidget);

        setupGui();
        if (settings.allKeys().size()==0) resetSettings();
        else settings.loadIntoBuffer();
        loadMoves(Persistence.moveChain((String) settings.getValue("lastGame")),
                QVariant.toInt(settings.getValue("lastPly")));
        mainWin.restoreGeometry(
                QVariant.toByteArray(settings.getValue(mainWin.objectName()+"-geometry")));
        shiftAnimations.finished.connect(this, "shiftSquares()");
        updateGuiToGame();
    }

    private void loadMoves(String[] moveChain, int upToPly) {
        gameController.loadMoves(moveChain, upToPly);
    }

    @Override
    public void setSavegamePath(Path path){
        saveGamePath = path;
    }

    private void onBoardScrolled(Boolean positive){
        scrollBoards(positive);
    }

    private void onBoardEnter(Board board){
        switchPieces(board, false);
    }

    private void onBoardLeave(Board board){
        switchPieces(board, true);
    }

    private void switchPieces(Board destinationBoard, boolean switchBack){
        if (moveDecisionSquare == null) return;
        if (!validBoards.contains(destinationBoard)) return;

        if (switchBack) QApplication.restoreOverrideCursor();
        else QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.PointingHandCursor));

/*        Square sourceSquare = (switchBack && decisionType != MoveType.STEAL)?
                destinationBoard.squares[moveDecisionSquare.row][moveDecisionSquare.column]:
                centralWidget.getSelectedSquare();*/
        Square sourceSquare = centralWidget.getSelectedSquare();

        Board sourceBoard = (Board) sourceSquare.parentWidget();
        Game.Board sourceGameBoard = gameController.getBoard(sourceBoard.name);

        Game.Board destinationGameBoard = gameController.getBoard(destinationBoard.name);

        Piece sourcePiece = sourceGameBoard.getPiece(sourceSquare.coordinates);
        Piece destinationPiece =
                (decisionType == MoveType.STEAL? sourceGameBoard: destinationGameBoard)
                .getPiece(moveDecisionSquare.coordinates);

        Square destinationSquare;

        if (decisionType == MoveType.STEAL){
            if (!switchBack){
                sourceSquare.display(null);
                moveDecisionSquare.display(null);

                destinationSquare = destinationBoard
                        .squares[moveDecisionSquare.row][moveDecisionSquare.column];
                sourceSquare =  // opposite board
                        (destinationBoard.name == 'A'? centralWidget.beta: centralWidget.alpha)
                                .squares[moveDecisionSquare.row][moveDecisionSquare.column];
            } else {
                centralWidget.alpha.squares[moveDecisionSquare.row][moveDecisionSquare.column]
                        .display(null);
                centralWidget.beta.squares[moveDecisionSquare.row][moveDecisionSquare.column]
                        .display(null);

                destinationSquare = moveDecisionSquare;
            }
        } else {
            destinationSquare = destinationBoard.squares
                    [moveDecisionSquare.row][moveDecisionSquare.column];
        }

        if(switchBack){
            centralWidget.deghostifySquare(sourceSquare);
            centralWidget.deghostifySquare(destinationSquare);
        } else {
            centralWidget.ghostifySquare(sourceSquare);
            centralWidget.ghostifySquare(destinationSquare);
        }

        destinationSquare.display(getPieceIcon(switchBack?destinationPiece: sourcePiece));

        if (destinationPiece == null){
            sourceSquare.display(getPieceIcon(switchBack? sourcePiece: null));
        } else {
            String destinationPieceName = destinationPiece.value.fullName;
            Color destinationPieceColor = destinationPiece.getColor();
            if (decisionType == MoveType.STEAL && !switchBack){
                destinationPieceColor = destinationPieceColor.opposite();
            }
            sourceSquare.display(switchBack?
                    getPieceIcon(sourcePiece):
                    getPieceIcon(destinationPieceName, destinationPieceColor));
        }
    }

    private void onRewindMove(){
        gameController.rewindMove();
        centralWidget.unPersistentlyHighlightAllSquares();
        updateGuiToGame();
    }

    private void onRepeatMove(){
        gameController.repeatMove();
        centralWidget.unPersistentlyHighlightAllSquares();
        updateGuiToGame();
    }

    private void onNewGame(){
        gameController.resetGame();
        currentSavegame = null;
        centralWidget.unPersistentlyHighlightAllSquares();
        updateGuiToGame();
    }

    private void hostageSelected(Square square){
        Value value = getValueOn(square, gameController.getCurrentPlayer().opposite());
        if (value == null) return;
        if (centralWidget.isPersistentlyHighlighted(square)){  // hostage already selected
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
        settings.setValue(mainWin.objectName()+"-geometry", mainWin.saveGeometry());
        settings.flush();
        QApplication.exit();
    }

    private void onCancelMove(){
        centralWidget.deselectSquare();
        centralWidget.unHighlightAllSquares();
        centralWidget.deghostifyAllSquares();

        if (moveDecisionSquare != null) {
            moveDecisionSquare = null;
            QApplication.restoreOverrideCursor();
        }

        if (selectedHostages.size() > 0){
            if (centralWidget.isDragging()){
                // dragging freed hostage
                stopDragging();
            }
            else {
                centralWidget.unPersistentlyHighlightAllSquares();
            }
            return;
        }
        stopDragging();
        centralWidget.unPersistentlyHighlightAllSquares();
        updateGuiToGame();
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
                    gameController.getGameState(gameController.getCurrentPlayer().opposite()),
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
                    gameController.getGameState(gameController.getCurrentPlayer().opposite()),
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
        switch (validMoves.length){
            case 0:
                signals.moveCanceled.emit();
                return;
            case 1: doMove(validMoves[0]); break;
            default:
                decideMove(validMoves, destinationSquare); break;
        }
    }

    private void stopDragging(){
        centralWidget.dragSquare.display(null);
        centralWidget.dragSquare.hide();
        centralWidget.setDragging(false);
        updateGuiToGame();
    }

    // Call when multiple moves are possible. Resolve choice by letting user choose boards.
    private void decideMove(Move[] options, Square destinationSquare){
        Board board;

        // all options are of same type,
        // as translation and swap are handled the same (like in for loop below)
        decisionType = MoveType.of(options[0]);
        if (decisionType == MoveType.STEAL && options[0].pieceNames[0] == options[0].pieceNames[1]) {
            // both options are equivalent
            doMove(options[0]);
            return;
        }

        validBoards.clear();
        // persistently highlight relevant squares
        for (Move option: options){
            board = centralWidget.getBoard(option.boardNames[1]);
            if (!validBoards.contains(board)) validBoards.add(board);
            if (MoveType.of(option) == MoveType.STEAL)
                    board = centralWidget.getBoard(option.boardNames[2]);
                    if (!validBoards.contains(board)) validBoards.add(board);
            }
        for (Board validBoard: validBoards){
            centralWidget.persistentlyHighlightSquare(
                    validBoard.squares[destinationSquare.row][destinationSquare.column]);
        }
        stopDragging();
        moveDecisionSquare = destinationSquare;
        centralWidget.getSelectedSquare().setSelected(false);  // purely for cosmetic reasons
        return;
    }

    private void doMove(Move move){
        gameController.addMove(move);
        centralWidget.unPersistentlyHighlightAllSquares();
        moveDecisionSquare = null;
        stopDragging();
        centralWidget.deselectSquare();
        QApplication.restoreOverrideCursor();
        persistentlyHighlightAllCheckBreakingSourceSquare();
        centralWidget.deghostifyAllSquares();
        updateGuiToGame();
    }

    private void persistentlyHighlightAllCheckBreakingSourceSquare(){
        if (gameController.inCheck()){
            Pair<Character[], String> source;
            int[] coordinates;
            for (Move validMove:
                    gameController.getAllCheskBreakingMoves(gameController.getCurrentPlayer())){
                source = validMove.getSource();
                coordinates = Game.Board.getCoordinates(source.getValue());
                centralWidget.persistentlyHighlightSquare(
                        centralWidget.getBoard(source.getKey()[0])
                                .squares[coordinates[0]][coordinates[1]]
                );
            }
        }
    }

/*    private void scrollBoards(int columns){
        int extraRow;
        QPropertyAnimation animation;
*//*        int delta = (columns + squareShift) % 8;
        if (delta < 0) delta += 8;*//*

        for (Board board:
                new Board[]{centralWidget.alpha, centralWidget.beta, centralWidget.gamma}){

            extraRow = 0;
            for (Square extraSquare:
                    (columns>0? board.extraColumnLeft: board.extraColumnRight)){

                extraSquare.setBackgroundUnhighlightedBrush(new QBrush(QColor.fromRgb(0, 0, 0)));

                ((QGridLayout) board.layout()).removeWidget(extraSquare);

                extraSquare.display(getPieceIcon(gameController.getBoard(board.name)
                        .getPiece(extraRow, columns>0? 0: 7)));

                if (columns < 0) {
                    extraSquare.move(board.x() - extraSquare.width(), extraSquare.y());
                } else {
                    extraSquare.move(board.x()+board.width() + extraSquare.width(), extraSquare.y());
                }

                animation = new QPropertyAnimation(extraSquare, new QByteArray("pos"));
                animation.setStartValue(extraSquare.pos());
                animation.setEndValue(new QPoint(Math.round(extraSquare.x() +
                        extraSquare.width() * columns), extraSquare.y()));
                animation.setDuration(100);
                animation.setEasingCurve(new QEasingCurve(QEasingCurve.Type.OutQuad));
                shiftAnimations.addAnimation(animation);

                extraRow += 1;
            }

            for (Square[] squareRow: board.squares){
                for (Square square: squareRow){
                    ((QGridLayout) board.layout()).removeWidget(square);
                    animation = new QPropertyAnimation(square, new QByteArray("pos"));
                    animation.setStartValue(square.pos());
                    animation.setEndValue(new QPoint(Math.round(square.x() +
                            square.width() * columns), square.y()));
                    animation.setDuration(100);
                    animation.setEasingCurve(new QEasingCurve(QEasingCurve.Type.OutQuad));
                    shiftAnimations.addAnimation(animation);
                }
            }
        }
        squareShift += columns;
        squareShift %= 8;
        if (squareShift < 0) squareShift += 8;
        shiftAnimations.start();
    }*/

    private void scrollBoards(boolean left){
        if (shiftAnimations.state() != QAbstractAnimation.State.Stopped) return;
        QPropertyAnimation animation;
        QPoint coordinates;
        int column;

        squareShift += left? -1: 1;
        squareShift %= 8;
        if (squareShift < 0) squareShift += 8;

        for (Board board:
                new Board[]{centralWidget.alpha, centralWidget.beta, centralWidget.gamma}){

            for (Square extraSquare:
                    (left? board.extraColumnLeft: board.extraColumnRight)){

                ((QGridLayout) board.layout()).removeWidget(extraSquare);
                extraSquare.raise();

                column = ((left? 7: 0) - squareShift) % 8;
                if (column < 0) column += 8;

                extraSquare.setBackgroundUnhighlightedBrush(
                        board.squares[extraSquare.row][(column) % 8]
                                .getBackgroundBrush()
                );

                extraSquare.display(getPieceIcon(gameController.getBoard(board.name)
                        .getPiece(extraSquare.row, column)));

                if (!left) {
                    coordinates = new QPoint(-extraSquare.width(), extraSquare.y());
                } else {
                    coordinates = new QPoint(board.width(), extraSquare.y());
                }

                extraSquare.move(coordinates);

                animation = new QPropertyAnimation(extraSquare, new QByteArray("pos"));
                animation.setStartValue(extraSquare.pos());
                animation.setEndValue(new QPoint(Math.round(extraSquare.x() +
                        extraSquare.width() * (left? -1: 1)), extraSquare.y()));
                animation.setDuration(300);
                animation.setEasingCurve(new QEasingCurve(QEasingCurve.Type.InOutQuad));
                shiftAnimations.addAnimation(animation);
            }

            for (Square[] squareRow: board.squares){
                for (Square square: squareRow){
                    ((QGridLayout) board.layout()).removeWidget(square);
                    animation = new QPropertyAnimation(square, new QByteArray("pos"));
                    animation.setStartValue(square.pos());
                    animation.setEndValue(new QPoint(Math.round(square.x() +
                            square.width() * (left? -1: 1)), square.y()));
                    animation.setDuration(300);
                    animation.setEasingCurve(new QEasingCurve(QEasingCurve.Type.InOutQuad));
                    shiftAnimations.addAnimation(animation);
                }
            }
        }
        shiftAnimations.start();
    }

    private void shiftSquares(){
        shiftAnimations.clear();
        int shift;
        int extraColumn = 0;
        for (Board board:
                new Board[]{centralWidget.alpha, centralWidget.beta, centralWidget.gamma}) {
            for (Square[] extraSquareColumn:
                    new Square[][]{board.extraColumnLeft, board.extraColumnRight}) {
                for (Square extraSquare : extraSquareColumn) {
                    ((QGridLayout) board.layout()).addWidget(
                            extraSquare,
                            extraSquare.row,
                            extraColumn);
                }
                extraColumn = 7;
            }

            for (Square[] squareRow : board.squares) {
                for (Square square : squareRow) {
                    square.raise();
                    shift = (squareShift + square.column) % 8;
                    if (shift < 0) shift += 8;
                    ((QGridLayout) board.layout()).addWidget(
                            square,
                            square.row,
                            shift);
                }
            }
        }
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

        if (gameController.inCheck()) persistentlyHighlightAllCheckBreakingSourceSquare();
        resetHostages();
    }

    private void resetHostages() {
        selectedHostages.clear();
        selectedHostageSum = 0;
        for (Board board: new Board[]{centralWidget.whitePrison, centralWidget.blackPrison}) {
            for (Square[] squareRow : board.squares) {
                for (Square square: squareRow){
                    centralWidget.unPersistentlyHighlightSquare(square);
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
        if (moveDecisionSquare != null) return;

        Board sourceBoard;
        Game.Board sourceGameBoard;
        Game.Board destinationGameBoard;
        Game.Square destinationGameSquare;

        centralWidget.deselectSquare();

        sourceBoard = (Board) sourceSquare.parentWidget();
        sourceGameBoard = gameController.getBoard(sourceBoard.name);

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
        } else if (sourceBoard.name == 'F'){
            Value value = getValueOn(sourceSquare, gameController.getCurrentPlayer());
            if (value == null) return;
            centralWidget.highlightSquare(sourceSquare);
            centralWidget.selectSquare(sourceSquare);
            return;
        }

        validMoves = gameController.getValidMoves(
                sourceGameBoard.getSquare(sourceSquare.coordinates));
        if (validMoves.length > 0) centralWidget.selectSquare(sourceSquare);
        for (Move move: validMoves) {
            Pair<Character[], String> destination = move.getDestination();
            Character[] destinationBoardNames = destination.getKey();
            String destinationSquareName = destination.getValue();
            for (Character boardName: destinationBoardNames) {
               destinationGameBoard = gameController.getBoard(boardName);
               destinationGameSquare = destinationGameBoard.getSquare(destinationSquareName);
               centralWidget.highlightSquare(getGuiSquare(destinationGameSquare));
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

    private void boardSelected(Board destinationBoard){
        if (moveDecisionSquare == null) return;
        // board selected, necessary for translate on C, steal, swap
        Square sourceSquare = centralWidget.getSelectedSquare();
        Square destinationSquare = moveDecisionSquare;  // on same board as source square

        String sourceSquareName = Game.Board.getSquareName(sourceSquare.coordinates);
        String destinationSquareName = Game.Board.getSquareName(destinationSquare.coordinates);

        Board sourceBoard = (Board) centralWidget.getSelectedSquare().parentWidget();

        Piece sourcePiece =
                gameController.getBoard(sourceBoard.name).getPiece(sourceSquareName);
        Piece destinationPiece =
                gameController.getBoard(sourceBoard.name).getPiece(destinationSquareName);
        Piece otherPiece = // on selected board
                gameController.getBoard(destinationBoard.name).getPiece(destinationSquareName);

        Character[] pieceNames;
        Character[] boardNames;
        String[] squareNames;

        if (decisionType == MoveType.STEAL){
            pieceNames = new Character[]{sourcePiece.value.name, destinationPiece.value.name};
            boardNames = new Character[]{'C', destinationBoard.name,
                    (destinationBoard == centralWidget.alpha?
                    centralWidget.beta: centralWidget.alpha).name};
            squareNames = new String[]{sourceSquareName, destinationSquareName};
        } else {
            pieceNames = otherPiece == null?
                    new Character[]{sourcePiece.value.name}:
                    new Character[]{sourcePiece.value.name, otherPiece.value.name};
            boardNames = new Character[]{sourceBoard.name, destinationBoard.name};
            squareNames = new String[]{sourceSquareName, destinationSquareName};
        }
        Move move = new Move(
                gameController.getCurrentPlayer(),
                null,
                null,
                pieceNames,
                boardNames,
                squareNames
        );
        if (gameController.validMove(move)) doMove(move);
    }

    private void onPieceSelected(Square sourceSquare){
        //player clicked on square
        Board sourceBoard = (Board) sourceSquare.parentWidget();
        boardSelected(sourceBoard);

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

    private QGraphicsSvgItem getPieceIcon(Game.Piece piece){
        if (piece == null) return null;
        String key = centralWidget.objectName() + "-" +
                (piece.getColor() == Color.WHITE? "white": "black") + piece.value.fullName;
        return new QGraphicsSvgItem((String) settings.getValue(key));
    }

    private void setupGui(){
        setupMenuBar();

        mainWin.show();
    }

    private void resetSettings(){
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
