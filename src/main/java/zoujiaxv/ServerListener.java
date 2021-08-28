package zoujiaxv;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author jiaxv
 */
public class ServerListener extends Thread{
    MainForm mainForm;
    public  ServerListener(MainForm mainForm){
        this.mainForm = mainForm;
    }
    @Override
    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            Socket socket = null;
            System.out.println("服务端启动");

            while(true){
                socket = serverSocket.accept();
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println(socket);
                System.out.println(inetAddress);
                ServerThread thread = new ServerThread(socket, inetAddress,mainForm);
                thread.start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
