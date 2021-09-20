package zoujiaxv;

import Protocal.BasicProtocol;
import Protocal.DataProtocol;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author jiaxv
 */
public class ManageFile {
    private static final int HEAD=0;
    private static final int BODY=1;
    private static final int BUFFERLENGTH=8192;

    //在迭代的时候保证一致性（添加、移除），需要使用CopyOnWriteArrayList
    //private CopyOnWriteArrayList<File> waitsend;
    protected volatile ConcurrentLinkedQueue<DataProtocol> dataQueue;
    protected volatile ConcurrentLinkedQueue<File> files;
    private ServerThread serverThread;
    private GenerateMessage generateMessage;
    private GenerateFile generateFile;
    private DeviceInfo deviceInfo;

    public ManageFile(ServerThread serverThread, DeviceInfo deviceInfo){
        files = new ConcurrentLinkedQueue<>();
        dataQueue = new ConcurrentLinkedQueue<>();
        generateMessage = new GenerateMessage();
        generateMessage.start();
        generateFile = new GenerateFile();
        generateFile.start();
        this.serverThread = serverThread;
        this.deviceInfo = deviceInfo;
    }

    public void addMessage(DataProtocol dataProtocol) {
        dataQueue.offer(dataProtocol);
        toNotifyAll(dataQueue);
    }

    public void addFile(File f){
        files.offer(f);
        toNotifyAll(files);
    }

    private void toWaitAll(Object o){
        synchronized (o){
            try{
                o.wait();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void toNotifyAll(Object o){
        synchronized (o){
            o.notifyAll();
        }
    }

    public class GenerateMessage extends Thread{
        @Override
        public void run() {
            try{
                while(true){
                    File f = files.poll();
                    if(f == null){
                        toWaitAll(files);
                    }else {
                        FileInputStream fis = new FileInputStream(f);
                        String fileMessage = String.format("Start--%-1003s--%012d",ToolUtil.str2HexStr(f.getName()), f.length());
                        serverThread.addMessage(generateProtocol(0, HEAD, fileMessage.getBytes()));

                        byte[] data = new byte[BUFFERLENGTH];
                        int length = 0;
                        long progress = 0;

                        int msgId = 0x00;
                        while((length = fis.read(data, 0, data.length)) != -1) {
                            serverThread.addMessage(generateProtocol(msgId, BODY, data));
                            msgId++;
                            progress += length;
                            //TODO 为什么data需要重新new
                            data = null;
                            data = new byte[BUFFERLENGTH];
                        }
                        System.out.println(f.getAbsolutePath() + "结束");
                        System.out.println(files.size());
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public class GenerateFile extends Thread {
        FileOutputStream fos = null;
        String fileName = null;

        @Override
        public void run() {
            try {
                while (true) {
                    DataProtocol dataProtocol = dataQueue.poll();
                    if (dataProtocol == null) {
                        toWaitAll(dataQueue);
                    } else {
                        if (dataProtocol.getPattion() == HEAD) {

                            byte[] fileMessageByte = dataProtocol.getData();
                            String fileMessage = new String(fileMessageByte);

                            fileName = ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim());
                            serverThread.receivelistModel.addElement("文件: " + fileName);

                            String tempPath = serverThread.mainForm.filePath + deviceInfo.getDeviceName().replaceAll(" ", "_");
                            System.out.println("TempPath:" + tempPath);

                            File defaultPath = new File(tempPath);
                            //如果我的文档目录下没有cryptogoose文件夹，那么就创建一个cryptogoose文件夹
                            if(!defaultPath.exists() && !defaultPath.isDirectory()) {
                                defaultPath.mkdir();
                            }

                            fos = new FileOutputStream(defaultPath + "/" + fileName);
                        } else if (dataProtocol.getPattion() == BODY) {
                            int length = dataProtocol.getData().length;

                            fos.write(dataProtocol.getData(), 0, length);
                            fos.flush();

                            if(length < BUFFERLENGTH){
                                //TODO 取消进度条
                            }
                            //MainActivity.receiveListAdapter.finishReceive(mainActivity.updatebarHandler, fileName);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BasicProtocol generateProtocol(int msgId, int Pattern, byte[] data){
        DataProtocol dataProtocol = new DataProtocol();
        dataProtocol.setReserved(0);
        dataProtocol.setVersion(1);
        dataProtocol.setPattion(Pattern);
        dataProtocol.setDtype(0);
        dataProtocol.setMsgId(msgId);
        dataProtocol.setData(data);
        return dataProtocol;
    }
}
