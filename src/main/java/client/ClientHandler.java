package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private int Port = 8189;
    private String address = "localhost";
    private DataInputStream in;
    private DataOutputStream out;
    private static Socket socket;
    private String[] nicks;

    public ClientHandler(){
//        try {
//            socket = StartWindowController.getSocket();
//            in = new DataInputStream(socket.getInputStream());
//            out = new DataOutputStream(socket.getOutputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        new Thread(new Runnable() {
//            public void run() {
//                while (true) {
//                    String msg = null;
//                    try {
//                        msg = in.readUTF();
//                        System.out.println(msg);
//                        String[] tokens = msg.split(" ");
//                        switch (tokens[0]){
//                            case("/online"):
//                                nicks = new String[tokens.length];
//                                for (int i = 0;i<nicks.length;i++){
//                                    active.add(new Friend(nicks[i]));
//                                }
//                                //tablesInit();
//                                break;
//                            default:
//                                System.out.println(msg);
//                                textArea.appendText(msg + "\n");
//                                break;
//                        }
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//    }

    }
}
