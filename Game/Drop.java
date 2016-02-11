package Game;

public class Drop extends Move{
    public final int[] coordinates;
    public Drop(Color player, int[] coordinates){
        super(player);
        this.coordinates = coordinates;
    }
}
