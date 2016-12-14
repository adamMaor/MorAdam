package com.othello;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

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
    private JPanel currentColorPanel;
    private JPanel statusBar;
    private CirclePanel currentColorCirclePanel;
    private CirclePanel[][] boardGuiArray;
    private int currentBlack = 2;
    private int currentWhite = 2;
    private int currentProgress = 4;
    private String blackPlayerType = "Human";
    private String whitePlayerType = "Human";
    private boolean bCurrentPlayerIsBlack = true;
    private Logic logic = null;

    public GameBoard() {
        boardGuiArray = new CirclePanel[ReversiConstants.boardHeight][ReversiConstants.boardWidth];
        progressBar.setMaximum(ReversiConstants.boardSquare);
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

    public void repaintBoard(ReversiBoardState boardState) {
        currentBlack = 0;
        currentWhite = 0;
        boardPanel.removeAll();
        bCurrentPlayerIsBlack = boardState.bIsBlackMove;
        setCurrentPlayerIndicator(bCurrentPlayerIsBlack);
        byte[][] boardArray = boardState.boardStateBeforeMove;
        for (int i = 0; i < ReversiConstants.boardHeight; i++){
            for (int j = 0; j < ReversiConstants.boardWidth; j++) {
                final int finalI = i;
                final int finalJ = j;
                boardGuiArray[i][j] = null;
                byte positionValue = boardArray[i][j];
                Color color = ReversiConstants.Colors.reversiGreen;
                switch (positionValue) {
                    case ReversiConstants.CubeStates.white:
                        color =   ReversiConstants.Colors.reversiWhite;
                        currentWhite++;
                        break;
                    case ReversiConstants.CubeStates.black:
                        color =   ReversiConstants.Colors.reversiBlack;
                        currentBlack++;
                }
                CirclePanel guiObject = new CirclePanel(color);
                guiObject.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        guiObjectClicked(finalI, finalJ);
                    }
                });
                boardGuiArray[i][j] = guiObject;
                boardPanel.add(guiObject);
            }
        }
        System.gc();
        updateProgressBar();
    }

    private void setCurrentPlayerIndicator(boolean isCurrentPlayerBlack) {
        Color colorToSet = ReversiConstants.Colors.reversiWhite;
        if (isCurrentPlayerBlack) {
            colorToSet = ReversiConstants.Colors.reversiBlack;
        }
        currentColorPanel.removeAll();
        currentColorCirclePanel = new CirclePanel(colorToSet);
        currentColorPanel.add(currentColorCirclePanel);
    }

    private void updateProgressBar() {
        gameStatusLabel.setText("Black (" + blackPlayerType + "): " + currentBlack + ", White (" + whitePlayerType + "):" + currentWhite);
        currentProgress = currentBlack + currentWhite;
        progressBar.setValue(currentProgress);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        boardPanel = new JPanel(new GridLayout(ReversiConstants.boardHeight, ReversiConstants.boardHeight));
        currentColorPanel = new JPanel(new GridLayout(1,1));
    }

    public void guiObjectClicked(int row, int col){
        if ((blackPlayerType.equals("Human") && bCurrentPlayerIsBlack) ||  (whitePlayerType.equals("Human") && !bCurrentPlayerIsBlack)) {
            if (boardGuiArray[row][col].getColor() == ReversiConstants.Colors.reversiGreen) {
                logic.getHumanMove(row, col);
            }
        }
    }

    public Container mainPanel() {
        return mainPanel;
    }

    public void init(Logic logic, byte whitePlayerType, byte blackPlayerType) {
        this.logic = logic;
        this.blackPlayerType = blackPlayerType != 0 ? (blackPlayerType == 1 ? "PC" : "Another PC" ) : "Human";
        this.whitePlayerType = whitePlayerType != 0 ? (whitePlayerType == 1 ? "PC" : "Another PC" ) : "Human";
    }

    public void gameIsOver() {
        String strRes = currentBlack > currentWhite ? "Black Wins !!! " : currentBlack == currentWhite ? "It Was A Tie !" : "White Wins !!!";
        strRes += "\n" + "The Score Was: Black (" + blackPlayerType + "): " + currentBlack + ", White (" + whitePlayerType + "):" + currentWhite;
        JOptionPane.showMessageDialog(null, "Game Is Over, " + strRes);
    }

    private static class CirclePanel extends JPanel {

        Color mainColor = ReversiConstants.Colors.reversiGreen;
        Color midColor1 = ReversiConstants.Colors.reversiGreen;
        Color midColor2 = ReversiConstants.Colors.reversiGreen;

        public CirclePanel(Color color) {
            this.setPreferredSize(new Dimension(20, 20));
            this.setForeground(ReversiConstants.Colors.reversiGreen);
            this.setBackground(ReversiConstants.Colors.reversiGreen);
            this.setBorder(BorderFactory.createLineBorder(ReversiConstants.Colors.reversiDarkGreen));
            setColor(color);

        }

        public void setColor(Color color) {
            if (color.equals(ReversiConstants.Colors.reversiGreen))
            {
                mainColor = ReversiConstants.Colors.reversiGreen;
                midColor1 = ReversiConstants.Colors.reversiGreen;
                midColor2 = ReversiConstants.Colors.reversiGreen;
            }
            else if (color.equals(ReversiConstants.Colors.reversiWhite))
            {
                mainColor = ReversiConstants.Colors.reversiWhite;
                midColor1 = ReversiConstants.Colors.lightMiddle;
                midColor2 = ReversiConstants.Colors.darkMiddle;
            }
            else if (color.equals(ReversiConstants.Colors.reversiBlack))
            {
                mainColor = ReversiConstants.Colors.darkMiddle;
                midColor1 = ReversiConstants.Colors.reversiBlack;
                midColor2 = Color.BLACK;
            }
        }

        public Color getColor() {
            return mainColor;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension size = this.getSize();
            int d = Math.min(size.width, size.height) - 10;
            int x = (size.width - d) / 2;
            int y = (size.height - d) / 2;
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();
            Point2D center = new Point2D.Float(2*w/3, h/3);
            float[] dist = {0.9f, 0.97f, 1.0f};
            Color[] colors = {mainColor, midColor1, midColor2};
            RadialGradientPaint gp = new RadialGradientPaint(center, (d+20)/2, dist, colors);  //GradientPaint(0, 0, mainColor, w/3, h/3, color2, false);
            g2d.setPaint(gp);
            g2d.fillOval(x, y, d, d);
            g2d.drawOval(x, y, d, d);


        }
    }
}
