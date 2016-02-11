package Game;

public class EnPassant extends Move{
    public final int column;
    public EnPassant(Color player, StandardBoard board, int column){
        super(player, board);
        this.column = column;
    }
}
