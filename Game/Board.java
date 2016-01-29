package Game;

import com.google.common.collect.HashBiMap;

public abstract class Board {
    HashBiMap<Square, Piece> state = HashBiMap.create(32);
    Square[][] squares;
}
