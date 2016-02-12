package Game;

public class HostageExchange extends Move{
    public final Value hostage;
    public final int[] coordinates;
    public HostageExchange(Color player,
                           State state,
                           Value hostage,
                           int[] coordinates){
        super(player, state);
        this.hostage = hostage;
        this.coordinates = coordinates;
    }
}
