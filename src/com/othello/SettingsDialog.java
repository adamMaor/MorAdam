package com.othello;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox whiteCombo;
    private JSpinner depthSpinner;
    private JSpinner delaySpinner;
    private JComboBox blackCombo;
    private JComboBox firstMoveCombo;
    private JTextField filePathField;
    private JButton generateFileButton;
    private JCheckBox createdByMeCheckBox;
    private JCheckBox showAvailableMovesGreenCheckBox;
    private JCheckBox showLastMoveBlueCheckBox;
    private JCheckBox useAlphaBetaPruningCheckBox;
    private JCheckBox useCacheMaxDepthCheckBox;
    private JCheckBox blackh1CheckBox;
    private JCheckBox whiteh1CheckBox;
    private JCheckBox blackh2CheckBox;
    private JCheckBox whiteh2CheckBox;
    private JCheckBox blackh3CheckBox;
    private JCheckBox whiteh3CheckBox;
    private JCheckBox blackh4CheckBox;
    private JCheckBox whiteh4CheckBox;
    private JLabel h1Label;
    private JLabel h2Label;
    private JLabel h3Label;
    private JLabel h4Label;
    private JCheckBox whiteh5CheckBox;
    private JCheckBox blackh5CheckBox;
    private JLabel h5Label;

    private static FileParser fileParser;
    private static GameLogic gameLogic;
    private static GameGUI gameGUI;

    public SettingsDialog(GameLogic gameLogic, FileParser fileParser) {

        this.gameLogic = gameLogic;
        this.fileParser = fileParser;
        setContentPane(contentPane);
        setTitle("Set your Reversi Game Up");
        pack();
        setSize(500, getHeight());
        setMaximumSize(getSize());
        setResizable(false);
        setLocationRelativeTo(null);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        h1Label.setText("Discs Count (" + ReversiConstants.HeuristicsWeight.h1 + "): ");
        h2Label.setText("Corners (" + ReversiConstants.HeuristicsWeight.h2 + "): ");
        h3Label.setText("Stability (" + ReversiConstants.HeuristicsWeight.h3 + "): ");
        h4Label.setText("Frontiers (" + ReversiConstants.HeuristicsWeight.h4 + "): ");
        h5Label.setText("Neighbors of Corners (" + ReversiConstants.HeuristicsWeight.h5 + "): ");

        whiteCombo.setSelectedIndex(ReversiConstants.PlayerTypes.human);
        blackCombo.setSelectedIndex(ReversiConstants.PlayerTypes.pc);
        firstMoveCombo.setSelectedIndex(ReversiConstants.PlayerTypes.human);

        SpinnerModel depthModel = new SpinnerNumberModel(5, 1, 10, 1);
        depthSpinner.setModel(depthModel);
        JComponent editor = depthSpinner.getEditor();
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)editor;
        spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);

        SpinnerModel DelayModel = new SpinnerNumberModel(250, 0, 1500, 250);
        delaySpinner.setModel(DelayModel);
        JComponent editor2 = delaySpinner.getEditor();
        JSpinner.DefaultEditor delayEditor = (JSpinner.DefaultEditor)editor2;
        delayEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);

        showAvailableMovesGreenCheckBox.setSelected(true);
        showLastMoveBlueCheckBox.setSelected(true);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        generateFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filePathField.setText(generateFile());
            }
        });
    }

    private String generateFile() {
        String filePath = "";
        try {
            Path path = Paths.get("reversiComFile.txt");
            if (Files.exists(path) == false)
            {
                Files.createFile(path);
            }
            filePath = path.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        createdByMeCheckBox.setSelected(true);
        return filePath;
    }

    private void onOK() {
        boolean bIsFirstMoveBlack = firstMoveCombo.getSelectedIndex() == 1;
        boolean bIsCreatedHere = createdByMeCheckBox.isSelected();
        byte whitePlayerType = (byte)whiteCombo.getSelectedIndex();
        byte blackPlayerType = (byte)blackCombo.getSelectedIndex();
        HashMap<String,Boolean> blackPlayerHeuristicsMap = new HashMap<String, Boolean>();
        // fill black player heuristics
        blackPlayerHeuristicsMap.put("h1", blackh1CheckBox.isSelected());
        blackPlayerHeuristicsMap.put("h2", blackh2CheckBox.isSelected());
        blackPlayerHeuristicsMap.put("h3", blackh3CheckBox.isSelected());
        blackPlayerHeuristicsMap.put("h4", blackh4CheckBox.isSelected());
        blackPlayerHeuristicsMap.put("h5", blackh5CheckBox.isSelected());
        HashMap<String,Boolean> whitePlayerHeuristicsMap = new HashMap<String, Boolean>();
        // fill white player heuristics
        whitePlayerHeuristicsMap.put("h1", whiteh1CheckBox.isSelected());
        whitePlayerHeuristicsMap.put("h2", whiteh2CheckBox.isSelected());
        whitePlayerHeuristicsMap.put("h3", whiteh3CheckBox.isSelected());
        whitePlayerHeuristicsMap.put("h4", whiteh4CheckBox.isSelected());
        whitePlayerHeuristicsMap.put("h5", whiteh5CheckBox.isSelected());
        int depth = (Integer) depthSpinner.getValue();
        boolean isAlphaBeta = useAlphaBetaPruningCheckBox.isSelected();
        boolean useCache = useCacheMaxDepthCheckBox.isSelected();
        int delayTime = (Integer) delaySpinner.getValue();
        String filePath = filePathField.getText();
        boolean isShowAvailableMoves = showAvailableMovesGreenCheckBox.isSelected();
        boolean isShowLastMove = showLastMoveBlueCheckBox.isSelected();
        if (whitePlayerType == ReversiConstants.PlayerTypes.otherPC || blackPlayerType == ReversiConstants.PlayerTypes.otherPC) {
            if (whitePlayerType == blackPlayerType) {
                JOptionPane.showMessageDialog(null, "Both Players can't be Other PC !!!!\nPlease change your selections");
                return;
            }
            if (filePath.isEmpty() || Files.exists(Paths.get(filePath)) == false)
            {
                JOptionPane.showMessageDialog(null, "When Playing vs other pc, you must enter a shared file path !!!!");
                return;
            }
        }
        gameGUI = new GameGUI(this);
        fileParser.init(filePath, bIsCreatedHere, bIsFirstMoveBlack);
        gameLogic.init(gameGUI, fileParser, whitePlayerType, blackPlayerType, bIsFirstMoveBlack, depth, isAlphaBeta, useCache, delayTime, isShowAvailableMoves, isShowLastMove, blackPlayerHeuristicsMap, whitePlayerHeuristicsMap);
        gameGUI.init(gameLogic, whitePlayerType, blackPlayerType);
        this.setVisible(false);
        gameGUI.setVisible(true);
    }

    private void onCancel() {
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
            dispose();
            System.exit(0);
        }

    }

}
