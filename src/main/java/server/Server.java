package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    final String IP_ADRESS = "localhost";
    final int PORT = 8089;
    private static Socket clientSocket; //сокет для общения
    private static ServerSocket server; // серверсокет
    private static Map<ChatUser, String> users;

    public Server(){
        try{
            if (DbConnection.connect())
            System.out.println("server is listening");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            server = new ServerSocket(PORT);
            users = new ConcurrentHashMap<ChatUser,String>();
            while (true){
                clientSocket = server.accept();
                try{
               new ChatUser(this, clientSocket);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                DbConnection.disconnect();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static void addUser(ChatUser user, String nick){
         users.put(user,nick);
    }

    protected static void removeUser(ChatUser user){
        users.remove(user);
    }

    protected Map<ChatUser,String> getUsers(){
        return users;
    }

}
