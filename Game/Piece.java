package Game;

public class Piece {
    private Color color;
    private boolean canNullMove = true;
    private boolean canCastle = true;
    public final Value value;

    public Piece(Value value, Color color){
        this.value = value;
        this.color = color;
        if (value == Value.KING){
            canNullMove = false;
        }
    }

    public Color getColor(){
        return color;
    }

    public void switchColor(){
        color = color.opposite();
    }

    public boolean getCastleAbility(){
        return canCastle;
    }

    public void cannotCastleAnymore(){
        canCastle = false;
    }

    public boolean getNullMoveAbility(){
        return canNullMove;
    }

    public void setNullMoveAbility(boolean value){
        canNullMove = value;
    }
}
