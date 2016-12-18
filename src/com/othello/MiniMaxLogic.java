package com.othello;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adam on 15/12/2016.
 */
public class MiniMaxLogic {

    private ReversiBoardState initialState;
    private ArrayList<ReversiBoardState> nextAvailableMovesList;
    private MovesCache movesCache;
    boolean bIsBlackMax; // this will be initialized and is for player(s)
    int nInitDepth;
    int hits;
    int misses;
    private HashMap<String,Boolean> blackPlayerHeuristicsMap;
    private HashMap<String,Boolean> whitePlayerHeuristicsMap;

    public MiniMaxLogic(ReversiBoardState initialState, int nInitDepth, ArrayList<ReversiBoardState> nextAvailableMovesList, MovesCache movesCache,
                        HashMap<String,Boolean> blackPlayerHeuristicsMap, HashMap<String,Boolean> whitePlayerHeuristicsMap) {
        this.initialState = initialState;
        this.nextAvailableMovesList = nextAvailableMovesList;
        this.movesCache = movesCache;
        this.bIsBlackMax = initialState.bIsBlackMove;
        this.nInitDepth = nInitDepth;
        this.blackPlayerHeuristicsMap = blackPlayerHeuristicsMap;
        this.whitePlayerHeuristicsMap = whitePlayerHeuristicsMap;
        hits = misses = 0;
//        movesCache.write(initialState,nextAvailableMovesList);
//        System.out.println("Minimax init - available moves = " + nextAvailableMovesList.size() + ", Max is black ? " + bIsBlackMax + ", Depth is: " + nInitDepth);
    }

    public ReversiBoardState launchMiniMax(boolean isAlphaBeta)
    {
        final ReversiBoardState bestBoard;
        if (isAlphaBeta) {
            bestBoard = alphaBetaMiniMax(initialState, nInitDepth);
        }
        else {
            bestBoard = simpleMiniMax(initialState, nInitDepth);
        }
        if (movesCache != null) {
            Thread cleaner = new Thread(new Runnable() {
                @Override
                public void run() {
                    movesCache.cleanCache(bestBoard);
                }
            });
            cleaner.start();


//          System.out.println("...Done");
        }
        return bestBoard;
    }

    public ReversiBoardState alphaBetaMiniMax(ReversiBoardState currentState, int nDepth) {
        ReversiBoardState nextState = null;
        if (nDepth == 0) {
            return currentState;
        }
        if (nextAvailableMovesList.size() > 0) {
            Integer alpha = Integer.MIN_VALUE;
            Integer beta = Integer.MAX_VALUE;
            // we know that first player is Max
            int bestScore = Integer.MIN_VALUE;
            for (ReversiBoardState state : nextAvailableMovesList) {
                // next level is Min
                int currScore = alphaBetaMiniMaxScorer(state, nDepth - 1, alpha, beta, false);
                if (currScore > bestScore) {
                    bestScore = currScore;
                    nextState = state;
                    alpha = Math.max(alpha, bestScore);
                    if (alpha >= beta) {    // This is the Cutoff - no need to look at further moves -
                        break;              // we now that whatever this maximizer will return will be greater or equal to what the minimizer in the upper level will take.
                    }
                }
            }
        }
        return nextState;
    }

