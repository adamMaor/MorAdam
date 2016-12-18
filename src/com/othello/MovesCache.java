package com.othello;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adam on 16/12/2016.
 */
public class MovesCache {

    private HashMap<ReversiBoardState, ArrayList<ReversiBoardState>> cacheMap;

    public MovesCache() {
        cacheMap = new HashMap<ReversiBoardState, ArrayList<ReversiBoardState>>();
    }

    public boolean write(ReversiBoardState state, ArrayList<ReversiBoardState> possibleMovesList) {
        return (cacheMap.put(state, possibleMovesList) != null);
    }

    public ArrayList<ReversiBoardState> read(ReversiBoardState currBoardState) {
        return cacheMap.get(currBoardState);
    }

    /**
     * needs to be called after a move was selected
     * will keep only the possible paths from the chosen move and remove all else
     * @param chosenState
     */
    public void cleanCache(ReversiBoardState chosenState){
        HashMap<ReversiBoardState, ArrayList<ReversiBoardState>> tempCacheMap = new HashMap<ReversiBoardState, ArrayList<ReversiBoardState>>();
        saveAllPossibleSubMoves(chosenState, tempCacheMap, 0);
        cacheMap.clear();
        cacheMap.putAll(tempCacheMap);

    }

    private void saveAllPossibleSubMoves(ReversiBoardState chosenState, HashMap<ReversiBoardState, ArrayList<ReversiBoardState>> tempCacheMap, int currentDepth) {
        if (currentDepth < ReversiConstants.Performance.maxCacheSize) {
            ArrayList<ReversiBoardState> chosenPossibleMoves = cacheMap.get(chosenState);
            if (chosenPossibleMoves != null) {
                tempCacheMap.put(chosenState, chosenPossibleMoves);
                for (ReversiBoardState possibleMove : chosenPossibleMoves) {
                    saveAllPossibleSubMoves(possibleMove, tempCacheMap, ++currentDepth);
                }
            }
        }
    }
}
