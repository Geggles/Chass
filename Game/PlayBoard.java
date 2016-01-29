package Game;

/**
* Used for Boards Alpha, Beta and Gamma
* */
public abstract class PlayBoard extends Board{
    abstract void move(Square source, Square destination);
    abstract boolean validMove(Square source, Square destination);
    abstract void capture(Square source, Square destination);
}
