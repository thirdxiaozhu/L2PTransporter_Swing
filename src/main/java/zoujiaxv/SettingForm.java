package zoujiaxv;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class SettingForm {

    public JPanel MyPanel;
    private JList FunctionList;
    private JPanel GeneralPanel;
    private JPanel Card;
    private JLabel InfoTitle;
    private JPanel InfoPanel;
    private JPanel InfoModelPanel;
    private JLabel ToolTitle;
    private JLabel GeneralTitle;
    private JLabel InfoIcon;
    private JTextField portField;
    private JTextField filePathField;
    private JButton clearCache;
    private JButton NakButton;
    private JButton AckButton;
    private JButton pathChoose;
    private CardLayout cardLayout;
    private DefaultListModel listModel;
    private ButtonGroup btnGroupHashModel;
    private MainForm mainForm;
    private final JFrame jFrame;


    public SettingForm(MainForm mainForm, JFrame jFrame) {
        this.mainForm = mainForm;
        this.jFrame = jFrame;

        initList();
        addCard();
        initPanel();

        FunctionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                doValueChange();
            }
        });
    }

    /** 将标签页添加到Layout中 */
    private void doValueChange() {
        if(FunctionList.isSelectedIndex(0)){
            cardLayout.show(Card , "0");
        }
        else if(FunctionList.isSelectedIndex(1)){
            cardLayout.show(Card , "1");
        }
    }

    /** 添加左侧目录标签 */
    private void initList() {
        FunctionList.setBorder(BorderFactory.createEtchedBorder(0));
        String[] listData = {"常规设置" , "关于"};
        listModel = new DefaultListModel();
        for(String s:listData){
            listModel.addElement(s);
        }
        FunctionList.setModel(listModel);
    }

    /** 向CardLayout中添加六个面板，承载六个不同功能 */
    private void addCard() {
        cardLayout = new CardLayout();
        Card.setLayout(cardLayout);
        Card.add(GeneralPanel,"0");
        Card.add(InfoPanel, "1");
    }

    /** 设置标签对应的面板 */
    private void initPanel() {
        initGeneralPanel();
        initInfoPanel();
    }

    /** 设置常规设置面板 */
    private void initGeneralPanel() {
        GeneralTitle.setFont(new Font("Dialog" , Font.BOLD, 25));
        portField.setText(mainForm.port);
        filePathField.setText(mainForm.filePath);
        AckButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Document configXml = null;

                try {
                    configXml = new SAXReader().read(mainForm.configPath + "/config.xml");
                    configXml.getRootElement().element("port").addAttribute("value", portField.getText());
                    configXml.getRootElement().element("filepath").addAttribute("value", filePathField.getText());

                    XMLWriter writer = new XMLWriter(new FileOutputStream(mainForm.configPath + "/config.xml"));
                    writer.write(configXml);
                    writer.close();
                    jFrame.dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (DocumentException ex) {
                    ex.printStackTrace();
                }
            }
        });

        NakButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
            }
        });

        pathChoose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = null;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(1); //读取到绝对路径

                if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    f = fileChooser.getSelectedFile();
                }
                filePathField.setText(f.getAbsolutePath());
            }
        });
    }

    /** 设置软件信息面板 */
    public void initInfoPanel(){
        InfoModelPanel.setBorder(BorderFactory.createEtchedBorder());
        InfoTitle.setFont(new Font("Dialog" , Font.BOLD, 25));
        ToolTitle.setFont(new Font("Dialog" , Font.BOLD, 13));
    }

}
