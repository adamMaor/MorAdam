package com.othello;

import java.awt.*;

/**
 * Created by Adam on 05/12/2016.
 */
public class ReversiConstants {

    public static class BoardSize {
        public static final int boardHeight = 12;
        public static final int boardWidth = 12;
        public static final int boardSquare = boardHeight * boardWidth;
        public static final String initBoardString = "12 0,12 0,12 0,12 0,12 0,5 0,1 1,1 2,5 0,5 0,1 2,1 1,5 0,12 0,12 0,12 0,12 0,12 0,end\n";
    }

    public static class Colors {
        public static Color reversiGreen = new Color(0, 110, 0);
        public static Color reversiDarkGreen = new Color(0, 60, 0);
        public static Color reversiWhite = new Color(230,230,220);
        public static Color reversiBlack = new Color(30,30,30);
        public static Color lightMiddle = new Color(180,180,180);
        public static Color darkMiddle = new Color (50,50,50);
    }

    public static class CubeStates {
        public static final byte none = 0;
        public static final byte white = 1;
        public static final byte black = 2;
    }

    public static class PlayerTypes {
        public static final byte human = 0;
        public static final byte pc = 1;
        public static final byte otherPC = 2;
    }

    public static class GameStatus {
        public static final byte noMoreMoves = 0;
        public static final byte noMovesForCurrentPlayer = 1;
        public static final byte currentPlayerCanPlay = 2;
    }

    public static class Performance {
        public static final byte maxCacheDepth = 7;
    }

    public static class HeuristicsWeight {
        public static final byte h1 = 10 ;
        public static final byte h2 = 20 ;
        public static final byte h3 = 15 ;
        public static final byte h4 = 10 ;
        public static final byte h5 = 15;
        public static final int maxUtilityScore = (h1 + h2 + h3 + h4 + h5) * 50;
        public static final int minUtilityScore = -maxUtilityScore;
    }
}
