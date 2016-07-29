package GUI;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;

public class MoveNavigator extends QFrame{
    private final Signals signals = Signals.getInstance();
    public final QTableWidget movesViewer = new QTableWidget(this);
    private boolean doNotSendSignal = false;
    public MoveNavigator(QWidget parent, String objectName) {
        super(parent);
        setObjectName(objectName);

        setLayout(new QVBoxLayout(this));

        movesViewer.setColumnCount(2);
        ArrayList<String> headers = new ArrayList<>(2);
        headers.add("White");
        headers.add("Black");
        movesViewer.setHorizontalHeaderLabels(headers);
        //movesViewer.itemClicked.connect(this, "onItemClicked(QTableWidgetItem)");
        movesViewer.itemChanged.connect(this, "onItemChanged(QTableWidgetItem)");
        movesViewer.currentItemChanged.connect(this, "onCurrentItemChanged(QTableWidgetItem)");
        movesViewer.setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection);
        movesViewer.horizontalHeader().setResizeMode(QHeaderView.ResizeMode.Stretch);
        layout().addWidget(movesViewer);
    }

    private void onCurrentItemChanged(QTableWidgetItem item){
        if (doNotSendSignal) return;
        signals.changeCurrentPly.emit(item.row()*2 + item.column());
    }

    private void onItemChanged(QTableWidgetItem item){
        if (doNotSendSignal) return;
        signals.setMove.emit(item.row()*2 + item.column(), item.text());
    }

    public void updateToArray(String[] moves){
        updateToArray(moves, -1);
    }

    public void updateToArray(String[] moves, int ply){
        doNotSendSignal = true;
        movesViewer.clearContents();
        //movesViewer.clear();
        movesViewer.setRowCount((int) Math.ceil((moves.length+1)/2.0));
        QTableWidgetItem item;
        int index = 0;
        for (String move: moves){
            item = new QTableWidgetItem(move);
            item.setTextAlignment(Qt.AlignmentFlag.AlignCenter.value());
            movesViewer.setItem(index/2, index%2, item);
            index += 1;
        }
        item = new QTableWidgetItem(); // placeholder
        movesViewer.setItem(index/2, index%2, item);
        if (index%2 == 0){
            item = new QTableWidgetItem(); // placeholder
            movesViewer.setItem(index/2, 1, item);
            //item.setFlags(Qt.ItemFlag.ItemIsEnabled);
            item.setFlags(Qt.ItemFlag.NoItemFlags);
        }
        setCurrentPly(ply==-1? index: ply);
        doNotSendSignal = false;
    }

    public void setCurrentPly(int ply){
        setCurrentPly(ply, true);
    }

    public void setCurrentPly(int ply, boolean supressSignals){
        doNotSendSignal = supressSignals;
        movesViewer.setCurrentItem(movesViewer.itemAt(ply/2, ply%2));
        QModelIndex index = movesViewer.model().index(ply/2, ply%2);
        movesViewer.setCurrentIndex(index);
        doNotSendSignal = false;
    }

    private void moveSelection(boolean left){
        QTableWidgetItem currentItem = movesViewer.currentItem();
        if (currentItem == null) return;
        int index = currentItem.row()*2 + currentItem.column() + (left? -1: 1);
        int rows = movesViewer.rowCount();
        int itemCount = rows * 2 -1;
        QTableWidgetItem lastItem = movesViewer.itemAt(rows-1, 1);
        if (!lastItem.flags().isSet(Qt.ItemFlag.ItemIsEnabled)) itemCount -= 1;
        index %= itemCount;
        if (index < 0) index += itemCount;
        setCurrentPly(index, false);
    }

    @Override
    protected void wheelEvent(QWheelEvent wheelEvent) {
        if (movesViewer.verticalScrollBar().isVisible()) return;
        moveSelection(wheelEvent.delta()<0);
        wheelEvent.accept();
    }

    @Override
    protected void keyPressEvent(QKeyEvent keyEvent) {
        if (keyEvent.key() == Qt.Key.Key_Left.value()) moveSelection(true);
        else if (keyEvent.key() == Qt.Key.Key_Right.value()) moveSelection(false);
        keyEvent.accept();
    }
}
