package zoujiaxv;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jiaxv
 */
public class ServerListener{
    private static boolean isStrat;
    private static ServerThread serverThread;
    public static ConcurrentHashMap<String, ServerThread> onLineClient = new ConcurrentHashMap<>();
    MainForm mainForm;
    ExecutorService executorService = Executors.newCachedThreadPool();
    ServerSocket serverSocket = null;
    public  ServerListener(MainForm mainForm){
        this.mainForm = mainForm;
    }

    public void start(String port){
        try {
            serverSocket = new ServerSocket(Integer.parseInt(port));
            isStrat = true;
            System.out.println("服务端启动");

            while(isStrat){
                Socket socket = serverSocket.accept();
                serverThread = new ServerThread(socket, mainForm);
                System.out.println("有设备接入");
                if(socket.isConnected()){
                    executorService.execute(serverThread);
                }
            }
            serverSocket.close();
        }catch (BindException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "端口被占用，请重试");
        }catch (SocketException e){
            JOptionPane.showMessageDialog(null, "连接终止");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(serverSocket == null){
                try{
                    isStrat = false;
                    serverSocket.close();
                    if(serverSocket != null){
                        serverThread.stop();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
