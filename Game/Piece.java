package Game;

import Shared.Color;
import Shared.Value;

public class Piece {
    private Color color;
    public final Value value;
    private boolean canSwitchBoards = true;

    public Piece(Value value, Color color){
        this.value = value;
        this.color = color;
        if (value == Value.KING) canSwitchBoards = false;
    }

    public void setCanSwitchBoards(boolean state){
        canSwitchBoards = state;
    }

    public boolean getCanSwitchBoards(){
        return canSwitchBoards;
    }

    public Color getColor(){
        return color;
    }

    public void switchColor(){
        color = color.opposite();
    }
}
