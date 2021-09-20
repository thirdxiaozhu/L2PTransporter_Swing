package zoujiaxv;

import com.google.gson.Gson;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author jiaxv
 */
public class MainForm {
    public JPanel mainPanel;
    public JTextArea messages;
    private JLabel QRCode;
    private JLabel Text1;
    private JTextArea PCInfomation;
    private JList<String> sendlist;
    private JList<String> receivelist;
    private JButton config;
    private JButton deleteBtn;
    public DefaultListModel<String> listModel;
    public Vector<DeviceInfo> infos;
    private String isSelected;
    private JList<String> devicelist;
    public String configPath;
    public String port;
    public String filePath;


    private void createUIComponents() {
        // TODO: place custom component creation code here
        //添加字符串到list
        listModel = new DefaultListModel<>();
        infos = new Vector<>();
        isSelected = null;
        //map = new HashMap<>();
        initDeviceList();
    }

    public MainForm() throws IOException, DocumentException {
        initConfigPath();
        initDom();
        initSRLists();
        initQRCode();
        initPCInfo();

        new Thread(){
            @Override
            public void run() {
                new ServerListener(MainForm.this).start(port);
            }
        }.start();
        //启动服务器并绑定窗口

        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isSelected != null) {
                    try {
                        ServerThread currentThread = ServerListener.onLineClient.get(isSelected);
                        currentThread.stop();
                        infos.remove(currentThread.deviceInfo);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    infos.removeElement(isSelected);
                    listModel.removeElementAt(devicelist.getSelectedIndex());
                    devicelist.setSelectedIndex(0);
                }else{
                    new MessageFrame("当前未选中任何设备");
                }
            }
        });

        config.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("设置");
                frame.setContentPane(new SettingForm(MainForm.this, frame).MyPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setSize(700,500);
                frame.setLocation(550 , 350); //在屏幕中间显示
                frame.setVisible(true);
                frame.setLayout(null);
                frame.setLocationRelativeTo(null);

                frame.setResizable(false);
            }
        });
    }

    private void initConfigPath(){
        //获取当前系统的“我的文档”文件夹，并生成/cryptogoose文件夹用以存储.keystore文件(Linux下为/home文件夹)
        JFileChooser tempfilechooser = new JFileChooser();
        FileSystemView fw = tempfilechooser.getFileSystemView();
        //拼接字符串，指向我的文档
        configPath = fw.getDefaultDirectory().toString() + "/l2preceived/";
    }

    private void initDeviceList() {
        devicelist = new JList<>(listModel);
        devicelist.setBorder(BorderFactory.createEtchedBorder(0));
        devicelist.setCellRenderer(new MyListCellRenderer(infos));
        devicelist.setPreferredSize(new Dimension(200, 350));

        devicelist.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                doListValueChange(e);
            }
        });
    }

    private void initQRCode(){
        Gson gson = new Gson();
        String text = gson.toJson(HostInfo.getHostInfo());
        //生成二维码
        try {
            QRCode.setSize(300,150);
            QRCode.setText("");
            QRCode.setIcon(QRCodeUtil.encode(HostInfo.getHostInfo().hostIP + ":" + port));
            System.out.println(HostInfo.getHostInfo().hostIP + ":" + port);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void initPCInfo(){
        PCInfomation.setBorder(BorderFactory.createTitledBorder("本机信息"));
        PCInfomation.append("主机名：  " + HostInfo.getHostInfo().hostName + "\n");
        PCInfomation.append("主机IP：  " + HostInfo.getHostInfo().hostIP + "\n");
        PCInfomation.append("OS：  " + HostInfo.getHostInfo().osName + "\n");
        PCInfomation.append("OS架构:  " + HostInfo.getHostInfo().osArch + "\n");
        PCInfomation.append("OS版本：  " + HostInfo.getHostInfo().osVersion);
    }

    private void initSRLists(){
        sendlist.setBorder(BorderFactory.createEtchedBorder(0));
        receivelist.setBorder(BorderFactory.createEtchedBorder(0));
        sendlist.setPreferredSize(new Dimension(400, 100));
        receivelist.setPreferredSize(new Dimension(400, 100));
    }

    private void initDom() throws IOException, DocumentException {

        File defaultDir = new File(configPath);
        //如果我的文档目录下没有设置文件夹，那么就创建一个
        if(!defaultDir.exists() && !defaultDir.isDirectory()) {
            defaultDir.mkdir();
        }

        File defaultXml = new File(configPath + "/config.xml");
        if(!defaultXml.exists()) {
            Document new_document = DocumentHelper.createDocument();
            Element root = new_document.addElement("root");

            root.addElement("port")
                    .addAttribute("value", "1234");
            root.addElement("filepath")
                    .addAttribute("value", configPath);

            FileWriter out = new FileWriter(configPath + "/config.xml");
            new_document.write(out);
            out.close();
        }

        Document configXml = new SAXReader().read(configPath + "/config.xml");
        Element root = configXml.getRootElement();
        port = configXml.getRootElement().element("port").attribute("value").getStringValue();
        filePath = configXml.getRootElement().element("filepath").attribute("value").getStringValue();

        //如果用户自定义的文件夹不存在，那么就创建一个
        File customDir = new File(filePath);
        if(!customDir.exists() && !customDir.isDirectory()) {
            customDir.mkdir();
        }

    }


    /**
     * 每当有设备接入，向列表添加设备
     * @return
     */
    public void deviceAccess(DeviceInfo device) throws AWTException {
        //在设备vector中加入连接进来的设备
        System.out.println(device.getDeviceMac());
        infos.add(device);
        listModel.addElement(device.getDeviceIP());

        Toolkit.getDefaultToolkit().beep();
        new MessageFrame("有新设备接入");
    }


    public void deviceDisconnect(String IP){
        try {
            ServerThread currentThread = ServerListener.onLineClient.get(IP);
            currentThread.stop();
            infos.remove(currentThread.deviceInfo);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        infos.removeElement(isSelected);
        listModel.removeElement(IP);
        devicelist.setSelectedIndex(0);
    }

    private void doListValueChange(ListSelectionEvent e){
        isSelected = devicelist.getSelectedValue();
        if(isSelected != null) {
            //当选择设备改变时，两个列表的绑定的Model也要更新成deviceinfo类里面的model
            ServerThread currentThread = ServerListener.onLineClient.get(isSelected);
            sendlist.setModel(currentThread.sendlistModel);
            receivelist.setModel(currentThread.receivelistModel);

            //每次改变选择，设置文件拖拽监听器
            DropTargetListener listener = new MyDropTargetListener(currentThread);
            DropTarget dropTarget = new DropTarget(sendlist, DnDConstants.ACTION_COPY_OR_MOVE, listener, true);

            new MessageFrame("当前设备：" + currentThread.deviceInfo.getDeviceName());
        }else {
            new MessageFrame("当前无选中设备");
        }
    }

    public void receiveFile(String filename){
        ServerThread currentThread = ServerListener.onLineClient.get(isSelected);
        currentThread.receivelistModel.addElement("文件: " + filename);
    }
}
