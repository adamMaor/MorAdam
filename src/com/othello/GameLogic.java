package com.othello;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Adam on 09/12/2016.
 */
public class GameLogic {
    private ReversiBoardState lastState;
    public static ReversiBoardState currentState;
    ArrayList<ReversiBoardState> nextAvailableMovesList;
    private byte whitePlayerType, blackPlayerType;
    private int depth;
    private boolean bIsAlphaBeta;
    private int delayTime;
    private GameGUI gameGUI;
    private FileParser fileParser;
    private MovesCache movesCache;
    private boolean bIsAutoPlayOn;
    private boolean bShowAvailableMoves;
    private boolean bShowLastMove;

    public void init(GameGUI gameGUI, FileParser fileParser, byte whitePlayerType, byte blackPlayerType, int depth, boolean isAlphaBeta, int delayTime, boolean isShowAvailableMoves, boolean isShowLastMove) {
        this.whitePlayerType = whitePlayerType;
        this.blackPlayerType = blackPlayerType;
        this.depth = depth;
        this.bIsAlphaBeta = isAlphaBeta;
        this.fileParser = fileParser;
        this.currentState = fileParser.getNextState();
        this.movesCache = new MovesCache();
        this.nextAvailableMovesList = new ArrayList<ReversiBoardState>();
        this.lastState = currentState;
        this.gameGUI = gameGUI;
        this.delayTime = delayTime;
        this.bShowAvailableMoves = isShowAvailableMoves;
        this.bShowLastMove = isShowLastMove;
        validateMove(currentState);
        refreshGui();
        bIsAutoPlayOn = false;
    }

