package zoujiaxv;

import com.google.gson.Gson;
import zoujiaxv.ManageClient;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class ChatSocket extends Thread{
    Socket socket;
    MainForm mainForm;

    public ChatSocket(Socket s, MainForm mainForm) throws IOException, AWTException {
        this.socket = s;
        this.mainForm = mainForm;
        //每当接入一个设备就读取传来的设备信息
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = reader.readLine();
        Gson gson = new Gson();
        //反序列化
        DeviceInfo info = gson.fromJson(line, DeviceInfo.class);
        //遍历所有已连接的info， 如果mac地址相等，那么就弹出错误
        for (DeviceInfo currentdevice : mainForm.infos) {
            if(currentdevice.getDeviceMac().equals(info.getDeviceMac())){
                new MessageFrame("该设备已经连接");
                return;
            }
        }
        mainForm.infos.add(info);
        //每当接入一个设备就返回一次本机信息
        //PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        //writer.write(gson.toJson(HostInfo.getHostInfo()) + "\n");
        //writer.flush();
        String temp = gson.toJson(HostInfo.getHostInfo()) + "\n";
        socket.getOutputStream().write(temp.getBytes(StandardCharsets.UTF_8));
        mainForm.deviceAccess(info);

    }

    public void out(String out) throws IOException {
        socket.getOutputStream().write(out.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String line = null;
            while((line = bufferedReader.readLine()) != null){
                //ManageClient.get().publish(this, line+="\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
