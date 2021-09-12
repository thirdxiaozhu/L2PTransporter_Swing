package zoujiaxv;

import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.net.UnknownHostException;

public class Main {
    private static void createGUI() throws UnknownHostException {
        FlatIntelliJLaf.install();
        JFrame frame = new JFrame("L2P文件传输");
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        frame.setResizable(false);
    }

    /**
     * 主函数
     * @param args
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    createGUI();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
