package com.othello;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Adam on 09/12/2016.
 */
public class Worker {
    public static ReversiBoardState currentState = null;
    private byte whitePlayerType = 0, blackPlayerType = 0;
    private int depth = 0;
    private int delayTime = 250;
    private GameBoard board = null;
    private FileParser parser = null;

    public void generateMove() {

        if (getAvailableMoves(currentState).size() == 0)
        {
            ReversiBoardState nextState = new ReversiBoardState(currentState.boardStateBeforeMove, !currentState.bIsBlackMove);
            if (getAvailableMoves(nextState).size() == 0)
            {
                board.gameIsOver();
            }
        }

        if ( (currentState.bIsBlackMove && blackPlayerType == 1) || (!currentState.bIsBlackMove && whitePlayerType == 1) ) {
            if (getPCMove() == false) {
                if ((!currentState.bIsBlackMove && blackPlayerType == 1) || (currentState.bIsBlackMove && whitePlayerType == 1)) {
                    currentState.bIsBlackMove = !currentState.bIsBlackMove;
                    getPCMove();
                }
            }
            generateMove();
        }
        // now check for other pc logic

        // if non then waiting for user (Human) input
    }

    public void init(GameBoard board, FileParser parser, byte whitePlayerType, byte blackPlayerType, int depth, int delayTime) {
        this.whitePlayerType = whitePlayerType;
        this.blackPlayerType = blackPlayerType;
        this.depth = depth;
        this.currentState = parser.getNextState();
        this.board = board;
        this.parser = parser;
        this.delayTime = delayTime;
        board.repaintBoard(currentState);
    }

    private boolean getPCMove() {
        ArrayList<byte[][]> potentialMoves = getAvailableMoves(currentState);
        if (potentialMoves.size() == 0) {
            // no legal moves
            return false;
        }
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        byte[][] selectedMove = potentialMoves.get(rand.nextInt(potentialMoves.size()));
        currentState = new ReversiBoardState(selectedMove, !currentState.bIsBlackMove);
        board.repaintBoard(currentState);
        parser.writeNextState(currentState);
        return true;
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

    /**
     * Called when GUI gets click from user on an empty spot on his turn
     * @param row the row on the board
     * @param col the column on the board
     */
    public void getHumanMove(int row, int col) {
        byte[][] optionalBoard = deepCopyMatrix(currentState.boardStateBeforeMove);
        checkMoves(currentState.boardStateBeforeMove, row, col, currentState.bIsBlackMove, optionalBoard);
        if (!(Arrays.deepEquals(optionalBoard,currentState.boardStateBeforeMove ))) {
            currentState.boardStateBeforeMove = optionalBoard;
            currentState.bIsBlackMove = !(currentState.bIsBlackMove);
            board.repaintBoard(currentState);
            parser.writeNextState(currentState);
            Timer timer = new Timer(25, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    generateMove();
                }
            });
            timer.start();

            ;
        }
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





}
