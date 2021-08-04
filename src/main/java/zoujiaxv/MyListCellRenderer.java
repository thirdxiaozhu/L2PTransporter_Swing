package zoujiaxv;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyListCellRenderer extends DefaultListCellRenderer {

    private final ArrayList<DeviceInfo> devicecell;
    ImageIcon icon;

    public MyListCellRenderer(ArrayList<DeviceInfo> devicecell){
        this.devicecell = devicecell;
        icon = new ImageIcon("src/image/—Pngtree—mobile phone png smartphone camera_6067590.png");
        icon.setImage(icon.getImage().getScaledInstance(50, 47, Image.SCALE_DEFAULT));
    }
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        Font font = new Font("ubuntu", Font.BOLD, 15);

        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        label.setText(((DeviceInfo) value).getDeviceName());

        if(((DeviceInfo) value).deviceType == "Phone"){
            label.setIcon(icon);
        }
        label.setHorizontalTextPosition(JLabel.RIGHT);
        label.setFont(font);
        return label;
    }
}
