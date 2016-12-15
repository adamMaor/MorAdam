package com.othello;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Adam on 15/12/2016.
 */
public class MiniMaxLogic {

    ReversiBoardState initialState;
    ArrayList<ReversiBoardState> nextAvailableMovesList;
    boolean bIsBlackMax = true; // this will be initialized and is for player(s)
    int nDepth = 4;

    public MiniMaxLogic(ReversiBoardState initialState, int nDepth, ArrayList<ReversiBoardState> nextAvailableMovesList) {
        this.initialState = initialState;
        this.nextAvailableMovesList = nextAvailableMovesList;
        this.bIsBlackMax = initialState.bIsBlackMove;
        this.nDepth = nDepth;
    }

    public ReversiBoardState launchMiniMax(boolean isAlphaBeta)
    {
        ReversiBoardState bestBoard = null;
        if (isAlphaBeta) {

        }
        else {
            bestBoard =  simpleMiniMax(initialState, nDepth);
        }
        System.gc();
        return bestBoard;
    }

    public ReversiBoardState simpleMiniMax(ReversiBoardState currentState, int nDepth) {
        ReversiBoardState nextState = null;
        if (nDepth == 0 || terminal(currentState)) {
            return currentState;
        }
        if (nextAvailableMovesList.size() > 0) {
            // we know that first player is Max
            int bestScore = Integer.MIN_VALUE;
            for (ReversiBoardState state : nextAvailableMovesList) {
                // next level is Min
                int currScore = simpleMiniMaxScorer(state, nDepth - 1, false);
                if (currScore > bestScore) {
                    bestScore = currScore;
                    nextState = state;
                }
            }
        }
        else { // no moves available - not supposed to happen - will be checked in Logic
            // will return null
        }

        return nextState;
    }

    private int simpleMiniMaxScorer (ReversiBoardState currentState, int nDepth, boolean bIsCurrentMax) {
        if (nDepth == 0 || terminal(currentState)) {
            return utility(currentState);
        }
        ArrayList<ReversiBoardState> allPossibleMoves = allResults(currentState);
        int bestScore;
        if (allPossibleMoves.size() > 0) {
            if (bIsCurrentMax) {     // current player is Max
                bestScore = Integer.MIN_VALUE;
                for (ReversiBoardState state : allPossibleMoves) {
                    // next level is Min
                    int currScore = simpleMiniMaxScorer(state, nDepth - 1, false);
                    if (currScore > bestScore) {
                        bestScore = currScore;
                    }
                }
            }
            else {                  // current player is Min
                bestScore = Integer.MAX_VALUE;
                for (ReversiBoardState state : allPossibleMoves) {
                    // next level is Min
                    int currScore = simpleMiniMaxScorer(state, nDepth - 1, true);
                    if (currScore < bestScore) {
                        bestScore = currScore;
                    }
                }
            }
        }
        else  { // no legal moves for this player - change player and return the next score
            currentState.bIsBlackMove = !currentState.bIsBlackMove;
            return simpleMiniMaxScorer(currentState, nDepth - 1 , !bIsCurrentMax);
        }
        System.gc();
        return bestScore;
    }

    private boolean isCurrentPlayerMax(ReversiBoardState currentState) {
        return ( (bIsBlackMax && currentState.bIsBlackMove) || (!bIsBlackMax && !currentState.bIsBlackMove));
    }

    /**
     *  this is a combination of actions(s) and results(s.a) - we don't think there is a need to separate them
     *
     **/
    private ArrayList<ReversiBoardState> allResults(ReversiBoardState currentState) {
        ArrayList<ReversiBoardState> allResults = new ArrayList<ReversiBoardState>();
        byte[][] currentBoard = currentState.boardStateBeforeMove;
        boolean bCurrentPlayerIsBlack = currentState.bIsBlackMove;
        byte[][] optionalBoard = deepCopyMatrix(currentBoard);
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth ; j++) {
                if (currentBoard[i][j] == ReversiConstants.CubeStates.none) {
                    checkMovesForPoint(currentBoard, i, j, bCurrentPlayerIsBlack, optionalBoard);
                }
                if (!(Arrays.deepEquals(optionalBoard, currentBoard))) {
                    allResults.add(new ReversiBoardState(optionalBoard,!bCurrentPlayerIsBlack));
                    optionalBoard = deepCopyMatrix(currentBoard);
                }
            }
        }
        return allResults;
    }

    private static void checkMovesForPoint(byte[][] currentBoard, int i, int j, boolean bCurrentPlayerIsBlack, byte[][] optionalBoard) {
        byte oppositePlayer = (byte) (bCurrentPlayerIsBlack ? 1 : 2);
        for (int horInterval = -1; horInterval < 2 ; horInterval++) {
            for (int verInterval = -1; verInterval < 2; verInterval++) {
                int row = i + horInterval, col = j + verInterval;
                while (row < ReversiConstants.BoardSize.boardHeight && row >= 0
                        && col < ReversiConstants.BoardSize.boardWidth && col >= 0
                        && oppositePlayer == currentBoard[row][col]) {
                    row += horInterval;
                    col += verInterval;
                }
                if (row < ReversiConstants.BoardSize.boardHeight && row >= 0 && col < ReversiConstants.BoardSize.boardWidth && col >= 0) {
                    if ((row == i + horInterval && col == j + verInterval)
                            || currentBoard[row][col] == 0){
                        continue;
                    }
                    while (row != i || col != j) {
                        row -= horInterval;
                        col -= verInterval;
                        optionalBoard[row][col] = (byte) (bCurrentPlayerIsBlack ? 2 : 1);
                    }
                }
            }
        }
    }

    private static byte[][] deepCopyMatrix(byte[][] currentBoard) {
        if (currentBoard == null)
            return null;
        byte[][] result = new byte[currentBoard.length][];
        for (int r = 0; r < currentBoard.length; r++) {
            result[r] = currentBoard[r].clone();
        }
        return result;
    }

    private boolean terminal (ReversiBoardState currentState) {
        boolean result = false;

        return result;
    }

    private int utility(ReversiBoardState currentState) {
        int result = 0;
        result += h1(currentState);
        return result;
    }

    /** this is the most basic anf obvious heuristic - diff in disks count **/
    private int h1 (ReversiBoardState currentState) {
        int whiteCount = sumAllWhites(currentState);
        int blackCount = sumAllBlacks(currentState);
        if (bIsBlackMax)
            return (blackCount - whiteCount);
        else
            return (whiteCount - blackCount);
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
