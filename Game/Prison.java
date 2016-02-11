package Game;

public class Prison extends ExtraBoard{
    public Prison(Color color){
        super(color);
    }
    protected void setupSquares(){

    }

    public Square getEmptyCell(){
        for (int row=0; row<8; row++){
            for (int column=0; column<8; column++){
                if (getPiece(new int[] {row, column}) == null) {
                    return getSquare(new int[] {row, column});
                }
            }
        }
        return null;
    }
}
