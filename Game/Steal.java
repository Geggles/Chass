package Game;

public class Steal extends Move{
    public final int[] sourceCoordinates;
    public final int[] destinationCoordinates;
    public final PlayBoard destinationBoardPlayer;
    public final PlayBoard destinationBoardOpponent;

    public Steal(Color player,
                 State state,
                 int[] sourceCoordinates,
                 int[] destinationCoordinates,
                 PlayBoard destinationBoardPlayer,
                 PlayBoard destinationBoardOpponent){
        super(player, state);
        this.sourceCoordinates = sourceCoordinates;
        this.destinationCoordinates = destinationCoordinates;
        this.destinationBoardPlayer = destinationBoardPlayer;
        this.destinationBoardOpponent = destinationBoardOpponent;
    }
}
