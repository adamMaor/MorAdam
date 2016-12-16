package com.othello;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adam on 16/12/2016.
 */
public class MovesCache {

    private HashMap<ReversiBoardState, ArrayList<ReversiBoardState>> cacheMap;
    private HashMap<ReversiBoardState, Long> lruMap;

    public MovesCache() {
        cacheMap = new HashMap<ReversiBoardState, ArrayList<ReversiBoardState>>();
        lruMap = new HashMap<ReversiBoardState, Long>();
    }

    public boolean write(ReversiBoardState state, ArrayList<ReversiBoardState> possibleMovesList) {
        if (cacheMap.put(state, possibleMovesList) != null) {
            return true;
        }
        return false;
    }

    public ArrayList<ReversiBoardState> read(ReversiBoardState currBoardState) {
        return cacheMap.get(currBoardState);
    }

    public void cleanCache(){
        // will use LRU to clean val from Map

    }
}
