package com.othello;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Adam on 09/12/2016.
 */
public class FileParser {

    private Path sharedFile;

    public FileParser() {
        sharedFile = null;
    }

    public void init(String filePath, boolean bIsFileCreatedByMe, boolean bIsFirstMoveBalck) {
        sharedFile = Paths.get(filePath);
        if (sharedFile == null)
        {
            // Error
            return;
        }
        if (bIsFileCreatedByMe) {
            writeInitialState(bIsFirstMoveBalck);
        }
    }

    public ReversiBoardState getNextState() {
        ReversiBoardState currentState = null;
        List<String> linesList = null;
        try {
            linesList =  Files.readAllLines(sharedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (linesList != null) {
            String strLastMove = linesList.get(linesList.size() - 1);
            ArrayList<String> words = new ArrayList(Arrays.asList(strLastMove.split(",")));
            boolean isBlackTurn = words.get(0).equals("black") ? true : false;
            byte[][] byteArray = new byte[ReversiConstants.boardHeight][ReversiConstants.boardWidth];
            int row = 0, col =0;
            int wordIndex = 1;
            while (row < ReversiConstants.boardHeight)
            {
                String[] currParams = words.get(wordIndex++).split(" ");
                int seqLength = Integer.parseInt(currParams[0]);
                int value = Integer.parseInt(currParams[1]);
//                System.out.println("length: " + seqLength + ", value: " + value);
                int currColIndex = col;
                for (; currColIndex < col + seqLength; currColIndex++) {
                    byteArray[row][currColIndex] = (byte)value;
                }

                if (currColIndex == ReversiConstants.boardWidth) {
                    col = 0;
                    row++;
                }
                else { col = currColIndex; }
            }
            if (words.get(wordIndex).equals("end") == false) {
                System.out.println("An Error has occurred in file parsing, check legality of file lines");
            }
            else { currentState = new ReversiBoardState(byteArray,isBlackTurn); }
        }
        return currentState;
    }

    public void writeNextState(ReversiBoardState nextState){
        String strLine = nextState.bIsBlackMove ? "black," : "white,";
        byte[][] byteArray = nextState.boardStateBeforeMove;
        for (int i = 0; i < ReversiConstants.boardHeight; i++) {
            int j = 0;
            while (j <ReversiConstants.boardWidth) {
                byte val = byteArray[i][j];
                int seqLen = 1;
                while (j + seqLen < ReversiConstants.boardWidth && byteArray[i][j + seqLen] == val) {
                    seqLen++;
                }
                strLine += "" + seqLen + " " + val + ",";
                j += seqLen;
            }
        }
        try {
            Files.write(sharedFile, strLine.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeInitialState(boolean bIsBlackFirst)
    {
        String strLine = bIsBlackFirst ? "black," : "white,";
        strLine += "12 0,12 0,12 0,12 0,12 0,5 0,1 1,1 2,5 0,5 0,1 2,1 1,5 0,12 0,12 0,12 0,12 0,12 0,end" +  "\n";
        try {
            Files.write(sharedFile, strLine.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
