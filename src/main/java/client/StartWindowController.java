package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class StartWindowController implements Initializable {
    @FXML
    TextField information;
    @FXML
    TextField login;
    @FXML
    PasswordField password;
    @FXML
    Button Auth;

    private int Port = 8089;
    private String adress = "localhost";
    private DataInputStream in;
    private DataOutputStream out;
    private static Socket socket;

    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket(adress,Port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void TryToAuth(ActionEvent event) {
        String logInfo ="/auth"+" "+ login.getText() +" "+ password.getText();
        try {
        out.writeUTF(logInfo);
        out.flush();
            System.out.println(logInfo);
        } catch (IOException e) {
        e.printStackTrace();
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    String serverAnswer = in.readUTF();
                    String[] tokens = serverAnswer.split(" ");
                    System.out.println(serverAnswer);
                    switch (tokens[0]){
//                        case("/wrong"):
//                            information.setVisible(true);
//                            information.appendText("wrong login or password");
//                            break;
//                        case("/inUse"):
//                            information.setVisible(true);
//                            information.appendText("you are already authorized");
//                            break;
                        case ("/authOk"):
                            out.writeUTF("/friends");
                            out.writeUTF("/banList");
                            out.flush();
                            try {
                                createClientWindow();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void clientWindow() throws IOException, SQLException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/Window.fxml"));
        stage.setTitle("Chat");
        stage.setScene(new Scene(root, 600, 300));
        Stage oldStage = (Stage) Auth.getScene().getWindow();
        oldStage.close();
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stage.close();
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void createClientWindow(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    clientWindow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static Socket getSocket(){
        return socket;
    }
}
