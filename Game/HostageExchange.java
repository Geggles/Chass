package Game;

public class HostageExchange extends Move{
    public final Value hostage;
    public final Square square;
    public HostageExchange(Color player, Value hostage, Square square){
        super(player, null);
        this.hostage = hostage;
        this.square = square;
    }
}
