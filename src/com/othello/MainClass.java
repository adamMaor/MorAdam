package com.othello;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Adam on 09/12/2016.
 */
public class MainClass {

    private static FileParser fileParser;
    private static GameLogic gameLogic;
    private static GameGUI gameGUI;

    public static void main(String[] args) {

        gameGUI = new GameGUI();
        gameLogic = new GameLogic();
        fileParser = new FileParser();

        SettingsDialog dlg = new SettingsDialog(gameGUI, gameLogic, fileParser);
        dlg.pack();
        dlg.setMinimumSize(dlg.getSize());
        dlg.setSize(500, dlg.getHeight());
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Reversi");
                frame.setMaximumSize(new Dimension(600, 600));
                frame.setContentPane(gameGUI.mainPanel());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(900,1000);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            }
        });

//        gameLogic.generateMove();
    }
}
