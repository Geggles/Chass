package Game;

import com.google.common.collect.HashBiMap;

import java.util.HashMap;

/**
 * this class will control the flow of the game
 * */
public class Controller {
    StandardBoard alpha;
    StandardBoard beta;
    SpecialBoard gamma;
    HashMap<Colors, Prison> prisons;
    HashMap<Colors, Airfield> airfields;

    public Controller() {
        alpha = new StandardBoard();
        beta = new StandardBoard();
        gamma = new SpecialBoard();
        prisons = new HashMap<>(2);
        airfields = new HashMap<>(2);

        SetupPieces();
    }

    private void SetupPieces(){

    }
}
