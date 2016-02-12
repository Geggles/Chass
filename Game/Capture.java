package Game;

public class Capture extends Move{
    public final Square source;
    public final Square destination;
    public final PlayBoard destinationBoard;
    public Capture(Color player,
                   State state,
                   Square source,
                   Square destination,
                   PlayBoard destinationBoard){
        super(player, state);
        this.source = source;
        this.destination = destination;
        this.destinationBoard = destinationBoard;
    }
}
