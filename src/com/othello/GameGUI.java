package com.othello;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Main Gui Class
 * Handles all GUI paints and Events
 */
public class GameGUI extends JFrame{
    private SettingsDialog parent;
    private JPanel mainPanel;
    private JButton nextButton;
    private JCheckBox autoPlayCheckBox;
    private JPanel boardPanel;
    private JProgressBar progressBar;
    private JLabel gameStatusLabel;
    private JTextPane logTextPane;
    private JPanel currentColorPanel;
    private JPanel statusBar;
    private JLabel nextMoveLabel;
    private CirclePanel currentColorCirclePanel;
    private static CirclePanel[][] boardGuiArray;
    private int currentBlack = 2;
    private int currentWhite = 2;
    private int currentProgress = 0;
    private String blackPlayerType = "Human";
    private String whitePlayerType = "Human";
    private boolean bCurrentPlayerIsBlack = true;
    private GameLogic gameLogic = null;
    private boolean pcRunning;

    public GameGUI(SettingsDialog parent) {
        this.parent = parent;
        pcRunning = false;
        this.setTitle("Reversi");
        this.setContentPane(mainPanel);
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        this.setSize(925, 1000);
        this.setLocationRelativeTo(null);
        boardGuiArray = new CirclePanel[ReversiConstants.BoardSize.boardHeight][ReversiConstants.BoardSize.boardWidth];
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++) {
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth; j++) {
                CirclePanel guiObject = new CirclePanel(ReversiConstants.Colors.reversiGreen);
                boardPanel.add(guiObject);
            }
        }
        gameStatusLabel.setMinimumSize(new Dimension(200,gameStatusLabel.getHeight()));
        gameStatusLabel.setMaximumSize(new Dimension(200, gameStatusLabel.getHeight()));
        gameStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gameStatusLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        progressBar.setMaximum(ReversiConstants.BoardSize.boardSquare);
        updateProgressBar();
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean bIsAutoSelected = autoPlayCheckBox.isSelected();
                nextButton.setEnabled(!bIsAutoSelected);
                gameLogic.generateMove();
            }
        });
        autoPlayCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSelected = autoPlayCheckBox.isSelected();
                gameLogic.setAutoPlay(isSelected);
                if (!isSelected) {
                    Thread setNextAvail = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (pcRunning) {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            nextButton.setEnabled(true);
                        }
                    });
                    setNextAvail.start();
                }
            }
        });
    }

    public void repaintBoard(ReversiBoardState boardState, ReversiBoardState lastState, boolean bShowLastMove, boolean bShowAvailableMoves, ArrayList<ReversiBoardState> nextAvailableMovesList) {
//        boardPanel.removeAll();
        bCurrentPlayerIsBlack = boardState.bIsBlackMove;
        setCurrentMoveLabel();
        setCurrentPlayerIndicator(bCurrentPlayerIsBlack);
        byte[][] boardArray = boardState.boardStateBeforeMove;
        byte[][] lastBoardArray = lastState.boardStateBeforeMove;
        for (int i = 0; i < ReversiConstants.BoardSize.boardHeight; i++){
            for (int j = 0; j < ReversiConstants.BoardSize.boardWidth; j++) {
                final int finalI = i;
                final int finalJ = j;
                byte currState = boardArray[i][j];
                byte prevState = lastBoardArray[i][j];
                if (currState != prevState || boardGuiArray[i][j] == null) {
                    boardPanel.remove(i * ReversiConstants.BoardSize.boardHeight + j);
                    boardGuiArray[i][j] = null;
                    if (prevState != ReversiConstants.CubeStates.none) {
                        if (prevState == ReversiConstants.CubeStates.black) {
                            currentBlack--;
                        }
                        else {
                            currentWhite--;
                        }
                    }
                    Color color = ReversiConstants.Colors.reversiGreen;
                    switch (currState) {
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
                    boardPanel.add(guiObject, i * ReversiConstants.BoardSize.boardHeight + j);
                }
            }
        }
        if (bShowAvailableMoves) {
            markAvailableMoves(boardState, nextAvailableMovesList);
        }
        if (bShowLastMove) {
            markLastMove(boardState, lastState);
        }
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
        gameStatusLabel.setText("White (" + whitePlayerType + "):  " + currentWhite + "   ,   Black (" + blackPlayerType + "):  " + currentBlack);
        currentProgress = currentBlack + currentWhite;
        progressBar.setValue(currentProgress);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        boardPanel = new JPanel(new GridLayout(ReversiConstants.BoardSize.boardHeight, ReversiConstants.BoardSize.boardHeight));
        currentColorPanel = new JPanel(new GridLayout(1,1));
    }

    public void guiObjectClicked(final int row, final int col){
        if ((blackPlayerType.equals("Human") && bCurrentPlayerIsBlack) ||  (whitePlayerType.equals("Human") && !bCurrentPlayerIsBlack)) {
            if (boardGuiArray[row][col].getColor() == ReversiConstants.Colors.reversiGreen) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        gameLogic.getHumanMove(row, col);
                    }
                });

            }
        }
    }

    public void init(GameLogic gameLogic, byte whitePlayerType, byte blackPlayerType) {
        this.gameLogic = gameLogic;
        this.blackPlayerType = blackPlayerType != 0 ? (blackPlayerType == 1 ? "PC" : "Another PC" ) : "Human";
        this.whitePlayerType = whitePlayerType != 0 ? (whitePlayerType == 1 ? "PC" : "Another PC" ) : "Human";
        setCurrentMoveLabel();
        updateProgressBar();
    }

    private void setCurrentMoveLabel() {
        String strNextMove = "Current Move (";
        strNextMove += bCurrentPlayerIsBlack ? blackPlayerType : whitePlayerType;
        strNextMove += ") ";
        nextMoveLabel.setText(strNextMove);
    }

    public void gameIsOver() {
        String strRes = currentBlack > currentWhite ? "Black Wins !!! " : currentBlack == currentWhite ? "It Was A Tie !" : "White Wins !!!";
        strRes += "\n" + "The Score Was: \n    White (" + whitePlayerType + "):" + currentWhite + "\n    Black (" + blackPlayerType + "): " + currentBlack + "\n";
        strRes += gameLogic.getGameSum();
        JOptionPane.showMessageDialog(null, "Game Is Over, " + strRes);
        this.setVisible(false);
        parent.pack();
        parent.setVisible(true);
        this.dispose();
    }

    private void confirmExit() {
        //Custom button text
        Object[] options = {"Yes, sure",
                "No, bring me back"};
        int n = JOptionPane.showOptionDialog(this,
                "Are you sure you want to exit?",
                "Done playing?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        if (n == 0) {
            System.exit(0);
        }
    }

    public void playerHadChanged() {
        if (blackPlayerType.equals("Human") ||  whitePlayerType.equals("Human")) {
            String strRes = "Player had changed because no legal moves were available";
            JOptionPane.showMessageDialog(null, strRes);
        }
    }

    private static void markAvailableMoves(ReversiBoardState currentState, ArrayList<ReversiBoardState> nextAvailableMovesList) {
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row ++ ) {
             for (int col = 0 ; col < ReversiConstants.BoardSize.boardWidth; col++ ) {
                 Color color = ReversiConstants.Colors.reversiGreen;
                 for (int i = 0 ; i < nextAvailableMovesList.size(); i++) {
                     byte currentCubeState = currentState.boardStateBeforeMove[row][col];
                     if (currentCubeState == ReversiConstants.CubeStates.none &&
                            currentCubeState != nextAvailableMovesList.get(i).boardStateBeforeMove[row][col]) {
                         color = Color.green;
                         break;
                     }
                 }
                 boardGuiArray[row][col].setBackground(color);
             }
        }
    }

    private void markLastMove(ReversiBoardState currentState, ReversiBoardState lastState) {
        for (int row = 0; row < ReversiConstants.BoardSize.boardHeight; row ++ ) {
            for (int col = 0; col < ReversiConstants.BoardSize.boardWidth; col++) {
                if (lastState.boardStateBeforeMove[row][col] == ReversiConstants.CubeStates.none
                        && currentState.boardStateBeforeMove[row][col] != ReversiConstants.CubeStates.none)
                {
                    boardGuiArray[row][col].setBackground(Color.blue);
                    return;
                }
            }
        }
    }

    public void setPcRunning(boolean bIsRunning) {
        this.pcRunning = bIsRunning;
        if (autoPlayCheckBox.isSelected() == false) {
            nextButton.setEnabled(!bIsRunning);
        }
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
            int d = Math.min(size.width, size.height) - 7;
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
