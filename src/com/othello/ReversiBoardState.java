package com.othello;

import java.util.Arrays;

public class ReversiBoardState {
    public boolean bIsBlackMove;
    public byte[][] boardStateBeforeMove;

    public ReversiBoardState(byte[][] boardStateBeforeMove, boolean bIsBlackMove) {
        this.boardStateBeforeMove = boardStateBeforeMove;
        this.bIsBlackMove = bIsBlackMove;
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
