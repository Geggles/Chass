package Game;

public class EnPassant extends Move{
    public final int column;
    public final StandardBoard sourceBoard;
    public final StandardBoard destinationBoard;
    public EnPassant(Color player,
                     State state,
                     StandardBoard sourceBoard,
                     StandardBoard destinationBoard,
                     int column){
        super(player, state);
        this.sourceBoard = sourceBoard;
        this.destinationBoard = destinationBoard;
        this.column = column;
    }
}
