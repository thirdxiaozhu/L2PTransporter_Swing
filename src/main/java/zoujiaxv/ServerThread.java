package zoujiaxv;

import Protocal.*;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author jiaxv
 */
public class ServerThread implements Runnable{

    private ReciveTask receiveTask;
    private SendTask sendTask;
    public Socket socket;
    private MainForm mainForm;
    private ResponseCallBack tBack;
    public DeviceInfo deviceInfo;

    private volatile ConcurrentLinkedQueue<BasicProtocol> dataQueue = new ConcurrentLinkedQueue<>();
    public DefaultListModel<String> sendlistModel;
    public DefaultListModel<String> receivelistModel;

    private InetAddress inetAddress;
    private String userIP;

    public  ServerThread(Socket socket, MainForm mainForm){
        this.socket = socket;
        this.inetAddress = socket.getInetAddress();
        this.mainForm = mainForm;
        this.userIP = this.inetAddress.getHostAddress();
        this.sendlistModel = new DefaultListModel<>();
        this.receivelistModel = new DefaultListModel<>();
        ServerListener.onLineClient.put(userIP, this);
    }

    @Override
    public void run() {
        BufferedReader reader;
        try{
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            Gson gson = new Gson();
            //反序列化
            deviceInfo = new Gson().fromJson(line, DeviceInfo.class);
            //遍历所有已连接的info， 如果mac地址相等，那么就弹出错误
            for (DeviceInfo currentdevice : mainForm.infos) {
                if(currentdevice.getDeviceMac().equals(deviceInfo.getDeviceMac())){
                    new MessageFrame("该设备已连接");
                    return;
                }
            }
            //每当接入一个设备就返回一次本机信息
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(gson.toJson(HostInfo.getHostInfo()) + "\n");
            writer.flush();

            mainForm.deviceAccess(deviceInfo);

            //开启接收线程
            receiveTask = new ReciveTask();
            receiveTask.inputStream = new DataInputStream(socket.getInputStream());
            receiveTask.start();

            //开启发送线程
            sendTask = new SendTask();
            sendTask.outputStream = new DataOutputStream(socket.getOutputStream());
            sendTask.start();

            printAllClient();

        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        if (receiveTask != null) {
            receiveTask.isCancle = true;
            receiveTask.interrupt();
            if (receiveTask.inputStream != null) {
                SocketUtil.closeInputStream(receiveTask.inputStream);
                receiveTask.inputStream = null;
            }
            receiveTask = null;
        }

        if(sendTask != null){
            sendTask.isCancled = true;
            sendTask.interrupt();
            if(sendTask.outputStream != null){
                synchronized (sendTask.outputStream){
                    //防止写数据是停止，写完在停止
                    sendTask.outputStream = null;
                }
            }

            sendTask = null;
        }

        if(socket.isConnected()){
            socket.close();
        }
    }
    public void addMessage(BasicProtocol data) throws IOException {
        if (!isConnected()) {
            return;
        }

        dataQueue.offer(data);
        toNotifyAll(dataQueue);//有新增待发送数据，则唤醒发送线程
    }

    public Socket getConnectdClient(String clientID) {
        return ServerListener.onLineClient.get(clientID).socket;
    }

    /**
     * 打印已经链接的客户端
     */
    public static void printAllClient() {
        if (ServerListener.onLineClient == null) {
            return;
        }
        Iterator<String> inter = ServerListener.onLineClient.keySet().iterator();
        while (inter.hasNext()) {
            System.out.println("client:" + inter.next());
        }
    }

    public void toWaitAll(Object o) {
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void toNotifyAll(Object obj) {
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    private boolean isConnected() throws IOException {
        if (socket.isClosed() || !socket.isConnected()) {
            ServerListener.onLineClient.remove(userIP);
            ServerThread.this.stop();
            System.out.println("socket closed...");
            return false;
        }
        return true;
    }

    public class ReciveTask extends Thread {

        private DataInputStream inputStream;
        private boolean isCancle;

        @Override
        public void run() {
            while (!isCancle) {
                try {
                    if (!isConnected()) {
                        isCancle = true;
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BasicProtocol clientData = SocketUtil.readFromStream(inputStream);

                if (clientData != null) {
                    if (clientData.getProtocolType() == 0) {
                        System.out.println("dtype: " + ((DataProtocol) clientData).getDtype() + ", pattion: " + ((DataProtocol) clientData).getPattion() + ", msgId: " + ((DataProtocol) clientData).getMsgId() + ", data: " + ((DataProtocol) clientData).getData());

                        DataAckProtocol dataAck = new DataAckProtocol();
                        dataAck.setUnused("收到消息：" + ((DataProtocol) clientData).getData());
                        dataQueue.offer(dataAck);
                        toNotifyAll(dataQueue); //唤醒发送线程

                        tBack.targetIsOnline(userIP);
                    } else if (clientData.getProtocolType() == 2) {
                        System.out.println("pingId: " + ((PingProtocol) clientData).getPingId());

                        PingAckProtocol pingAck = new PingAckProtocol();
                        pingAck.setUnused("收到心跳");
                        dataQueue.offer(pingAck);
                        toNotifyAll(dataQueue); //唤醒发送线程

                        tBack.targetIsOnline(userIP);
                    }
                } else {
                    System.out.println("client is offline...");
                    break;
                }
            }

            SocketUtil.closeInputStream(inputStream);
        }
    }

    public class SendTask extends Thread {

        private DataOutputStream outputStream;
        private boolean isCancled;

        @Override
        public void run() {
            while (!isCancled) {
                try {
                    if (!isConnected()) {
                        isCancled = true;
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BasicProtocol procotol = dataQueue.poll();
                if (procotol == null) {
                    toWaitAll(dataQueue);
                } else if (outputStream != null) {
                    synchronized (outputStream) {
                        SocketUtil.write2Stream(procotol, outputStream);
                    }
                }
            }

            SocketUtil.closeOutputStream(outputStream);
        }
    }
}
