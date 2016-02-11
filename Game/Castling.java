package Game;

public class Castling extends Move{
    public final Value side;

    public Castling(Color player, StandardBoard board, Value side){
        super(player, board);
        this.side = side;
    }
}
