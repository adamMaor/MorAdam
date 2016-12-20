package com.othello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Adam on 15/12/2016.
 */
public class MiniMaxLogic {
    private LogicUtils utils;
    private ReversiBoardState initialState;
    private ArrayList<ReversiBoardState> nextAvailableMovesList;
    private MovesCache movesCache;
    boolean bIsBlackMax; // this will be initialized and is for player(s)
    int nInitDepth;
    private HashMap<String,Boolean> blackPlayerHeuristicsMap;
    private HashMap<String,Boolean> whitePlayerHeuristicsMap;

    public MiniMaxLogic(LogicUtils utils, ReversiBoardState initialState, int nInitDepth, ArrayList<ReversiBoardState> nextAvailableMovesList, MovesCache movesCache,
                        HashMap<String,Boolean> blackPlayerHeuristicsMap, HashMap<String,Boolean> whitePlayerHeuristicsMap) {
        this.initialState = initialState;
        this.nextAvailableMovesList = nextAvailableMovesList;
        this.nInitDepth = nInitDepth;
        this.utils = utils;
        this.movesCache = movesCache;
        if (nInitDepth > ReversiConstants.Performance.maxCacheDepth) { // limit cache to depth 7
            this.movesCache = null;
        }
        this.bIsBlackMax = initialState.bIsBlackMove;
        this.blackPlayerHeuristicsMap = blackPlayerHeuristicsMap;
        this.whitePlayerHeuristicsMap = whitePlayerHeuristicsMap;
    }

    public ReversiBoardState launchMiniMax(boolean isAlphaBeta) {
        ReversiBoardState bestBoard;
        if (isAlphaBeta) {
            Integer alpha = new Integer(Integer.MIN_VALUE);
            Integer beta = new Integer(Integer.MAX_VALUE);
            bestBoard = alphaBetaMiniMax(initialState, nInitDepth, alpha, beta);
        }
        else {
            bestBoard = simpleMiniMax(initialState, nInitDepth);
        }
        return bestBoard;
    }

