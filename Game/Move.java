package Game;

/*import com.sun.istack.internal.Nullable;*/

public abstract class Move {
    public final Color player;
    public final State state;

    public Move(Color player, State state) {
        this.player = player;
        this.state = state;
    }
}