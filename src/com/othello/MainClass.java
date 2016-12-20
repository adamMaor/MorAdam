package com.othello;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Adam on 09/12/2016.
 */
public class MainClass {

    private static FileParser fileParser;
    private static GameLogic gameLogic;
    private static LogicUtils logicUtils;

    public static void main(String[] args) {

        logicUtils = new LogicUtils();
        gameLogic = new GameLogic(logicUtils);
        fileParser = new FileParser();

        SettingsDialog dlg = new SettingsDialog(gameLogic, fileParser);

        dlg.setVisible(true);
    }
}
