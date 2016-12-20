package com.othello;

import java.util.ArrayList;

/**
 * Created by Adam on 20/12/2016.
 */
public class LogicUtils {

    private MovesCache movesCache;

    public LogicUtils() {
        this.movesCache = new MovesCache();
    }

    /**
     * CACHE Version!!!
     * this is a combination of actions(s) and results(s.a) - we don't think there is a need to separate them
     */
    public ArrayList<ReversiBoardState> allResultsCached(ReversiBoardState currentState, int currDepth) {
        ArrayList<ReversiBoardState> allResults = movesCache.read(currentState);  // try to get from cache
        if (allResults == null) {  // if not in cache do the following
            allResults = allResults(currentState);      // use regular function
            if (currDepth < ReversiConstants.Performance.maxCacheDepth) {
                movesCache.write(currentState, allResults); // write to cache
            }
        }
        return allResults;
    }

    /**
     *  this is a combination of actions(s) and results(s.a) - we don't think there is a need to separate them
     *
     **/
    public ArrayList<ReversiBoardState> allResults(ReversiBoardState currentState) {
        ArrayList<ReversiBoardState> allResults = new ArrayList<ReversiBoardState>();
        byte[][] currentBoard = currentState.boardStateBeforeMove;
        boolean bCurrentPlayerIsBlack = currentState.bIsBlackMove;
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth ; j++) {
                if (currentBoard[i][j] == ReversiConstants.CubeStates.none) {
                    boolean areMoves = checkMovesForPoint(currentBoard, i, j, bCurrentPlayerIsBlack);
                    if (areMoves) {
                        byte[][] optionalBoard = deepCopyMatrix(currentBoard);
                        getMovesForPoint(currentBoard, i, j, bCurrentPlayerIsBlack, optionalBoard);
                        allResults.add(new ReversiBoardState(optionalBoard,!bCurrentPlayerIsBlack));
                    }
                }
            }
        }
        return allResults;
    }

    /**
     * checks if there are more than 0 available moves
     * @param currentBoard
     * @param i
     * @param j
     * @param bCurrentPlayerIsBlack
     * @return true if there are any available moves
     */
    private boolean checkMovesForPoint(byte[][] currentBoard, int i, int j, boolean bCurrentPlayerIsBlack) {
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
                    movesWereMade = true;
                    break;
                }
            }
            if (movesWereMade) break;
        }
        return movesWereMade;
    }

    /**
     * Similar to checkMovesForPoint - only fills the optionalboard - should be called after checkMovesForPoint
     * @param currentBoard
     * @param i
     * @param j
     * @param bCurrentPlayerIsBlack
     * @param optionalBoard - board to fill
     * @return
     */
    private boolean getMovesForPoint(byte[][] currentBoard, int i, int j, boolean bCurrentPlayerIsBlack, byte[][] optionalBoard) {
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

    public byte[][] deepCopyMatrix(byte[][] currentBoard) {
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

    public byte[][] fillOptionalBoardForPoint(byte[][] boardStateBeforeMove, int row, int col, boolean bIsBlackMove) {
        byte[][] optionalBoard = deepCopyMatrix(boardStateBeforeMove);
        getMovesForPoint(boardStateBeforeMove, row, col, bIsBlackMove, optionalBoard);
        return optionalBoard;

    }

    public MovesCache getCache() {
        return movesCache;
    }
}