    private int alphaBetaMiniMaxScorer (ReversiBoardState currentState, int nDepth, Integer alpha, Integer beta, boolean bIsCurrentMax) {
        if (nDepth == 0) {
            return utility(currentState);
        }
        ArrayList<ReversiBoardState> allPossibleMoves = (movesCache != null) ?  allResultsCached(currentState, nInitDepth - nDepth) : allResults(currentState);
        int bestScore;
        if (allPossibleMoves.size() > 0) {
            if (bIsCurrentMax) {  // current player is Max
                bestScore = Integer.MIN_VALUE;
                for (ReversiBoardState state : allPossibleMoves) {
                    bestScore = Math.max(bestScore, alphaBetaMiniMaxScorer(state, nDepth - 1, alpha, beta, false));   // next level is Min
                    alpha = Math.max(alpha, bestScore);
                    if (alpha >= beta) {    // This is the Cutoff - no need to look at further moves -
                        break;              // we now that whatever this maximizer will return will be greater or equal to what the minimizer in the upper level will take.
                    }
                }
            }
            else {   // current player is Min
                bestScore = Integer.MAX_VALUE;
                for (ReversiBoardState state : allPossibleMoves) {
                    bestScore = Math.min(bestScore, alphaBetaMiniMaxScorer(state, nDepth - 1, alpha, beta, true)); // next level is Max
                    beta = Math.min(beta, bestScore);
                    if (alpha >= beta) {    // Same as with maximizer only with min.
                        break;
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
            for (ReversiBoardState state : nextAvailableMovesList) { // next level is Min
                int currScore = simpleMiniMaxScorer(state, nDepth - 1, false);
                if (currScore > bestScore) {
                    bestScore = currScore;
                    nextState = state;
                }
            }
        }
        return nextState;
    }

    private int simpleMiniMaxScorer (ReversiBoardState currentState, int nDepth, boolean bIsCurrentMax) {
        if (nDepth == 0) {
            return utility(currentState);
        }
        ArrayList<ReversiBoardState> allPossibleMoves = (movesCache != null) ?  allResultsCached(currentState, nInitDepth - nDepth) : allResults(currentState);
        int bestScore;
        if (allPossibleMoves.size() > 0) {
            if (bIsCurrentMax) {     // current player is Max
                bestScore = Integer.MIN_VALUE;
                for (ReversiBoardState state : allPossibleMoves) { // next level is Min
                    bestScore = Math.max(bestScore, simpleMiniMaxScorer(state, nDepth - 1, false));
                }
            }
            else {                  // current player is Min
                bestScore = Integer.MAX_VALUE;
                for (ReversiBoardState state : allPossibleMoves) { // next level is Max
                    bestScore = Math.min(bestScore, simpleMiniMaxScorer(state, nDepth - 1, true));
                }
            }
        }
        else  { // no legal moves for this player - change player and return the next score
            ReversiBoardState newState = new ReversiBoardState(currentState.boardStateBeforeMove, !currentState.bIsBlackMove);
            return simpleMiniMaxScorer(newState, nDepth - 1 , !bIsCurrentMax);
        }
        return bestScore;
    }

    /**
     *  this is a combination of actions(s) and results(s.a) - we don't think there is a need to separate them
     *
     **/
    private ArrayList<ReversiBoardState> allResults(ReversiBoardState currentState) {
        ArrayList<ReversiBoardState> allResults = null;
        allResults = new ArrayList<ReversiBoardState>();
        byte[][] currentBoard = currentState.boardStateBeforeMove;
        boolean bCurrentPlayerIsBlack = currentState.bIsBlackMove;
        byte[][] optionalBoard = deepCopyMatrix(currentBoard);
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth ; j++) {
                if (currentBoard[i][j] == ReversiConstants.CubeStates.none) {
                    boolean areMoves = checkMovesForPoint(currentBoard, i, j, bCurrentPlayerIsBlack, optionalBoard);
                    if (areMoves) {
                        allResults.add(new ReversiBoardState(optionalBoard,!bCurrentPlayerIsBlack));
                        optionalBoard = deepCopyMatrix(currentBoard);
                    }
                }
            }
        }
        return allResults;
    }

    /**
     * CACHE Version!!!
     * this is a combination of actions(s) and results(s.a) - we don't think there is a need to separate them
     */
    private ArrayList<ReversiBoardState> allResultsCached(ReversiBoardState currentState, int currDepth) {
        ArrayList<ReversiBoardState> allResults = movesCache.read(currentState);  // try to get from cache
        if (allResults == null) {  // if not in cache do the following
//            misses++;
            allResults = allResults(currentState);      // use regular function
            if (currDepth < ReversiConstants.Performance.maxCacheSize) {
                movesCache.write(currentState, allResults); // write to cache
            }
        }
        else {
//            hits++;
        }
        return allResults;
    }


    private static boolean checkMovesForPoint(byte[][] currentBoard, int i, int j, boolean bCurrentPlayerIsBlack, byte[][] optionalBoard) {
        byte oppositePlayer = (byte) (bCurrentPlayerIsBlack ? 1 : 2);
        boolean movesWereMade = false;
        for (int horInterval = -1; horInterval < 2 ; horInterval++) {
            for (int verInterval = -1; verInterval < 2; verInterval++) {
                int row = i + horInterval, col = j + verInterval;
                boolean inBounds = row < ReversiConstants.BoardSize.boardHeight && row >= 0
                        && col < ReversiConstants.BoardSize.boardWidth && col >= 0;
                while ( inBounds && oppositePlayer == currentBoard[row][col]) {
                    row += horInterval;
                    col += verInterval;
                    inBounds = row < ReversiConstants.BoardSize.boardHeight && row >= 0
                            && col < ReversiConstants.BoardSize.boardWidth && col >= 0;
                }
                if (inBounds) {
                    if ((row == i + horInterval && col == j + verInterval)
                            || currentBoard[row][col] == ReversiConstants.CubeStates.none){
                        // means it didn't move at all or got to empty cube
                        continue;
                    }
                    // if got here - changes were made
                    movesWereMade = true;
                    byte value = (byte) (bCurrentPlayerIsBlack ? 2 : 1);
                    while (row != i || col != j) {
                        row -= horInterval;
                        col -= verInterval;
                        optionalBoard[row][col] = value;
                    }
                }
            }
        }
        return movesWereMade;
    }

    private static byte[][] deepCopyMatrix(byte[][] currentBoard) {
        if (currentBoard == null)
            return null;
        byte[][] result = new byte[ReversiConstants.BoardSize.boardHeight][ReversiConstants.BoardSize.boardWidth];
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row++) {
            for (int col = 0; col < ReversiConstants.BoardSize.boardWidth; col++) {
                result[row][col] = currentBoard[row][col];
            }
        }
        return result;
    }

