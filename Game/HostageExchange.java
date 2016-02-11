package Game;

public class HostageExchange extends Move{
    public final Value hostage;
    public final Square square;
    public HostageExchange(Color player, Prison board, Value hostage, Square square){
        super(player, board);
        this.hostage = hostage;
        this.square = square;
    }
}
