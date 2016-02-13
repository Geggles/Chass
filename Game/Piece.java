package Game;

public class Piece {
    private Color color;
    private boolean canNullMove = true;
    private boolean canCastle = true;
    // pawns can not promote anymore once they have been to prison
    private boolean canPromote = true;
    // pawns can move 2 squares only if they have not moved at all yet
    private boolean canBoost = true;
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

    public void prohibitCastling(){
        canCastle = false;
    }

    public boolean getNullMoveAbility(){
        return canNullMove;
    }

    public void setNullMoveAbility(boolean value){
        canNullMove = value;
    }

    public boolean getPromotionAbility(){
        return canPromote;
    }

    public void prohibitPromotion(){
        canPromote = false;
    }

    public boolean getBoostAbility(){
        return canPromote;
    }

    public void prohibitBoost(){
        canPromote = false;
    }
}