    private int utility(ReversiBoardState currentState) {
        int result = 0;
        HashMap<String, Boolean> heuristicSelectionMap = bIsBlackMax ? blackPlayerHeuristicsMap : whitePlayerHeuristicsMap;
//        System.out.print("0+h1: " + (result += heuristicSelectionMap.get("h1") ? ReversiConstants.HeuristicsWeight.h1 * h1(currentState) : 0));
//        System.out.print(" +h2: " + (result += heuristicSelectionMap.get("h2") ? ReversiConstants.HeuristicsWeight.h2 * h2(currentState) : 0));
////        result += heuristicSelectionMap.get("h3") ? ReversiConstants.HeuristicsWeight.h3 * h3(currentState) : 0;
//        System.out.println(" +h4: " +(result += heuristicSelectionMap.get("h4") ? ReversiConstants.HeuristicsWeight.h4 * h4(currentState) : 0));

        result += heuristicSelectionMap.get("h1") ? ReversiConstants.HeuristicsWeight.h1 * h1(currentState) : 0;
        result += heuristicSelectionMap.get("h2") ? ReversiConstants.HeuristicsWeight.h2 * h2(currentState) : 0;
        result += heuristicSelectionMap.get("h3") ? ReversiConstants.HeuristicsWeight.h3 * h3(currentState) : 0;
        result += heuristicSelectionMap.get("h4") ? ReversiConstants.HeuristicsWeight.h4 * h4(currentState) : 0;

        return result;
    }

    /** this is the most basic anf obvious heuristic - diff in disks count **/
    private int h1 (ReversiBoardState currentState) {
        int whiteCount = sumAllWhites(currentState);
        int blackCount = sumAllBlacks(currentState);
        if (bIsBlackMax)
            return (int) (100 * (float)blackCount/(blackCount + whiteCount));
        else
            return (int) (100 * (float)whiteCount/(whiteCount + blackCount));
    }

