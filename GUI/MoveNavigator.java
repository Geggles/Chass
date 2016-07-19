package GUI;

import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;

public class MoveNavigator extends QFrame{
    private final Signals signals = Signals.getInstance();
    public final QTableWidget movesViewer = new QTableWidget(this);
    private boolean dontSendSignal = false;
    public MoveNavigator(QWidget parent, String objectName) {
        super(parent);
        setObjectName(objectName);

        setLayout(new QVBoxLayout(this));

        QWidget topBar = new QWidget(this);
        topBar.setLayout(new QHBoxLayout(topBar));
        QPushButton backButton = new QPushButton(topBar);
        QLineEdit moveEnter = new QLineEdit(topBar);
        QPushButton forwardButton = new QPushButton(topBar);
        topBar.layout().addWidget(backButton);
        topBar.layout().addWidget(moveEnter);
        topBar.layout().addWidget(forwardButton);
        layout().addWidget(topBar);

        backButton.clicked.connect(signals.rewindMove);
        forwardButton.clicked.connect(signals.repeatMove);

        movesViewer.setColumnCount(2);
        ArrayList<String> headers = new ArrayList<>(2);
        headers.add("White");
        headers.add("Black");
        movesViewer.setHorizontalHeaderLabels(headers);
        movesViewer.itemClicked.connect(this, "onItemClicked(QTableWidgetItem)");
        movesViewer.itemChanged.connect(this, "onItemChanged(QTableWidgetItem)");
        movesViewer.setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection);
        layout().addWidget(movesViewer);
    }

    private void onItemClicked(QTableWidgetItem item){
        if (!item.flags().isSet(Qt.ItemFlag.ItemIsSelectable)) return;
        signals.changeCurrentPly.emit(item.row()*2 + item.column());
    }

    private void onItemChanged(QTableWidgetItem item){
        if (dontSendSignal) return;
        signals.setMove.emit(item.row()*2 + item.column(), item.text());
    }

    public void updateToArray(String[] moves){
        updateToArray(moves, -1);
    }

    public void updateToArray(String[] moves, int ply){
        dontSendSignal = true;
        movesViewer.clearContents();
        //movesViewer.clear();
        movesViewer.setRowCount((int) Math.ceil((moves.length+1)/2.0));
        QTableWidgetItem item;
        int index = 0;
        for (String move: moves){
            item = new QTableWidgetItem(move);
            movesViewer.setItem(index/2, index%2, item);
            index += 1;
        }
        item = new QTableWidgetItem(); // placeholder
        movesViewer.setItem(index/2, index%2, item);
        if (index%2 == 0){
            item = new QTableWidgetItem(); // placeholder
            movesViewer.setItem(index/2, 1, item);
            item.setFlags(Qt.ItemFlag.ItemIsEnabled);
        }
        setCurrentPly(ply==-1? index: ply);
        dontSendSignal = false;
    }

    public void setCurrentPly(int ply){
        movesViewer.setCurrentItem(movesViewer.itemAt(ply/2, ply%2));
        QModelIndex index = movesViewer.model().index(ply/2, ply%2);
        movesViewer.setCurrentIndex(index);
    }
}
