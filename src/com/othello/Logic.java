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
public class Logic {
    public static ReversiBoardState currentState = null;
    private byte whitePlayerType = 0, blackPlayerType = 0;
    private int depth = 0;
    private int delayTime = 250;
    private GameBoard board = null;
    private FileParser parser = null;

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

    /**
     * This method will be called before every turn
     * If it is a computer turn it will call the computer move generator
     * If it is other computer turn will wait on the file to get the other PC move
     * Else it will wait for User Input
     */
    public void generateMove() {
        if (isCurrentPlayerPC()) {
            if (getPCMove() == false) {
                // no moves available for for this PC player
                if (isNextPlayerPC()) {
                    currentState.bIsBlackMove = !currentState.bIsBlackMove;
                    getPCMove();
                    return;
                }
            }
            if (gameIsOver() ==false) {
                generateMove();
            }
        }
        // now check for other pc logic
        else if (isCurrentPlayerOtherPC()) {
            // wait for other PC move - maybe same logic as PC to enable play between 2 PC via the file
            // We'll think about it. currently only one player can be other PC

        }
        // if non then waiting for user (Human) input - Do Nothing! just wait
    }

    private boolean isCurrentPlayerPC() {
        return (currentState.bIsBlackMove && blackPlayerType == ReversiConstants.PlayerTypes.pc)
                || (!currentState.bIsBlackMove && whitePlayerType == ReversiConstants.PlayerTypes.pc);
    }

    private boolean isNextPlayerPC() {
        return (!currentState.bIsBlackMove && blackPlayerType == ReversiConstants.PlayerTypes.pc)
                || (currentState.bIsBlackMove && whitePlayerType == ReversiConstants.PlayerTypes.pc);
    }

    private boolean isCurrentPlayerOtherPC() {
        return (currentState.bIsBlackMove && blackPlayerType == ReversiConstants.PlayerTypes.otherPC)
                || (!currentState.bIsBlackMove && whitePlayerType == ReversiConstants.PlayerTypes.otherPC);
    }

    /**
     * Called when GUI gets click from user on an empty spot on his turn
     * @param row the row on the board
     * @param col the column on the board
     */
    public void getHumanMove(int row, int col) {
        byte[][] optionalBoard = deepCopyMatrix(currentState.boardStateBeforeMove);
        checkMovesForPoint(currentState.boardStateBeforeMove, row, col, currentState.bIsBlackMove, optionalBoard);
        if (!(Arrays.deepEquals(optionalBoard,currentState.boardStateBeforeMove ))) {
            currentState.boardStateBeforeMove = optionalBoard;
            currentState.bIsBlackMove = !(currentState.bIsBlackMove);
            board.repaintBoard(currentState);
            parser.writeNextState(currentState);
            if (validateCurrentMove() == true) {
                Timer timer = new Timer(30, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        generateMove();
                    }
                });
                timer.start();
            }
        }
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
        currentState.boardStateBeforeMove = selectedMove;
        currentState.bIsBlackMove = !currentState.bIsBlackMove;
        board.repaintBoard(currentState);
        parser.writeNextState(currentState);
        return validateCurrentMove();
    }

    private ArrayList<byte[][]> getAvailableMoves(ReversiBoardState currentState) {
        byte[][] currentBoard = currentState.boardStateBeforeMove;
        boolean bCurrentPlayerIsBlack = currentState.bIsBlackMove;
        byte[][] optionalBoard = deepCopyMatrix(currentBoard);
        ArrayList<byte[][]> availableMoves = new ArrayList<byte[][]>();
        for (int i = 0; i < ReversiConstants.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.boardWidth ; j++) {
                if (currentBoard[i][j] == ReversiConstants.CubeStates.none) {
                    checkMovesForPoint(currentBoard, i, j, bCurrentPlayerIsBlack, optionalBoard);
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
     * Create a new board state according to given coordinate from the current board.
     * @param currentBoard -
     * @param i - row coordinate of human click on board
     * @param j - col coordinate of human click on board
     * @param bCurrentPlayerIsBlack
     * @param optionalBoard
     */
    private static void checkMovesForPoint(byte[][] currentBoard, int i, int j, boolean bCurrentPlayerIsBlack, byte[][] optionalBoard) {
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

    private static byte[][] deepCopyMatrix(byte[][] currentBoard) {
        if (currentBoard == null)
            return null;
        byte[][] result = new byte[currentBoard.length][];
        for (int r = 0; r < currentBoard.length; r++) {
            result[r] = currentBoard[r].clone();
        }
        return result;
    }

    private boolean validateCurrentMove() {
        if (gameIsOver() == true) {
            board.gameIsOver();
            System.exit(0);
            return false;
        }
        return true;
    }

    private boolean gameIsOver(){
        boolean gameIsOver = false;
        if (sumAllBoard() == ReversiConstants.boardSquare) {
            gameIsOver = true;
        } else if (getAvailableMoves(currentState).size() == 0) {
            ReversiBoardState nextState = new ReversiBoardState(currentState.boardStateBeforeMove, !currentState.bIsBlackMove);
            if(getAvailableMoves(nextState).size() == 0) {
                gameIsOver = true;
            }
        }
        return gameIsOver;
    }

    private int sumAllBoard(){
        int count = 0;
        for (int i = 0; i < ReversiConstants.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.boardWidth ; j++) {
                if (currentState.boardStateBeforeMove[i][j] != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private int sunAllWhites(){
        int count = 0;
        for (int i = 0; i < ReversiConstants.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.boardWidth ; j++) {
                if (currentState.boardStateBeforeMove[i][j] == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    private int sunAllBlacks(){
        int count = 0;
        for (int i = 0; i < ReversiConstants.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.boardWidth ; j++) {
                if (currentState.boardStateBeforeMove[i][j] == 2) {
                    count++;
                }
            }
        }
        return count;
    }
}
