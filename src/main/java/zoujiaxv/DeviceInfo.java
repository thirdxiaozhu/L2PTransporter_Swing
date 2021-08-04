package zoujiaxv;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.UUID;

/**
 * @author jiaxv
 */
public class DeviceInfo {
    private String deviceName;
    private String deviceIP;
    private String deviceMac;
    public String deviceUUID;
    public String deviceType;
    public DefaultListModel<String> sendlistModel;
    public DefaultListModel<String> receivelistModel;
    public ManageFile manageFile;
    //public ServerThread st;


    public DeviceInfo(){
        deviceUUID = getUUID32();
        deviceType = "Phone";
        sendlistModel = new DefaultListModel<>();
        receivelistModel = new DefaultListModel<>();
        manageFile = new ManageFile();
    }

    public String getDeviceName(){
        System.out.println(deviceName);
        return deviceName;
    }

    public String getDeviceIP(){
        return deviceIP;
    }

    public String getDeviceMac(){
        return deviceMac;
    }

    public String getDeviceType(){
        return deviceType;
    }

    private static String getUUID32(){
        return UUID.randomUUID().toString().replaceAll("-","").toLowerCase();
    }

    private void addModel(String path, int type){
        if(type == 0){
            sendlistModel.addElement(path);
        }
        else if(type == 1){
            receivelistModel.addElement(path);
        }
    }
}
