package zoujiaxv;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MyListCellRenderer extends DefaultListCellRenderer {

    public Vector<DeviceInfo> infos;
    private ImageIcon icon;

    public MyListCellRenderer(Vector<DeviceInfo> infos){
        this.infos = infos;
        icon = new ImageIcon("src/image/—Pngtree—mobile phone png smartphone camera_6067590.png");
        icon.setImage(icon.getImage().getScaledInstance(50, 47, Image.SCALE_DEFAULT));
    }
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        Font font = new Font("ubuntu", Font.BOLD, 15);

        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        for (DeviceInfo currentDevice : infos) {
            if(currentDevice.getDeviceIP().equals((String)value)){
                label.setText(currentDevice.getDeviceName());
                if(currentDevice.deviceType == "Phone"){
                    label.setIcon(icon);
                }
                break;
            }
        }

        label.setHorizontalTextPosition(JLabel.RIGHT);
        label.setFont(font);
        return label;
    }
}
