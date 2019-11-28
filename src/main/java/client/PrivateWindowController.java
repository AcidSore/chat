package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class PrivateWindowController implements Initializable {
    @FXML
    TextArea textArea;
    @FXML
    TextField textField;
    @FXML
    Button send;

    private DataInputStream in;
    private DataOutputStream out;
    private static Socket socket;
    private static  String nickTo;

    protected static void setNickTo(String nickTo) {
        PrivateWindowController.nickTo = nickTo;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket =ClientWindowController.getSocket();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            OutStaticClass outStaticClass = new OutStaticClass();
            outStaticClass.textArea = textArea;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String msg = null;
                        try {
                            msg = in.readUTF();
                            System.out.println(msg);
                            String[] tokens = msg.split(" ");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        new Thread(new Runnable() {
                            public void run() {
                                while (true) {
                                    String msg = null;
                                    try {
                                        msg = in.readUTF();
                                        System.out.println(msg);
                                        String[] tokens = msg.split(" ");
                                        switch (tokens[0]) {
                                            case("/secret"):
//                                                System.out.println(msg+"secret");
//                                                textArea.appendText(msg + "\n");
                                                break;
                                            default:
                                                break;
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        }).start();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     public  void PrintSecretMsg(String msg){
        System.out.println(msg+"secret");
        textArea.appendText(msg + "\n");
    }

    public void sendMsg(ActionEvent event) throws IOException {
        String msg = "/private "+ nickTo + " " + textField.getText();
        System.out.println(nickTo);
        out.writeUTF(msg);
        out.flush();
        textField.clear();
        textField.requestFocus();
    }

}
