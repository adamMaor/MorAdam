package com.othello;

import java.util.Arrays;
import java.util.zip.CRC32;

public class ReversiBoardState {
    public boolean bIsBlackMove;
    public byte[][] boardStateBeforeMove;

    public ReversiBoardState(byte[][] boardStateBeforeMove, boolean bIsBlackMove) {
        this.boardStateBeforeMove = boardStateBeforeMove;
        this.bIsBlackMove = bIsBlackMove;
    }

    @Override
    public int hashCode() {
        CRC32 crc = new CRC32();
        byte[] singleDimArray = new byte[ReversiConstants.BoardSize.boardHeight * ReversiConstants.BoardSize.boardWidth];
        int indexInSingle = 0;
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row++) {
            for (int col = 0; col <ReversiConstants.BoardSize.boardWidth; col++) {
                singleDimArray[indexInSingle++] = boardStateBeforeMove[row][col];
            }
        }
        crc.update(singleDimArray);
        int value = (int) (crc.getValue());
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        ReversiBoardState other = (ReversiBoardState) obj;
        if (other.bIsBlackMove == this.bIsBlackMove) {
            for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row++) {
                for (int col = 0; col < ReversiConstants.BoardSize.boardWidth; col++) {
                    if (other.boardStateBeforeMove[row][col] != this.boardStateBeforeMove[row][col]) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String strRes = "ReversiBoardState{" +
                "bIsBlackMove=" + bIsBlackMove + "\n";
        for (int i = 0 ; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth; j++) {
                strRes += ("" + boardStateBeforeMove[i][j] +", ");
            }
            strRes += "\n";
        }
        return strRes;
    }
}
