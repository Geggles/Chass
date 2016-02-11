package Game;

public class HostageExchange extends Move{
    public final Value hostage;
    public final int[] coordinates;
    public HostageExchange(Color player, Value hostage, int[] coordinates){
        super(player, null);
        this.hostage = hostage;
        this.coordinates = coordinates;
    }
}
