package zoujiaxv;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author jiaxv
 */
public class ManageFile {

    //在迭代的时候保证一致性（添加、移除），需要使用CopyOnWriteArrayList
    public CopyOnWriteArrayList<File> havesend;
    public CopyOnWriteArrayList<File> waitsend;

    public ManageFile(){
        havesend = new CopyOnWriteArrayList<>();
        waitsend = new CopyOnWriteArrayList<>();
        //updateFile();
    }

    public void addFile(File file){
        waitsend.add(file);
    }

    public void updateFile(Socket socket){
        new Thread(){
            @Override
            public void run() {
                try {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    while(true){
                        while(waitsend.size() != 0){
                            for(File f: waitsend){
                                FileInputStream fis = new FileInputStream(f);
                                String fileMessage = String.format("Start--%-1003s--%012d",ToolUtil.str2HexStr(f.getName()),f.length());
                                System.out.println(fileMessage);
                                dos.write(fileMessage.getBytes(), 0 , fileMessage.getBytes().length);
                                dos.flush();

                                byte[] bytes = new byte[1024];
                                int length = 0;
                                long progress = 0;

                                while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                                    if (length!=1024) System.out.println(length);
                                    dos.write(bytes, 0, length);
                                    dos.flush();
                                    progress += length;
                                }

                                System.out.println(progress);

                                waitsend.remove(f);
                                //try {
                                //    sleep(1000);
                                //} catch (InterruptedException e) {
                                //    e.printStackTrace();
                                //}
                                System.out.println(f.getAbsolutePath() + "结束");
                                System.out.println(waitsend.size());
                            }
                        }
                        //必需等待一下，否则占用就爆了
                        sleep(100);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
