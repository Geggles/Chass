package Game;

import com.google.common.collect.HashBiMap;

public abstract class Board {
    private HashBiMap<Square, Piece> state = HashBiMap.create(32);
}
