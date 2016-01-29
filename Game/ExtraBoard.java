package Game;

/**
 * Used for Prisons and Airfields
 */
public abstract class ExtraBoard extends Board{
    public ExtraBoard(){
        squares = new Square[8][4];
    }
}
