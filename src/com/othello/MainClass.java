package com.othello;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Adam on 09/12/2016.
 */
public class MainClass {

    private static FileParser fileParser;
    private static GameLogic gameLogic;

    public static void main(String[] args) {

        gameLogic = new GameLogic();
        fileParser = new FileParser();

        SettingsDialog dlg = new SettingsDialog(gameLogic, fileParser);
        dlg.pack();
        dlg.setSize(500, dlg.getHeight());
        dlg.setMinimumSize(dlg.getSize());
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);



//        gameLogic.generateMove();
    }
}
