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
    public String ip;


    public DeviceInfo(){
        deviceUUID = getUUID32();
        deviceType = "Phone";
    }

    public String getDeviceName(){
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

}
