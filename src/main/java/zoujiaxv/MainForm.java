package zoujiaxv;

import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author jiaxv
 */
public class MainForm {
    public JPanel mainPanel;
    public JTextArea messages;
    private JList<DeviceInfo> devicelist;
    private JLabel QRCode;
    private JLabel Text1;
    private JTextArea PCInfomation;
    private JList<String> sendlist;
    private JList<String> receivelist;
    private JButton config;
    private JButton deleteBtn;
    public DefaultListModel<DeviceInfo> listModel;
    public Vector<DeviceInfo> infos;
    private DeviceInfo isSelected;
    public ArrayList<DeviceInfo> devicecell;
    public DefaultListModel<String> defaultModel = new DefaultListModel<>();
    public Map<DeviceInfo,ServerThread> map;

    private void createUIComponents() {
        // TODO: place custom component creation code here
        //添加字符串到list
        listModel = new DefaultListModel<>();
        infos = new Vector<>();
        devicecell = new ArrayList<>();
        isSelected = null;
        map = new HashMap<>();
        initDeviceList();
    }

    public MainForm() throws UnknownHostException {
        initSRLists();
        initQRCode();
        initPCInfo();
        //启动服务器并绑定窗口
        new ServerListener(this).start();

        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isSelected != null) {
                    try {
                        map.get(isSelected).socket.close();
                        map.remove(isSelected);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    infos.removeElement(isSelected);
                    devicecell.remove(isSelected);
                    listModel.removeElementAt(devicelist.getSelectedIndex());
                    devicelist.setSelectedIndex(0);
                }else{
                    new MessageFrame("当前未选中任何设备");
                }
            }
        });
    }

    private void initDeviceList() {
        devicelist = new JList<>(listModel);
        devicelist.setBorder(BorderFactory.createEtchedBorder(0));
        devicelist.setCellRenderer(new MyListCellRenderer(devicecell));
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
            QRCode.setIcon(QRCodeUtil.encode(HostInfo.getHostInfo().hostIP));
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


    /**
     * 每当有设备接入，向列表添加设备
     * @return
     */
    public void deviceAccess(DeviceInfo device) throws AWTException {
        //在设备vector中加入连接进来的设备
        infos.add(device);
        devicecell.add(device);
        listModel.addElement(device);

        Toolkit.getDefaultToolkit().beep();
        new MessageFrame("有新设备接入");
    }

    private void doListValueChange(ListSelectionEvent e){
        isSelected = (DeviceInfo) devicelist.getSelectedValue();
        if(isSelected != null) {
            //当选择设备改变时，两个列表的绑定的Model也要更新成deviceinfo类里面的model
            sendlist.setModel(isSelected.sendlistModel);
            receivelist.setModel(isSelected.receivelistModel);

            //每次改变选择，设置文件拖拽监听器
            DropTargetListener listener = new MyDropTargetListener(isSelected);
            DropTarget dropTarget = new DropTarget(sendlist, DnDConstants.ACTION_COPY_OR_MOVE, listener, true);

            new MessageFrame("当前设备：" +isSelected.getDeviceName());
        }else {
            new MessageFrame("当前无选中设备");
        }
    }
}