    // this is corner heuristic - add score for corner
    private int h2 (ReversiBoardState currentState) {
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
            return 25 * (blackCorner - whiteCorner);
        } else {
            return 25 * (whiteCorner - blackCorner);
        }
    }

    /** this is number of stable discs heuristic **/
    public int h3(ReversiBoardState currentState) {
        int blackStableDiscs = stabilityCheck(currentState, ReversiConstants.CubeStates.black);
        int whiteStableDiscs = stabilityCheck(currentState, ReversiConstants.CubeStates.white);

        int sum = blackStableDiscs + whiteStableDiscs;
        if (sum == 0 ) return 0;
        if (bIsBlackMax) {
            return (int) (100 * (float) blackStableDiscs / sum);
        } else {
            return (int) (100 * (float) whiteStableDiscs / sum);
        }
    }

    private int stabilityCheck (ReversiBoardState currentState, byte cubeState) {

        boolean[][] stableDiscs = new boolean[ReversiConstants.BoardSize.boardHeight][ReversiConstants.BoardSize.boardWidth];
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row++) {
            for (int col = 0; col < ReversiConstants.BoardSize.boardWidth; col++) {
                stableDiscs[row][col] = false;
            }
        }

        checkCorner(currentState, stableDiscs, 0, 0, 1, 1, ReversiConstants.BoardSize.boardWidth, ReversiConstants.BoardSize.boardHeight, cubeState);
        checkCorner(currentState, stableDiscs, 0, ReversiConstants.BoardSize.boardWidth - 1, -1, 1, 0, ReversiConstants.BoardSize.boardHeight, cubeState);
        checkCorner(currentState, stableDiscs, ReversiConstants.BoardSize.boardHeight - 1, ReversiConstants.BoardSize.boardWidth - 1, -1, -1, 0, 0, cubeState);
        checkCorner(currentState, stableDiscs, ReversiConstants.BoardSize.boardHeight - 1, 0, 1, -1, ReversiConstants.BoardSize.boardWidth, 0, cubeState);

        int resCounter = 0;
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row++) {
            for (int col = 0; col < ReversiConstants.BoardSize.boardWidth; col++) {
                resCounter += stableDiscs[row][col] ? 1 : 0;
            }
        }
        return resCounter;
    }


    private void checkCorner(ReversiBoardState currentState, boolean[][] stableDiscs, int x, int y, int xDir, int yDir, int xBoarder, int yBoarder, byte cubeState) {
        int yRunner = y, xRunner = x;
        while (yRunner != yBoarder && currentState.boardStateBeforeMove[yRunner][y] == cubeState) {
            stableDiscs[yRunner][y] = true;
            yRunner += yDir;
        }
        yRunner -= yDir;
        while (xRunner != xBoarder && currentState.boardStateBeforeMove[x][xRunner] == cubeState) {
            stableDiscs[x][xRunner] = true;
            xRunner += xDir;
        }
        xRunner -= xDir;

        if (yRunner > y + 1 && xRunner > x +1) {
            checkCorner(currentState, stableDiscs, x+xDir, y+yDir, xDir, yDir, xRunner, yRunner, cubeState);
        }

    }



    /** this is number of legal moves heuristic **/
    private int hh1(ReversiBoardState currentState) {
        int currentPlayerMoves;
        int nextPlayerMoves;
        if (movesCache != null) {
            currentPlayerMoves = allResultsCached(currentState, 0).size();
            ReversiBoardState nextState = new ReversiBoardState(currentState.boardStateBeforeMove, !currentState.bIsBlackMove);
            nextPlayerMoves = allResultsCached(nextState, 0).size();
        }
        else {
            currentPlayerMoves = allResults(currentState).size();
            ReversiBoardState nextState = new ReversiBoardState(currentState.boardStateBeforeMove, !currentState.bIsBlackMove);
            nextPlayerMoves = allResults(nextState).size();
        }
        return (int) (100 * (float)currentPlayerMoves/(currentPlayerMoves + nextPlayerMoves));
    }

    /** this is number of frontiers heuristic **/
    private int h4(ReversiBoardState currentState) {
        int blackFrontiers = 0;
        int whiteFrontiers = 0;
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth; j++) {
                if (currentState.boardStateBeforeMove[i][j] != ReversiConstants.CubeStates.none) {
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
                                if (currentState.boardStateBeforeMove[i][j] == ReversiConstants.CubeStates.black) {
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
        if(bIsBlackMax){
            return (int) (100 * (float)whiteFrontiers/(whiteFrontiers + blackFrontiers));
        } else {
            return (int) (100 * (float)blackFrontiers/(whiteFrontiers + blackFrontiers));
        }
    }

    private int sumAllWhites(ReversiBoardState currentState){
        int count = 0;
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth ; j++) {
                if (currentState.boardStateBeforeMove[i][j] == ReversiConstants.CubeStates.white) {
                    count++;
                }
            }
        }
        return count;
    }

    private int sumAllBlacks(ReversiBoardState currentState){
        int count = 0;
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth ; j++) {
                if (currentState.boardStateBeforeMove[i][j] == ReversiConstants.CubeStates.black) {
                    count++;
                }
            }
        }
        return count;
    }

}
