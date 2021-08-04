package zoujiaxv;

import java.io.IOException;
import java.util.Vector;

public class ManageClient {
    //因为只有一个聊天服务器，故单例化
    private ManageClient(){}
    private static final ManageClient cm = new ManageClient();

    public static ManageClient getManageClient(){
        return cm;
    }

    Vector<ServerThread> vector = new Vector<ServerThread>();

    public void add(ServerThread st){
        vector.add(st);
    }

    //public void publish(ChatSocket cs, String out) throws IOException {
    //    for( int i = 0; i < vector.size(); i++){
    //        ChatSocket csChatSocket = vector.get(i);
    //        if(!cs.equals(csChatSocket)){
    //            csChatSocket.out(out);
    //        }
    //    }
    //}
}
