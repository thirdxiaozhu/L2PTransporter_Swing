package zoujiaxv;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ChatManager {

    //单例化
    private ChatManager(){}
    private static  final ChatManager instance = new ChatManager();
    public static ChatManager getCM(){
        return instance;
    }

    //绑定窗口
    MainForm mainForm;
    Socket socket;
    String IP;
    BufferedReader reader;
    PrintWriter writer;

    public void setMainForm(MainForm mainForm){
        this.mainForm = mainForm;
    }

    public void connect(){
        this.IP = "127.0.0.1";
        new Thread(){
            @Override
            public void run() {
                try {
                    socket = new Socket(IP, 12345);
                    writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String line;
                    while((line = reader.readLine()) != null){
                        mainForm.messages.append("收到: " + line + "\n");
                    }
                    writer.close();
                    reader.close();
                    writer = null;
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void send(String out){
        if(writer != null){
            writer.write(out + "\n");
            //强制刷新
            writer.flush();
        }else{
            mainForm.messages.append("当前连接已经中断\n");
        }
    }


}
