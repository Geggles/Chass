package Game;

public class Swap extends Move{
    public final PlayBoard[] constellationBefore;
    public final PlayBoard[] constellationAfter;
    public final int[] coordinates;
    public Swap(Color player,
                int[] coordinates,
                PlayBoard[] constellationBefore,
                PlayBoard[] constellationAfter){
        super(player);
        this.constellationBefore = constellationBefore;
        this.constellationAfter = constellationAfter;
        this.coordinates = coordinates;
    }
}
