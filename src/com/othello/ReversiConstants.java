package com.othello;

import java.awt.*;

/**
 * Created by Adam on 05/12/2016.
 */
public class ReversiConstants {

    public static int boardHeight = 12;
    public static int boardWidth = 12;
    public static int boardSquare = boardHeight*boardWidth;
    public static Color reversiGreen = new Color(0, 150, 0);
    public static long PCDelayTime = 500;

    public enum Directions {N , NE, E, SE, S ,SW ,W, NW};

}

