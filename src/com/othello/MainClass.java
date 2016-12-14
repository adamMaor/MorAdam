package com.othello;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Adam on 09/12/2016.
 */
public class MainClass {

    private static FileParser parser;
    private static Logic logic;
    private static GameBoard board;

    public static void main(String[] args) {

        board = new GameBoard();
        logic = new Logic();
        parser = new FileParser();

        SettingsDialog dlg = new SettingsDialog(board, logic, parser);
        dlg.pack();
        dlg.setMinimumSize(dlg.getSize());
        dlg.setSize(500, dlg.getHeight());
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);

        JFrame frame = new JFrame("Reversi");
        frame.setMaximumSize(new Dimension(600,600));
        frame.setContentPane(board.mainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900,1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        logic.generateMove();
    }
}
