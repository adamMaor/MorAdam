package com.othello;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox whiteCombo;
    private JSpinner depthSpinner;
    private JComboBox blackCombo;
    private JComboBox firstMoveCombo;
    private JTextField filePathField;
    private JButton generateFileButton;

    private static FileParser parser;
    private static Worker worker;
    private static GameBoard board;

    public SettingsDialog(GameBoard board, Worker worker, FileParser parser) {

        this.board = board;
        this.worker = worker;
        this.parser = parser;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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
        return filePath;
    }

    private void onOK() {
        boolean bIsFirstMoveBlack = firstMoveCombo.getSelectedIndex() == 0;
        byte whitePlayerType = (byte)whiteCombo.getSelectedIndex();
        byte blackPlayerType = (byte)blackCombo.getSelectedIndex();
        int depth = (Integer) depthSpinner.getValue();
        String filePath = filePathField.getText();

        System.out.print("your values: " + bIsFirstMoveBlack + whitePlayerType + blackPlayerType + depth + filePath);

        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
