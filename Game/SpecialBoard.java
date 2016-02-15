package Game;

/**
 * Used for board Gamma
 * */
public class SpecialBoard extends Board{
    public SpecialBoard() {
        super(Color.NONE);
    }
    public Square getSquare(int[] coordinates){
        int height = coordinates[0] % 8;
        int width = coordinates[1] % 8;
        return super.getSquare(new int[] {height, width});
    }
}