    private ReversiBoardState alphaBetaMiniMax(ReversiBoardState currentState, int nDepth, Integer alpha, Integer beta) {
        ReversiBoardState nextState = null;
        if (nDepth == 0) {
            return currentState;
        }
        if (nextAvailableMovesList.size() > 0) {
            // we know that first player is Max
            int bestScore = new Integer(Integer.MIN_VALUE);
            Iterator<ReversiBoardState> stateIterator = nextAvailableMovesList.iterator();
            while (stateIterator.hasNext()) {
                ReversiBoardState state = stateIterator.next();
                int currScore = alphaBetaMiniMaxScorer(state, nDepth - 1, alpha, beta, false);
                if (currScore > bestScore) {
                    bestScore = currScore;
                    nextState = state;
                    if (bestScore == ReversiConstants.HeuristicsWeight.maxUtilityScore) break;
                    alpha = Math.max(alpha, bestScore); // cutoff will never happen in first level
                }
                if (movesCache != null) {
                    final ReversiBoardState finalNextState = nextState;
                    Thread cleaner = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            movesCache.cleanCache(finalNextState);
                        }
                    });
                    cleaner.start();
                }
            }
        }
        return nextState;
    }

    private int alphaBetaMiniMaxScorer(ReversiBoardState currentState, int nDepth, Integer alpha, Integer beta, boolean bIsCurrentMax) {
        if (nDepth <= 0) {
            return utility(currentState);
        }
        ArrayList<ReversiBoardState> allPossibleMoves = (movesCache != null) ?  utils.allResultsCached(currentState, nInitDepth - nDepth) : utils.allResults(currentState);
        int bestScore;
        if (allPossibleMoves.size() > 0) {
            if (bIsCurrentMax) {  // current player is Max
                bestScore = Integer.MIN_VALUE;
                for (ReversiBoardState state : allPossibleMoves) {
                    int currScore = alphaBetaMiniMaxScorer(state, nDepth - 1, alpha, beta, false);
                    if (currScore > bestScore) {
                        bestScore = currScore;   // next level is Min
                        alpha = Math.max(alpha, bestScore);
                        if (alpha >= beta || (bestScore == ReversiConstants.HeuristicsWeight.maxUtilityScore)) {    // This is the Cutoff - no need to look at further moves -
                            break;              // we now that whatever this maximizer will return will be greater or equal to what the minimizer in the upper level will take.
                        }
                    }
                    else if (movesCache != null) {
                        movesCache.removeSingle(state);
                    }
                }
            }
            else {   // current player is Min
                bestScore = Integer.MAX_VALUE;
                for (ReversiBoardState state : allPossibleMoves) {
                    int currScore = alphaBetaMiniMaxScorer(state, nDepth - 1, alpha, beta, true);
                    if (currScore < bestScore) {
                        bestScore = currScore; // next level is Max
                        beta = Math.min(beta, bestScore);
                        if (alpha >= beta || (bestScore == ReversiConstants.HeuristicsWeight.minUtilityScore) ) {    // Same as with maximizer only with min.
                            break;
                        }
                    }
                    else if (movesCache != null) {
                        movesCache.removeSingle(state);
                    }
                }
            }
        }
        else  { // no legal moves for this player - change player and return the next score
            ReversiBoardState newState = new ReversiBoardState(currentState.boardStateBeforeMove, !currentState.bIsBlackMove);
            return alphaBetaMiniMaxScorer(newState, nDepth - 1, alpha, beta, !bIsCurrentMax);
        }
        return bestScore;
    }

    private ReversiBoardState simpleMiniMax(ReversiBoardState currentState, int nDepth) {
        ReversiBoardState nextState = null;
        if (nDepth == 0) {
            return currentState;
        }
        if (nextAvailableMovesList.size() > 0) {
            int bestScore = Integer.MIN_VALUE;   // we know that first player is Max
            Iterator<ReversiBoardState> stateIterator = nextAvailableMovesList.iterator();
            while (stateIterator.hasNext()) {
                ReversiBoardState state = stateIterator.next();
                int currScore = simpleMiniMaxScorer(state, nDepth - 1, false);
                if (currScore > bestScore) {
                    bestScore = currScore;
                    nextState = state;
                    if (bestScore == ReversiConstants.HeuristicsWeight.maxUtilityScore) break;
                    if (movesCache != null) {
                        final ReversiBoardState finalNextState = nextState;
                        Thread cleaner = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                movesCache.cleanCache(finalNextState);
                            }
                        });
                        cleaner.start();
                    }
                }
            }
        }
        return nextState;
    }

    private int simpleMiniMaxScorer(ReversiBoardState currentState, int nDepth, boolean bIsCurrentMax) {
        if (nDepth == 0) {
            return utility(currentState);
        }
        ArrayList<ReversiBoardState> allPossibleMoves = (movesCache != null) ?  utils.allResultsCached(currentState, nInitDepth - nDepth) : utils.allResults(currentState);
        int bestScore;
        if (allPossibleMoves.size() > 0) {
            if (bIsCurrentMax) {     // current player is Max
                bestScore = Integer.MIN_VALUE;
                for (ReversiBoardState state : allPossibleMoves) { // next level is Min
                    int currScore = simpleMiniMaxScorer(state, nDepth - 1, false);
                    if (currScore > bestScore) {
                        bestScore = currScore;
                        if (bestScore == ReversiConstants.HeuristicsWeight.maxUtilityScore) break;
                    }
                    else if (movesCache != null) {
                        movesCache.removeSingle(state);
                    }
                }
            }
            else {                  // current player is Min
                bestScore = Integer.MAX_VALUE;
                for (ReversiBoardState state : allPossibleMoves) { // next level is Max
                    int currScore = simpleMiniMaxScorer(state, nDepth - 1, true);
                    if(currScore < bestScore) {
                        bestScore = currScore;
                        if (bestScore == ReversiConstants.HeuristicsWeight.minUtilityScore) break;
                    }
                    else if (movesCache != null) {
                        movesCache.removeSingle(state);
                    }
                }
            }
        }
        else  { // no legal moves for this player - change player and return the next score
            ReversiBoardState newState = new ReversiBoardState(currentState.boardStateBeforeMove, !currentState.bIsBlackMove);
            return simpleMiniMaxScorer(newState, nDepth - 1 , !bIsCurrentMax);
        }
        return bestScore;
    }

    private int utility(ReversiBoardState currentState) {
        int result = 0;
        HashMap<String, Boolean> heuristicSelectionMap = bIsBlackMax ? blackPlayerHeuristicsMap : whitePlayerHeuristicsMap;
        // each heuristics will return a number between -50 and 50 where 0 is neutral, -50 is really bad for MAX player, 50 is really good for MAX.
        int h1Res = h1(currentState);
        // corner case - if h1 (disc count) result is equal to 50 or -50 that means that the game is over with a total win or loose - no need to check other heuristics return min/max score
        if (h1Res == 50) result = ReversiConstants.HeuristicsWeight.maxUtilityScore;
        else if (h1Res == -50) result = ReversiConstants.HeuristicsWeight.minUtilityScore;
        else {
            result += heuristicSelectionMap.get("h1") ? ReversiConstants.HeuristicsWeight.h1 *  h1Res: 0;
            result += heuristicSelectionMap.get("h2") ? ReversiConstants.HeuristicsWeight.h2 * h2(currentState) : 0;
            result += heuristicSelectionMap.get("h3") ? ReversiConstants.HeuristicsWeight.h3 * h3(currentState) : 0;
            result += heuristicSelectionMap.get("h4") ? ReversiConstants.HeuristicsWeight.h4 * h4(currentState) : 0;
            result += heuristicSelectionMap.get("h5") ? ReversiConstants.HeuristicsWeight.h5 * h5(currentState) : 0;
        }
        return result;
    }

    /**
     * this is the most basic anf obvious heuristic - diff in disks count
      * @param currentState
     * @return
     */
    private int h1(ReversiBoardState currentState) {
        int whiteCount = 0, blackCount = 0;
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row++) {
            for (int col = 0; col < ReversiConstants.BoardSize.boardWidth ; col++) {
                byte currCubeState = currentState.boardStateBeforeMove[row][col];
                if (currCubeState != ReversiConstants.CubeStates.none) {
                    if (currCubeState == ReversiConstants.CubeStates.white){
                        whiteCount++;
                    }
                    else blackCount++;
                }
            }
        }
        if (bIsBlackMax)
            return (int) (100 * (float)blackCount/(blackCount + whiteCount)) -50;
        else
            return (int) (100 * (float)whiteCount/(whiteCount + blackCount)) -50;
    }

    /**
     * this is corner heuristic - add score for corner
     * @param currentState
     * @return
     */
    private int h2(ReversiBoardState currentState) {
        int whiteCorner = 0;
        int blackCorner = 0;

        byte currCorner = currentState.boardStateBeforeMove[0][0];
        if (currCorner != ReversiConstants.CubeStates.none) {
            if (currCorner == ReversiConstants.CubeStates.black)
                blackCorner++;
            else whiteCorner++;
        }
        currCorner = currentState.boardStateBeforeMove[0][ReversiConstants.BoardSize.boardWidth - 1];
        if (currCorner != ReversiConstants.CubeStates.none) {
            if (currCorner == ReversiConstants.CubeStates.black)
                blackCorner++;
            else whiteCorner++;
        }
        currCorner = currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 1][0];
        if (currCorner != ReversiConstants.CubeStates.none) {
            if (currCorner == ReversiConstants.CubeStates.black)
                blackCorner++;
            else whiteCorner++;
        }
        currCorner = currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 1][ReversiConstants.BoardSize.boardWidth - 1];
        if (currCorner != ReversiConstants.CubeStates.none) {
            if (currCorner == ReversiConstants.CubeStates.black)
                blackCorner++;
            else whiteCorner++;
        }

        if (bIsBlackMax) {
            return (25 * (blackCorner - whiteCorner)) / 2;
        } else {
            return (25 * (whiteCorner - blackCorner)) / 2;
        }
    }

    /**
     * this is number of stable discs heuristic
     * @param currentState
     * @return
     */
    private int h3(ReversiBoardState currentState) {
        int blackStableDiscs = stabilityCheck(currentState, ReversiConstants.CubeStates.black);
        int whiteStableDiscs = stabilityCheck(currentState, ReversiConstants.CubeStates.white);
        int sum = blackStableDiscs + whiteStableDiscs;
        if (sum == 0 ) return 0;
        if (bIsBlackMax) {
            return (int) (100 * (float) blackStableDiscs / sum) -50;
        } else {
            return (int) (100 * (float) whiteStableDiscs / sum) -50 ;
        }
    }

    private int stabilityCheck(ReversiBoardState currentState, byte playerToCheck) {
        boolean[][] stableDiscs = new boolean[ReversiConstants.BoardSize.boardHeight][ReversiConstants.BoardSize.boardWidth];
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row++) {
            for (int col = 0; col < ReversiConstants.BoardSize.boardWidth; col++) {
                stableDiscs[row][col] = false;
            }
        }
        checkCornerStability(currentState, stableDiscs, 0, 0, 1, 1, ReversiConstants.BoardSize.boardWidth, ReversiConstants.BoardSize.boardHeight, playerToCheck);
        checkCornerStability(currentState, stableDiscs, 0, ReversiConstants.BoardSize.boardWidth - 1, -1, 1, 0, ReversiConstants.BoardSize.boardHeight, playerToCheck);
        checkCornerStability(currentState, stableDiscs, ReversiConstants.BoardSize.boardHeight - 1, ReversiConstants.BoardSize.boardWidth - 1, -1, -1, 0, 0, playerToCheck);
        checkCornerStability(currentState, stableDiscs, ReversiConstants.BoardSize.boardHeight - 1, 0, 1, -1, ReversiConstants.BoardSize.boardWidth, 0, playerToCheck);
        int resCounter = 0;
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row++) {
            for (int col = 0; col < ReversiConstants.BoardSize.boardWidth; col++) {
                resCounter += stableDiscs[row][col] ? 1 : 0;
            }
        }
        return resCounter;
    }

    private void checkCornerStability(ReversiBoardState currentState, boolean[][] stableDiscs, int x, int y, int xDir, int yDir, int xBoarder, int yBoarder, byte playerToCheck) {
        int yRunner = y, xRunner = x;
        while (yRunner != yBoarder && currentState.boardStateBeforeMove[yRunner][y] == playerToCheck) {
            stableDiscs[yRunner][y] = true;
            yRunner += yDir;
        }
        yRunner -= yDir;
        while (xRunner != xBoarder && currentState.boardStateBeforeMove[x][xRunner] == playerToCheck) {
            stableDiscs[x][xRunner] = true;
            xRunner += xDir;
        }
        xRunner -= xDir;
        if (yRunner > y + 1 && xRunner > x +1) {
            checkCornerStability(currentState, stableDiscs, x + xDir, y + yDir, xDir, yDir, xRunner, yRunner, playerToCheck);
        }
    }

    /**
     * this is number of frontiers heuristic
     * @param currentState
     * @return
     */
    private int h4(ReversiBoardState currentState) {
        int blackFrontiers = 0;
        int whiteFrontiers = 0;
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth; j++) {
                byte currentCubeState = currentState.boardStateBeforeMove[i][j];
                if (currentCubeState != ReversiConstants.CubeStates.none) {
                    for (int horInterval = -1; horInterval < 2; horInterval++) {
                        for (int verInterval = -1; verInterval < 2; verInterval++) {
                            int row = i + horInterval;
                            if (row >= ReversiConstants.BoardSize.boardHeight || row < 0) {
                                continue;
                            }
                            int col = j + verInterval;
                            if (col >= ReversiConstants.BoardSize.boardWidth || col < 0) {
                                continue;
                            }
                            if (currentState.boardStateBeforeMove[row][col] == ReversiConstants.CubeStates.none) {
                                if (currentCubeState == ReversiConstants.CubeStates.black) {
                                    blackFrontiers++;
                                } else {
                                    whiteFrontiers++;
                                }
                            }
                        }
                    }
                }
            }
        }
        if(bIsBlackMax){ // sum can never be 0
            return (int) (100 * (float)whiteFrontiers/(whiteFrontiers + blackFrontiers)) - 50 ;
        } else {
            return (int) (100 * (float)blackFrontiers/(whiteFrontiers + blackFrontiers)) - 50;
        }
    }

    /**
     * this is number of corner neighbours heuristic
     * @param currentState
     * @return
     */
    private int h5(ReversiBoardState currentState) {
        int blackNeighbours = 0;
        int whiteNeighbours = 0;
        // neighbours of corner 0,0
        if (currentState.boardStateBeforeMove[0][0] == ReversiConstants.CubeStates.none) {
            if (currentState.boardStateBeforeMove[0][1] != ReversiConstants.CubeStates.none) {
                if(currentState.boardStateBeforeMove[0][1] == ReversiConstants.CubeStates.black){
                    blackNeighbours++;
                }else{
                    whiteNeighbours++;
                }
            }
            if (currentState.boardStateBeforeMove[1][1] != ReversiConstants.CubeStates.none){
                if (currentState.boardStateBeforeMove[1][1] == ReversiConstants.CubeStates.black){
                    blackNeighbours++;
                }else {
                    whiteNeighbours++;
                }
            }
            if(currentState.boardStateBeforeMove[1][0] != ReversiConstants.CubeStates.none) {
                if (currentState.boardStateBeforeMove[1][0] == ReversiConstants.CubeStates.black) {
                    blackNeighbours++;
                } else {
                    whiteNeighbours++;
                }
            }
        }

        // neighbours of corner 0,11
        if (currentState.boardStateBeforeMove[0][ReversiConstants.BoardSize.boardWidth - 1] == ReversiConstants.CubeStates.none) {
            if (currentState.boardStateBeforeMove[0][ReversiConstants.BoardSize.boardWidth - 2] != ReversiConstants.CubeStates.none) {
                if(currentState.boardStateBeforeMove[0][ReversiConstants.BoardSize.boardWidth - 2] == ReversiConstants.CubeStates.black){
                    blackNeighbours++;
                }else{
                    whiteNeighbours++;
                }
            }
            if (currentState.boardStateBeforeMove[1][ReversiConstants.BoardSize.boardWidth - 2] != ReversiConstants.CubeStates.none){
                if (currentState.boardStateBeforeMove[1][ReversiConstants.BoardSize.boardWidth - 2] == ReversiConstants.CubeStates.black){
                    blackNeighbours++;
                }else {
                    whiteNeighbours++;
                }
            }
            if(currentState.boardStateBeforeMove[1][ReversiConstants.BoardSize.boardWidth - 1] != ReversiConstants.CubeStates.none) {
                if (currentState.boardStateBeforeMove[1][ReversiConstants.BoardSize.boardWidth - 1] == ReversiConstants.CubeStates.black) {
                    blackNeighbours++;
                } else {
                    whiteNeighbours++;
                }
            }
        }

        // neighbours of corner 11,0
        if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 1][0] == ReversiConstants.CubeStates.none) {
            if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 1][1] != ReversiConstants.CubeStates.none) {
                if(currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 1][1] == ReversiConstants.CubeStates.black){
                    blackNeighbours++;
                }else{
                    whiteNeighbours++;
                }
            }
            if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 2][0] != ReversiConstants.CubeStates.none){
                if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 2][0] == ReversiConstants.CubeStates.black){
                    blackNeighbours++;
                }else {
                    whiteNeighbours++;
                }
            }
            if(currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 2][1] != ReversiConstants.CubeStates.none) {
                if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 2][1] == ReversiConstants.CubeStates.black) {
                    blackNeighbours++;
                } else {
                    whiteNeighbours++;
                }
            }
        }

        // neighbours of corner 11,11
        if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 1][ReversiConstants.BoardSize.boardWidth - 1] == ReversiConstants.CubeStates.none) {
            if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 1][ReversiConstants.BoardSize.boardWidth - 2] != ReversiConstants.CubeStates.none) {
                if(currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 1][ReversiConstants.BoardSize.boardWidth - 2] == ReversiConstants.CubeStates.black){
                    blackNeighbours++;
                }else{
                    whiteNeighbours++;
                }
            }
            if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 2][ReversiConstants.BoardSize.boardWidth - 2] != ReversiConstants.CubeStates.none){
                if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 2][ReversiConstants.BoardSize.boardWidth - 2] == ReversiConstants.CubeStates.black){
                    blackNeighbours++;
                }else {
                    whiteNeighbours++;
                }
            }
            if(currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 2][ReversiConstants.BoardSize.boardWidth - 1] != ReversiConstants.CubeStates.none) {
                if (currentState.boardStateBeforeMove[ReversiConstants.BoardSize.boardHeight - 2][ReversiConstants.BoardSize.boardWidth - 1] == ReversiConstants.CubeStates.black) {
                    blackNeighbours++;
                } else {
                    whiteNeighbours++;
                }
            }
        }
        // there are 12 cubes we checked
        int sum = blackNeighbours + whiteNeighbours;
        if (sum == 0) return 0;
        float multiplier = 50 / 12;
        if(bIsBlackMax){
            return (int) (multiplier * (whiteNeighbours - blackNeighbours));
        } else {
            return (int) (multiplier * (blackNeighbours - whiteNeighbours));
        }
    }
}
