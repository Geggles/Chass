package Game;

public class Prison extends ExtraBoard{
    public Prison(String name, Color color){
        super(name, color);
    }
    protected void setupSquares(){

    }

    public Square getEmptyCell(){
        for (int row=0; row<8; row++){
            for (int column=0; column<8; column++){
                if (getPieceOn(new int[] {row, column}) == null) {
                    return getSquareAt(new int[] {row, column});
                }
            }
        }
        return null;
    }
}
