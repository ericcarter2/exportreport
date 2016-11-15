import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by MLangreau on 10/11/2016.
 */
public class Menu extends JFrame {
    private JPanel rootPanel;
    private JTextField adminTextField;
    private JPasswordField adminPasswordField;
    private JComboBox comboBox1;
    private JTextField a19216899100TextField;
    private JButton openButton;
    private JButton exportButton;
    private JTextField textField3;
    private JComboBox comboBoxProjects;
    private JButton refreshButton;

    public Menu() {
        setContentPane(rootPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        comboBox1.addItem("http://");
        comboBox1.addItem("https://");

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showSaveDialog(null); //parent component to JFileChooser



                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File file = fc.getSelectedFile();

                    if(!fc.getSelectedFile().getAbsolutePath().endsWith(".csv")){
                        file = new File(fc.getSelectedFile() + ".csv");
                    }
                    try {
                        BufferedWriter o = new BufferedWriter(new FileWriter(file));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if(returnVal != JFileChooser.CANCEL_OPTION)
                    textField3.setText(fc.getSelectedFile().getAbsolutePath() + ".csv");
            }
        });

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JsonReader reader = new JsonReader(comboBox1.getSelectedItem()
                            + a19216899100TextField.getText(),
                            textField3.getText(),
                            adminTextField.getText(),
                            new String(adminPasswordField.getPassword()),
                            comboBoxProjects.getSelectedItem().toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Export completed successfully");
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JsonReader reader = new JsonReader(comboBox1.getSelectedItem()
                            + a19216899100TextField.getText(),
                            textField3.getText(),
                            adminTextField.getText(),
                            new String(adminPasswordField.getPassword()));
                    for (String str : reader.getStrList())
                        comboBoxProjects.addItem(str);

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setVisible(true);

    }
}

