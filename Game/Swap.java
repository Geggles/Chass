package Game;

public class Swap extends Move{
    public final PlayBoard[] constellationBefore;
    public final PlayBoard[] constellationAfter;
    public final int[] coordinates;
    public Swap(Color player,
                State state,
                int[] coordinates,
                PlayBoard[] constellationBefore,
                PlayBoard[] constellationAfter){
        super(player, state);
        this.constellationBefore = constellationBefore;
        this.constellationAfter = constellationAfter;
        this.coordinates = coordinates;
    }
}
