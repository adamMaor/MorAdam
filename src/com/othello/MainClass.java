package com.othello;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Adam on 09/12/2016.
 */
public class MainClass {

    private static FileParser parser;
    private static Worker worker;
    private static GameBoard board;

    public static void main(String[] args) {

        board = new GameBoard();
        worker = new Worker();
        parser = new FileParser();

        SettingsDialog dlg = new SettingsDialog(board, worker, parser);
        dlg.pack();
        dlg.setMinimumSize(dlg.getSize());
        dlg.setSize(500, dlg.getHeight());
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);

        JFrame frame = new JFrame("Reversi");
        frame.setMaximumSize(new Dimension(600,600));
        frame.setContentPane(board.mainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        worker.generateMove();
    }
}
