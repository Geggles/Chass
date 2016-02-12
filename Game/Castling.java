package Game;

public class Castling extends Move{
    public final Value side;

    public Castling(Color player,
                    State state,
                    Value side){
        super(player, state);
        this.side = side;
    }
}
