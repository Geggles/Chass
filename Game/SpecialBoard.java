package Game;

/**
 * Used for board Gamma
 * */
public class SpecialBoard extends PlayBoard{
    public SpecialBoard() {

    }
    public Square getSquareAt(int[] coordinates){
        int height = coordinates[0] % 8;
        int width = coordinates[1] % 8;
        return super.getSquareAt(new int[] {height, width});
    }
}
