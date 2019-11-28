package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ChatUser {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;

    public ChatUser(final Server server, final Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;

        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(new Runnable() {
                public void run(){
                    try {
                        while (true) {
                            String msg = null;
                            msg = in.readUTF();
                            System.out.println(msg);
                            String[] tokens = msg.split(" ");
                            switch (tokens[0]){
                                case ("/auth"):
                                        nick = DbConnection.getNickAuth(tokens[1], tokens[2]);
//                                        if(nick==null){
//                                            out.writeUTF("/wrong ");
//                                            out.flush();
//                                        }
//                                        else{
//                                            if(!server.getUsers().isEmpty() && server.getUsers().containsKey(getChatUser(nick))){
//                                               out.writeUTF("/inUse ");
//                                               out.flush();
//                                            }
//                                            else {
                                                server.addUser(ChatUser.this, nick);
                                                out.writeUTF("/authOk ");
                                                out.flush();
                                                updateBroadcast();
//                                            }
//                                        }

                                break;
                            case("/friends"):
                                out.writeUTF("/online "+ listToString(getOnline()));
                                out.flush();
                                break;
                            case("/open"):
                                ChatUser chatTo = getChatUser(tokens[1]);
                                String from = ChatUser.this.nick;
                                String invite = "/invitation "+from;
                                chatTo.sendMsg(invite);
                                break;
                            case("/ban"):
                                //add to ban
                                if(!getChatUser(tokens[1]).equals(ChatUser.this)){
                                DbConnection.addToBan(nick,tokens[1]);
                                }
                                out.writeUTF("/added ");
                                out.flush();
                                break;
                            case ("/back"):
                                //remove from ban
                                DbConnection.removeFromBan(nick,tokens[1]);
                                out.writeUTF("/added ");
                                out.flush();
                                break;
                            case("/banList"):
                                // send list of banned friends for this user
                                String bannedFriends = DbConnection.getBanedListByNick(nick);
                                if(bannedFriends!=null){
                                out.writeUTF("/yourBan "+ bannedFriends);
                                out.flush();
                                }
                                break;
                                case("/private"):
                                   // String msg1 = "/abc"+" "+msg;
//                                    StringBuilder sb = new StringBuilder();
//                                    sb.append("/abc").append(" ");
//                                    for(int i =2;i<tokens.length;i++){
//                                        sb.append(tokens[i]).append(" ");
//                                    }
                                    System.out.println("!"+msg);
                                   sendPrivateMsg(getChatUser(tokens[1]), msg, ChatUser.this);
                                break;
                            case("/exit"):
                                server.removeUser(ChatUser.this);
                                updateBroadcast();
                                break;
                            default:
                                safeBroadcast(msg, ChatUser.this);
                                break;
                            }
                        }
                    }
                    catch(IOException e){
                            e.printStackTrace();
                    }
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }

                }
            }).start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void sendMsg(String msg, String nick){
        try {
            out.writeUTF(nick +":" + "\n"+msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void sendMsg(String msg){
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void updateBroadcast() {
        Map<ChatUser, String> friends = new ConcurrentHashMap<ChatUser, String>();
        friends = server.getUsers();
        for (Map.Entry<ChatUser, String> entry : friends.entrySet()) {
            entry.getKey().sendMsg("/update ");
        }

    }

    protected void broadcast(Map<ChatUser, String> friends, String msg, ChatUser from) {
        for (Map.Entry<ChatUser, String> entry : friends.entrySet()) {
                entry.getKey().sendMsg(msg, friends.get(from));
        }
    }


    protected void safeBroadcast(String msg, ChatUser from){
        Map<ChatUser, String> friends = new ConcurrentHashMap<ChatUser, String>();
        friends = server.getUsers();
        String fromUser = friends.get(from);
        String deprecatedUsers = DbConnection.getWhoBannedMe(fromUser);
        if(deprecatedUsers == null) {
            broadcast(friends,msg,from);
        }
        else {
            String[] muteUserNick = deprecatedUsers.split(" ");
            for(int i = 0;i<muteUserNick.length;i++){
                for (Map.Entry<ChatUser, String> entry : friends.entrySet()) {
                    if(!entry.getValue().equals(muteUserNick[i])){
                        entry.getKey().sendMsg(msg, friends.get(from));
                    }
                }
            }
        }
    }

    protected ArrayList<String> getOnline(){
        Map<ChatUser,String> online = new ConcurrentHashMap<ChatUser,String>();
        online = server.getUsers();
        ArrayList<String> onlineNames = new ArrayList<>();
        for (Map.Entry<ChatUser,String> entry: online.entrySet()){
            onlineNames.add(entry.getKey().nick);
        }
        return onlineNames;
    }

    private String listToString(ArrayList<String> list){
        StringBuilder result = new StringBuilder();
        for(String s: list){
            result.append(s);
            result.append(" ");
        }
        return result.toString();
    }

    protected void sendPrivateMsg(ChatUser to, String msg, ChatUser from){
        to.sendMsg(msg);
    }

    protected ChatUser getChatUser(String nick){
        Map<ChatUser,String> users = new ConcurrentHashMap<ChatUser,String>();
        users = server.getUsers();
        for (Map.Entry<ChatUser,String> entry: users.entrySet()){
            if(entry.getValue().equals(nick)){
                return entry.getKey();
            }
        }
        return null;
    }

}
