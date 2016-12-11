package com.othello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Adam on 09/12/2016.
 */
public class Worker {

    private byte whitePlayer = 0, blackPlayer = 0;
    private int depth = 0;

    public void init(byte whitePlayerType, byte blackPlayerType, int depth) {
        this.whitePlayer = whitePlayerType;
        this.blackPlayer = blackPlayerType;
        this.depth = depth;
    }

    public ReversiBoardState getBestMove(ReversiBoardState currentState) {
        ArrayList<byte[][]> potentialMoves = getAvailableMoves(currentState);
        Random rand = new Random();
        byte[][] selectedMove = potentialMoves.get(rand.nextInt(potentialMoves.size() - 1));
        ReversiBoardState nextState = new ReversiBoardState(selectedMove, !currentState.bIsBlackMove);
        return nextState;
    }

    //create a counter method

    /* My logic - mabye not the best one but it will work :) . So... where I find blank cell i check all the 8 neighbours (or less) cells. if there cell with the
  oposite color i move in that direction as long as the oposite color is . if i cross a blank cell i can cover all cell with my
   */
    private ArrayList<byte[][]> getAvailableMoves(ReversiBoardState currentState) {
        byte[][] currentBoard = currentState.boardStateBeforeMove;
        boolean bCurrentPlayerIsBlack = currentState.bIsBlackMove;
        byte[][] optionalBoard = deepCopyMatrix(currentBoard);
        ArrayList<byte[][]> availableMoves = new ArrayList<byte[][]>();
        for (int i = 0; i < ReversiConstants.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.boardWidth ; j++) {
                if (currentBoard[i][j] == 0) {
                    checkMoves(currentBoard, i, j, bCurrentPlayerIsBlack, optionalBoard);
                }
                if (!(Arrays.deepEquals(optionalBoard, currentBoard))) {
                    availableMoves.add(optionalBoard);
                    optionalBoard = deepCopyMatrix(currentBoard);
                }

            }
        }
        return availableMoves;
    }

    public static void checkMoves(byte[][] currentBoard, int i, int j, boolean bCurrentPlayerIsBlack, byte[][] optionalBoard) {
        byte oppositePlayer = (byte) (bCurrentPlayerIsBlack ? 1 : 2);
        for (int horInterval = -1; horInterval < 2 ; horInterval++) {
            for (int verInterval = -1; verInterval < 2; verInterval++) {
                int row = i + horInterval, col = j + verInterval;
                while (row < ReversiConstants.boardHeight && row >= 0
                        && col < ReversiConstants.boardWidth && col >= 0
                        && oppositePlayer == currentBoard[row][col]) {
                    row += horInterval;
                    col += verInterval;
                }
                if (row < ReversiConstants.boardHeight && row >= 0 && col < ReversiConstants.boardWidth && col >= 0) {
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

    public static byte[][] deepCopyMatrix(byte[][] currentBoard) {
        if (currentBoard == null)
            return null;
        byte[][] result = new byte[currentBoard.length][];
        for (int r = 0; r < currentBoard.length; r++) {
            result[r] = currentBoard[r].clone();
        }
        return result;
    }
//
//    public static byte opposite(byte currentPlayer) {
//
//        byte oppositePlayer = (byte)(currentPlayer == 1 ? 2:1);
//
//        return oppositePlayer;
//    }




}
