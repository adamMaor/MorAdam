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
    private Worker worker = null;

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
                Color color = ReversiConstants.reversiGreen;
                switch (positionValue) {
                    case 1:
                        color =   ReversiConstants.reversiWhite;
                        currentWhite++;
                        break;
                    case 2:
                        color =   ReversiConstants.reversiBlack;
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

    private void setCurrentPlayerIndicator(boolean isCurrentPlayerBlack)
    {
        Color colorToSet = ReversiConstants.reversiWhite;
        if (isCurrentPlayerBlack) {
            colorToSet = ReversiConstants.reversiBlack;
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
            if (boardGuiArray[row][col].getColor() == ReversiConstants.reversiGreen) {
                worker.getHumanMove(row, col);
            }
        }
    }

    public Container mainPanel() {
        return mainPanel;
    }

    public void init(Worker worker, byte whitePlayerType, byte blackPlayerType) {
        this.worker = worker;
        this.blackPlayerType = blackPlayerType != 0 ? (blackPlayerType == 1 ? "PC" : "Another PC" ) : "Human";
        this.whitePlayerType = whitePlayerType != 0 ? (whitePlayerType == 1 ? "PC" : "Another PC" ) : "Human";
    }

    public void gameIsOver() {
        String strRes = "Black (" + blackPlayerType + "): " + currentBlack + ", White (" + whitePlayerType + "):" + currentWhite;
        JOptionPane.showMessageDialog(null, "Game Is Over !!! \n" + strRes);
        System.exit(0);
    }

    private static class CirclePanel extends JPanel {

        Color mainColor = ReversiConstants.reversiGreen;
        Color midColor1 = ReversiConstants.reversiGreen;
        Color midColor2 = ReversiConstants.reversiGreen;

        public CirclePanel(Color color) {
            this.setPreferredSize(new Dimension(20, 20));
            this.setForeground(ReversiConstants.reversiGreen);
            this.setBackground(ReversiConstants.reversiGreen);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setColor(color);

        }

        public void setColor(Color color) {
            if (color.equals(ReversiConstants.reversiGreen))
            {
                mainColor = ReversiConstants.reversiGreen;
                midColor1 = ReversiConstants.reversiGreen;
                midColor2 = ReversiConstants.reversiGreen;
            }
            else if (color.equals(ReversiConstants.reversiWhite))
            {
                mainColor = ReversiConstants.reversiWhite;
                midColor1 = ReversiConstants.lightMiddle;
                midColor2 = ReversiConstants.darkMiddle;
            }
            else if (color.equals(ReversiConstants.reversiBlack))
            {
                mainColor = ReversiConstants.darkMiddle;
                midColor1 = ReversiConstants.reversiBlack;
                midColor2 = Color.BLACK;
            }
        }

        public Color getColor() {
            return this.getForeground();
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
