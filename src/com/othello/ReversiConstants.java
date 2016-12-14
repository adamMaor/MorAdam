package com.othello;

import java.awt.*;

/**
 * Created by Adam on 05/12/2016.
 */
public class ReversiConstants {

    public static int boardHeight = 12;
    public static int boardWidth = 12;
    public static int boardSquare = boardHeight*boardWidth;

    public static class Colors {
        public static Color reversiGreen = new Color(0, 110, 0);
        public static Color reversiDarkGreen = new Color(0, 60, 0);
        public static Color reversiWhite = Color.white;
        public static Color reversiBlack = new Color(30,30,30);
        public static Color lightMiddle = new Color(180,180,180);
        public static Color darkMiddle = new Color (60,60,60);

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
}

