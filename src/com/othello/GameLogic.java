package com.othello;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Adam on 09/12/2016.
 */
public class GameLogic {
    private LogicUtils utils;
    private ReversiBoardState lastState;
    public static ReversiBoardState currentState;
    ArrayList<ReversiBoardState> nextAvailableMovesList;
    private boolean bFirstPlayerWasBlack;
    private byte whitePlayerType, blackPlayerType;
    private HashMap<String,Boolean> blackPlayerHeuristicsMap;
    private HashMap<String,Boolean> whitePlayerHeuristicsMap;
    private int depth;
    private boolean bIsAlphaBeta;
    private boolean bIsCacheUsed;
    private int delayTime;
    private GameGUI gameGUI;
    private FileParser fileParser;
    private MovesCache movesCache;
    private boolean bIsAutoPlayOn;
    private boolean bShowAvailableMoves;
    private boolean bShowLastMove;
    private Long pcMoveTime;
    private int pcMoveCounter;
    private boolean gameOver;

    public GameLogic(LogicUtils logicUtils) {
        utils = logicUtils;
    }

    public void init(GameGUI gameGUI, FileParser fileParser, byte whitePlayerType, byte blackPlayerType, boolean bFirstPlayerWasBlack, int depth, boolean isAlphaBeta, boolean useCache,
                     int delayTime, boolean isShowAvailableMoves, boolean isShowLastMove,
                     HashMap<String,Boolean> blackPlayerHeuristicsMap, HashMap<String,Boolean> whitePlayerHeuristicsMap) {
        this.whitePlayerType = whitePlayerType;
        this.blackPlayerType = blackPlayerType;
        this.blackPlayerHeuristicsMap = blackPlayerHeuristicsMap;
        this.whitePlayerHeuristicsMap = whitePlayerHeuristicsMap;
        this.depth = depth;
        this.bIsAlphaBeta = isAlphaBeta;
        this.bIsCacheUsed = useCache;
        this.fileParser = fileParser;
        this.bFirstPlayerWasBlack = bFirstPlayerWasBlack;
//        this.currentState = fileParser.getNextState();
        this.currentState = fileParser.getInitState(bFirstPlayerWasBlack);
        this.movesCache = utils.getCache();
        this.nextAvailableMovesList = new ArrayList<ReversiBoardState>();
        this.lastState = currentState;
        this.gameGUI = gameGUI;
        this.delayTime = delayTime;
        this.bShowAvailableMoves = isShowAvailableMoves;
        this.bShowLastMove = isShowLastMove;
        this.pcMoveTime = new Long(0);
        this.pcMoveCounter = 0;
        validateMove(currentState);
        refreshGui();
        bIsAutoPlayOn = true;
        gameOver = false;
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
                return;
            case ReversiConstants.GameStatus.currentPlayerCanPlay:
                if (isCurrentPlayerPC()) {
                    gameGUI.setPcRunning(true);
                    try {   // sleep for the delay time specified in settings
//                        startTime += delayTime;
                        Thread.sleep(delayTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Thread pcMove = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (getPCMove() == true) {   // PC has played - generate next move
                                if (bIsAutoPlayOn) {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() { generateMove(); } });
                                }
                            }
                        }
                    });
                    pcMove.start();

                }
                else if (isCurrentPlayerOtherPC()) {    // now check for other pc logic

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
    public void getHumanMove(final int row, final int col) {
        byte[][] optionalBoard = utils.fillOptionalBoardForPoint(currentState.boardStateBeforeMove, row, col, currentState.bIsBlackMove);
        if (!(Arrays.deepEquals(optionalBoard,currentState.boardStateBeforeMove ))) {
            changeTotalCurrentState(optionalBoard);
            // this timer is set to allow GUI to repaint
            Timer timer = new Timer(50, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (bIsAutoPlayOn) {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() { generateMove(); } });
                        }
                    }
                });
            timer.setRepeats(false);    // Single Shot
            timer.start();
        }
    }

    private boolean getPCMove() {
        Long startTime = System.currentTimeMillis();
        pcMoveCounter++;
        boolean bRes = true;
        MovesCache cache = bIsCacheUsed ? movesCache : null;
        MiniMaxLogic miniMaxSolver = new MiniMaxLogic(utils, currentState, depth, nextAvailableMovesList, cache, blackPlayerHeuristicsMap, whitePlayerHeuristicsMap);
        ReversiBoardState nextState = miniMaxSolver.launchMiniMax(bIsAlphaBeta);
        if (nextState == null) {
            bRes = false;
        }
        else {
            changeTotalCurrentState(nextState);
        }
        pcMoveTime += System.currentTimeMillis() - startTime;
        gameGUI.setPcRunning(false);
        return bRes;
    }

    /** will return true if current moves are available **/
    private boolean validateMove(ReversiBoardState state) {
        nextAvailableMovesList.clear();
        nextAvailableMovesList = utils.allResults(state);
        return nextAvailableMovesList.size() > 0;
    }

    private void endGameLogic() {
        this.gameOver = true;
        gameGUI.gameIsOver();
    }

    private void changeOnlyPlayer() {
        currentState.bIsBlackMove = !currentState.bIsBlackMove;
        gameGUI.playerHadChanged();
        refreshGui();
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

    public String getGameSum() {
        String res = "Game Summary:\n";
        res += "    First player was: " + (bFirstPlayerWasBlack ? "Black" : "White") + "\n";
        res += "    Average Pc Move Time: " + pcMoveTime / pcMoveCounter + " milliSeconds\n";
        res += "    Depth: " + depth + "\n    Used Alpha-Beta? " + bIsAlphaBeta + "\n    Used Cache? "+ bIsCacheUsed + "\n";
        res += "Pc Player Heuristics (Relevant only for PC players):\n";

        String h1Line = "    h1: ";
        String h2Line = "    h2: ";
        String h3Line = "    h3: ";
        String h4Line = "    h4: ";
        String h5Line = "    h5: ";

        if (blackPlayerType == ReversiConstants.PlayerTypes.pc) {
            res += "              White Player ";
            h1Line += "          " + whitePlayerHeuristicsMap.get("h1");
            h2Line += "          " + whitePlayerHeuristicsMap.get("h2");
            h3Line += "          " + whitePlayerHeuristicsMap.get("h3");
            h4Line += "          " + whitePlayerHeuristicsMap.get("h4");
            h5Line += "          " + whitePlayerHeuristicsMap.get("h5");
        }
        if (blackPlayerType == ReversiConstants.PlayerTypes.pc) {
            res += "     Black Player ";
            h1Line += "                  " + blackPlayerHeuristicsMap.get("h1");
            h2Line += "                  " + blackPlayerHeuristicsMap.get("h2");
            h3Line += "                  " + blackPlayerHeuristicsMap.get("h3");
            h4Line += "                  " + blackPlayerHeuristicsMap.get("h4");
            h5Line += "                  " + blackPlayerHeuristicsMap.get("h5");
        }
        res += "\n" + h1Line + "\n" +  h2Line + "\n" + h3Line + "\n" + h4Line + "\n" + h5Line + "\n";
        return res;
    }
}
