package zoujiaxv;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author jiaxv
 */
public class ServerThread extends Thread{
    Socket socket;
    InetAddress inetAddress;
    MainForm mainForm ;
    String ipAddress;
    private Gson gson;

    public  ServerThread(Socket socket, InetAddress inetAddress, MainForm mainForm){
        this.socket = socket;
        this.inetAddress = inetAddress;
        this.mainForm = mainForm;
    }

    @Override
    public void run() {
        BufferedReader reader;
        try{
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            Gson gson = new Gson();
            //反序列化
            DeviceInfo info = new Gson().fromJson(line, DeviceInfo.class);
            //遍历所有已连接的info， 如果mac地址相等，那么就弹出错误
            for (DeviceInfo currentdevice : mainForm.infos) {
                if(currentdevice.getDeviceMac().equals(info.getDeviceMac())){
                    new MessageFrame("该设备已连接");
                    return;
                }
            }
            //每当接入一个设备就返回一次本机信息
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(gson.toJson(HostInfo.getHostInfo()) + "\n");
            writer.flush();

            //增加键值对
            mainForm.map.put(info, this);

            mainForm.deviceAccess(info);
            info.manageFile.updateFile(socket);

        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
    }
}
