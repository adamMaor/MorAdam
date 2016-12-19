package com.othello;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Adam on 09/12/2016.
 */
public class FileParser {

    private Path sharedFile;
    private boolean initialized = false;

    public FileParser() {
        sharedFile = null;
    }

    public void init(String filePath, boolean bIsFileCreatedByMe, boolean bIsFirstMoveBlack) {
        if (filePath.isEmpty() == false) {
            sharedFile = Paths.get(filePath);
            if (sharedFile != null)
            {
                initialized = true;
                if (bIsFileCreatedByMe) {
                    writeInitialState(bIsFirstMoveBlack);
                }
                return;
            }
        }
    }

    public ReversiBoardState getNextState() {
        ReversiBoardState currentState = null;
        if (initialized){
            List<String> linesList = null;
            try {
                linesList =  Files.readAllLines(sharedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (linesList != null) {
                String strLastMove = linesList.get(linesList.size() - 1);
                currentState = parseBoard(strLastMove);
            }
        }
        return currentState;
    }

    public void writeNextState(ReversiBoardState nextState){
        if (initialized){
            String strLine = nextState.bIsBlackMove ? "black," : "white,";
            byte[][] byteArray = nextState.boardStateBeforeMove;
            for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
                int j = 0;
                while (j <ReversiConstants.BoardSize.boardWidth) {
                    byte val = byteArray[i][j];
                    int seqLen = 1;
                    while (j + seqLen < ReversiConstants.BoardSize.boardWidth && byteArray[i][j + seqLen] == val) {
                        seqLen++;
                    }
                    strLine += "" + seqLen + " " + val + ",";
                    j += seqLen;
                }
            }
            strLine += "end\n";
            try {
                Files.write(sharedFile, strLine.getBytes(), StandardOpenOption.APPEND);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeInitialState(boolean bIsBlackFirst)
    {
        if (initialized){
            String strLine = bIsBlackFirst ? "black," : "white,";
            strLine += ReversiConstants.BoardSize.initBoardString;
            try {
                Files.write(sharedFile, strLine.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ReversiBoardState getInitState(boolean bIsFirstBlack) {
        String strState = bIsFirstBlack ? "black," : "white,";
        strState += ReversiConstants.BoardSize.initBoardString;
        return parseBoard(strState);
    }

    private ReversiBoardState parseBoard(String boardStateLine) {
        ReversiBoardState currentState = null;
        if (boardStateLine.isEmpty() == false) {
            ArrayList<String> words = new ArrayList(Arrays.asList(boardStateLine.split(",")));
            boolean isBlackTurn = words.get(0).equals("black") ? true : false;
            byte[][] byteArray = new byte[ReversiConstants.BoardSize.boardHeight][ReversiConstants.BoardSize.boardWidth];
            int row = 0, col =0;
            int wordIndex = 1;
            while (row < ReversiConstants.BoardSize.boardHeight)
            {
                String[] currParams = words.get(wordIndex++).split(" ");
                int seqLength = Integer.parseInt(currParams[0]);
                int value = Integer.parseInt(currParams[1]);
                int currColIndex = col;
                for (; currColIndex < col + seqLength; currColIndex++) {
                    byteArray[row][currColIndex] = (byte)value;
                }

                if (currColIndex == ReversiConstants.BoardSize.boardWidth) {
                    col = 0;
                    row++;
                }
                else { col = currColIndex; }
            }

            if ( (words.get(wordIndex).equals("end\n") == false) && (words.get(wordIndex).equals("end") == false) ) {
                System.out.println("An Error has occurred in file parsing, check legality of file lines");
            }
            else { currentState = new ReversiBoardState(byteArray,isBlackTurn); }
            }
        return currentState;
    }
}
