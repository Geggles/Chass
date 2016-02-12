package Game;

public class Drop extends Move{
    public final int[] coordinates;
    public Drop(Color player,
                State state,
                int[] coordinates){
        super(player, state);
        this.coordinates = coordinates;
    }
}