    /**
     * This method will be called before every turn
     * First will check game status (1 of 3: end, current player can play, only next player can play)
     * than:
     * If it is a computer turn it will call the computer move generator
     * If it is other computer turn will wait on the file to get the other PC move
     * Else it will wait for User Input
     */
    public void generateMove() {
        switch (getGameStatus()) {
            case ReversiConstants.GameStatus.noMoreMoves:
                endGameLogic();
                break;
            case ReversiConstants.GameStatus.currentPlayerCanPlay:
                if (isCurrentPlayerPC()) {
                    if (getPCMove() == true) {
                        // PC has played - generate next move
                        if (bIsAutoPlayOn) {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    generateMove();

                                }
                            });
                        }
                    }
                    else {
                        //ERROR - never supposed to happen due to GameStatus check !!!
                    }
                } // now check for other pc logic
                else if (isCurrentPlayerOtherPC()) {

                }
                // if non then waiting for user (Human) input - Do Nothing! just wait
                break;
            case ReversiConstants.GameStatus.noMovesForCurrentPlayer:
                changeOnlyPlayer();
                generateMove();
        }
    }

    private byte getGameStatus(){
        byte gameStatus = ReversiConstants.GameStatus.currentPlayerCanPlay;
        if (sumAllBoard() == ReversiConstants.BoardSize.boardSquare) {
            gameStatus = ReversiConstants.GameStatus.noMoreMoves;
        } else if (validateMove(currentState) == false) {
            ReversiBoardState nextState = new ReversiBoardState(currentState.boardStateBeforeMove, !currentState.bIsBlackMove);
            if(validateMove(nextState) == false) {
                gameStatus = ReversiConstants.GameStatus.noMoreMoves;
            }
            else {
                gameStatus = ReversiConstants.GameStatus.noMovesForCurrentPlayer;
            }
        }
        return gameStatus;
    }

    private boolean isCurrentPlayerPC() {
        return (currentState.bIsBlackMove && blackPlayerType == ReversiConstants.PlayerTypes.pc)
                || (!currentState.bIsBlackMove && whitePlayerType == ReversiConstants.PlayerTypes.pc);
    }

    private boolean isCurrentPlayerOtherPC() {
        return (currentState.bIsBlackMove && blackPlayerType == ReversiConstants.PlayerTypes.otherPC)
                || (!currentState.bIsBlackMove && whitePlayerType == ReversiConstants.PlayerTypes.otherPC);
    }

    /**
     * Called when GUI gets click from user on an empty spot on his turn
     * @param row the row on the gameGUI
     * @param col the column on the gameGUI
     */
    public void getHumanMove(int row, int col) {
        byte[][] optionalBoard = deepCopyMatrix(currentState.boardStateBeforeMove);
        checkMovesForPoint(currentState.boardStateBeforeMove, row, col, currentState.bIsBlackMove, optionalBoard);
        if (!(Arrays.deepEquals(optionalBoard,currentState.boardStateBeforeMove ))) {
            changeTotalCurrentState(optionalBoard);
            Timer timer = new Timer(30, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    if (bIsAutoPlayOn) {
                        generateMove();
                    }
                    }
                });
            timer.start();
        }

    }

    public boolean getPCMove() {

        MiniMaxLogic miniMaxSolver = new MiniMaxLogic(currentState, depth, nextAvailableMovesList, movesCache);
        ReversiBoardState nextState = miniMaxSolver.launchMiniMax(bIsAlphaBeta);
        if (nextState == null) {
            return false;
        }
        changeTotalCurrentState(nextState);
        // sleep for the delay time specified in settings
        if (bIsAutoPlayOn) {
            try {
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private ArrayList<ReversiBoardState> getAvailableMoves(ReversiBoardState currentState) {
        byte[][] currentBoard = currentState.boardStateBeforeMove;
        boolean bCurrentPlayerIsBlack = currentState.bIsBlackMove;
        byte[][] optionalBoard = deepCopyMatrix(currentBoard);
        ArrayList<ReversiBoardState> availableMoves = new ArrayList<ReversiBoardState>();
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth ; j++) {
                if (currentBoard[i][j] == ReversiConstants.CubeStates.none) {
                    checkMovesForPoint(currentBoard, i, j, bCurrentPlayerIsBlack, optionalBoard);
                }
                if (!(Arrays.deepEquals(optionalBoard, currentBoard))) {
                    availableMoves.add(new ReversiBoardState(optionalBoard,!currentState.bIsBlackMove));
                    optionalBoard = deepCopyMatrix(currentBoard);
                }
            }
        }
        return availableMoves;
    }

    /**
     * Create a new gameGUI state according to given coordinate from the current gameGUI.
     * @param currentBoard -
     * @param i - row coordinate of human click on gameGUI
     * @param j - col coordinate of human click on gameGUI
     * @param bCurrentPlayerIsBlack
     * @param optionalBoard
     */
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

    /** will return true if current moves are available **/
    private boolean validateMove(ReversiBoardState state) {
        nextAvailableMovesList.clear();
        nextAvailableMovesList = getAvailableMoves(state);

        return nextAvailableMovesList.size() > 0;
    }

    private void endGameLogic() {
        gameGUI.gameIsOver();
        System.exit(0);
    }

    private void changeOnlyPlayer() {
        currentState.bIsBlackMove = !currentState.bIsBlackMove;
        refreshGui();
        gameGUI.playerHadChanged();
    }

    private void changeTotalCurrentState(byte[][] newBoard) {
        ReversiBoardState newState = new ReversiBoardState(newBoard, !currentState.bIsBlackMove);
        changeTotalCurrentState(newState);
    }

    private void changeTotalCurrentState(ReversiBoardState newState) {
        lastState = currentState;
        currentState = newState;
        validateMove(currentState);
        refreshGui();
    }

    private void refreshGui() {
        gameGUI.repaintBoard(currentState);
        if (bShowLastMove) {
            gameGUI.markLastMove(currentState, lastState);
        }
        if (bShowAvailableMoves) {
            GameGUI.markAvailableMoves(currentState, nextAvailableMovesList);
        }
        fileParser.writeNextState(currentState);
    }

    private int sumAllBoard(){
        int count = 0;
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth ; j++) {
                if (currentState.boardStateBeforeMove[i][j] != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public void setAutoPlay(boolean selected) {
        bIsAutoPlayOn = selected;
    }
}
