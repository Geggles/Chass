package Game;

/*import com.sun.istack.internal.Nullable;*/

public abstract class Move {
    public final Color player;
    public State state;
    public Move(Color player){
        this.player = player;
    }
/*    public final MoveType moveType;
    public final Square source;
    public final Square destination;
    public final PlayBoard destinationBoardPlayer;
    public final PlayBoard destinationBoardOpponent;
    public final Piece sourcePiece;
    public final Piece destinationPiece;
    public State state = State.NONE;

    public Move(MoveType moveType,
                Square source,
                Square destination,
                PlayBoard destinationBoardPlayer,
                @Nullable PlayBoard destinationBoardOpponent,
                Piece sourcePiece,
                @Nullable Piece destinationPiece) {

        this.moveType = moveType;
        this.source = source;
        this.destination = destination;
        this.destinationBoardPlayer = destinationBoardPlayer;
        this.destinationBoardOpponent= destinationBoardOpponent;
        this.sourcePiece = sourcePiece;
        this.destinationPiece = destinationPiece;
    }*/
}
