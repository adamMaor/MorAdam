package com.othello;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private static FileParser fileParser;
    private static GameLogic gameLogic;
    private static GameGUI gameGUI;

    public SettingsDialog(GameGUI gameGUI, GameLogic gameLogic, FileParser fileParser) {

        this.gameGUI = gameGUI;
        this.gameLogic = gameLogic;
        this.fileParser = fileParser;

        setContentPane(contentPane);
        setTitle("Set your Reversi Game Up");
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        SpinnerModel depthModel = new SpinnerNumberModel(4, 1, 10, 1);
        depthSpinner.setModel(depthModel);

        SpinnerModel DelayModel = new SpinnerNumberModel(250, 50, 1000, 50);
        delaySpinner.setModel(DelayModel);

        JComponent editor = depthSpinner.getEditor();
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)editor;
        spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);

        JComponent editor2 = delaySpinner.getEditor();
        JSpinner.DefaultEditor delayEditor = (JSpinner.DefaultEditor)editor2;
        delayEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
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
        int depth = (Integer) depthSpinner.getValue();
        int delayTime = (Integer) delaySpinner.getValue();
        String filePath = filePathField.getText();
        if (filePath.isEmpty() || Files.exists(Paths.get(filePath)) == false)
        {
            JOptionPane.showMessageDialog(null, "You Must Enter A shared File !!!!");
            return;
        }

        fileParser.init(filePath, bIsCreatedHere, bIsFirstMoveBlack);
        gameLogic.init(gameGUI, fileParser, whitePlayerType, blackPlayerType, depth, delayTime);
        gameGUI.init(gameLogic, whitePlayerType, blackPlayerType);
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
        System.exit(0);

    }

}
