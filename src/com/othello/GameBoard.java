package com.othello;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Adam on 04/12/2016.
 */
public class GameBoard {
    private JButton pauseButton;
    private JPanel mainPanel;
    private JButton startButton;
    private JCheckBox autoPlayCheckBox;
    private JPanel boardPanel;
    private JProgressBar progressBar;
    private JLabel gameStatusLabel;
    private JTextPane logTextPane;
    private CirclePanel[][] boardGuiArray;
    private int currentBlack = 2;
    private int currentWhite = 2;
    private int currentProgress = 4;

    public GameBoard() {
        boardGuiArray = new CirclePanel[ReversiConstants.boardHeight][ReversiConstants.boardWidth];
        progressBar.setMaximum(ReversiConstants.boardHeight * ReversiConstants.boardWidth);
        updateProgressBar();
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Starting Play");
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Pausing Play");
            }
        });
    }

    public void repaintBoard(byte[][] boardArray) {
        currentBlack = 0;
        currentWhite = 0;
        for (int i = 0; i < ReversiConstants.boardHeight; i++){
            for (int j = 0; j < ReversiConstants.boardWidth; j++) {
                CirclePanel guiObject = boardGuiArray[i][j];
                byte positionValue = boardArray[i][j];
                Color color = ReversiConstants.reversiGreen;
                switch (positionValue) {
                    case 1:
                        color =   Color.white;
                        currentWhite++;
                        break;
                    case 2:
                        color =   Color.black;
                        currentBlack++;
                }
                guiObject.setColor(color);
            }
        }
        updateProgressBar();
    }

    private void updateProgressBar() {
        gameStatusLabel.setText("Black (): " + currentBlack + ", White ():" + currentWhite);
        currentProgress = currentBlack + currentWhite;
        progressBar.setValue(currentProgress);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        boardPanel = new JPanel(new GridLayout(ReversiConstants.boardHeight, ReversiConstants.boardHeight));
        for (int i = 0; i < ReversiConstants.boardHeight; i++){
            for (int j = 0; j < ReversiConstants.boardWidth; j++) {
                CirclePanel guiObject = new CirclePanel();
                boardGuiArray[i][j] = guiObject;
                boardPanel.add(guiObject);

            }
        }
    }

    public Container mainPanel() {
        return mainPanel;
    }

    private static class CirclePanel extends JPanel {
        public CirclePanel() {
            this.setPreferredSize(new Dimension(20, 20));
            this.setForeground(ReversiConstants.reversiGreen);
            this.setBackground(ReversiConstants.reversiGreen);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    CirclePanel.this.update();
                }
            });
        }
        // this is only for demo session - no logic here - need to create logic
        public void update() {
            Color colorToSet = Color.black;
            if (this.getForeground() == Color.black) {
                colorToSet = Color.white;
            }
            setColor(colorToSet);

        }

        public void setColor(Color color) {
            this.setForeground(color);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension size = this.getSize();
            int d = Math.min(size.width, size.height) - 10;
            int x = (size.width - d) / 2;
            int y = (size.height - d) / 2;
            g.fillOval(x, y, d, d);
            g.drawOval(x, y, d, d);
        }
    }


}
