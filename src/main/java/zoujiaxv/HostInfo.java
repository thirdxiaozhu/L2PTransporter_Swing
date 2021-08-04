package zoujiaxv;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;

public class HostInfo {
    public String hostIP;
    public String hostName;
    public String osName;
    public String osArch;
    public String osVersion;

    //单例化
    private HostInfo() throws UnknownHostException {
        getHostIP();
        getHostName();
        Properties props=System.getProperties();
        osName = props.getProperty("os.name");
        osArch = props.getProperty("os.arch");
        osVersion = props.getProperty("os.version");
    }
    private static HostInfo hi = null;

    static {
        try {
            hi = new HostInfo();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static HostInfo getHostInfo(){
        return hi;
    }

    private void getHostIP(){
        String tempIP = "127.0.0.1";
        try {
            tempIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try{
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            Enumeration<InetAddress> addrs;
            while (networks.hasMoreElements())
            {
                addrs = networks.nextElement().getInetAddresses();
                while (addrs.hasMoreElements())
                {
                    ip = addrs.nextElement();
                    if (ip != null
                            && ip instanceof Inet4Address
                            && ip.isSiteLocalAddress()
                            && !ip.getHostAddress().equals(tempIP))
                    {
                        this.hostIP = ip.getHostAddress();
                    }
                }
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void getHostName() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        //获取本机计算机名称
        this.hostName = addr.getHostName();
    }
}
