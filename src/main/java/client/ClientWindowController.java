package client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;


public class ClientWindowController implements Initializable {
    @FXML
    TextArea textArea;
    @FXML
    TextField textField;
    @FXML
    TableView<Friend> users;
    @FXML
    TableView<Friend> ban;
    @FXML
    TableColumn<Friend, String> activeUsers;
    @FXML
    TableColumn<Friend, String> bannedUsers;

    @FXML
    private ObservableList<Friend> active = FXCollections.observableArrayList();
    @FXML
    private ObservableList<Friend> banned = FXCollections.observableArrayList();

    @FXML
    Button send;
    @FXML
    Button openPrivate;
    @FXML
    Button toBan;
    @FXML
    Button exit;
    private int Port = 8089;
    private String address = "localhost";
    private DataInputStream in;
    private DataOutputStream out;
    private static Socket socket;


    public void initialize(URL location, ResourceBundle resources) {
        activeUsers.setCellValueFactory(new PropertyValueFactory<Friend,String>("name"));
        users.setItems(active);
        bannedUsers.setCellValueFactory(new PropertyValueFactory<Friend,String>("name"));
        ban.setItems(banned);
        OutStaticClass outStaticClass = new OutStaticClass();
        try {
            socket = StartWindowController.getSocket();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
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
                            System.out.println(1 + " " + tokens[0].length() + " " + tokens[0]);
                            tokens[0].replaceAll("\\P{Print}","");
                            System.out.println(2 + " " + tokens[0].length() + " " + tokens[0] + " " + tokens[0].replaceAll("\\P{Print}",""));
                            switch (tokens[0]) {
                                case ("/online"):
                                    users.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        active.add(new Friend(tokens[i]));
                                    }
                                    break;
                                case ("/update"):
                                    refresh();
                                    break;
                                case ("/added"):
                                    refreshBan();
                                    break;
                                case ("/yourBan"):
                                    ban.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        banned.add(new Friend(tokens[i]));
                                    }
                                    break;
                                case ("/invitation"):
                                    createPrivateWindow();
                                    PrivateWindowController.setNickTo(tokens[1]);
                                    break;
                                case ("/private"):
                                    System.out.println(1);
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(tokens[1]+": \n");
                                    for(int i = 2;i<tokens.length;i++){
                                        sb.append(tokens[i]).append(" ");
                                    }
                                    outStaticClass.textArea.appendText(sb.toString() + "\n");
                                    break;
                                default:
                                    System.out.println(2);
                                    textArea.appendText(msg + "\n");
                                    break;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                 }

        }).start();
    }

    //get name from table
    private String getData(TableView<Friend> table){
        Friend myFriend= table.getSelectionModel().getSelectedItem();
        String data = myFriend.getName();
        return data;
    }

    protected void initData(){
        try{
            out.writeUTF("/friends ");
            out.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initBanData() {
        try{
            out.writeUTF("/banList ");
            out.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void refresh(){
        users.getItems().clear();
        initData();
    }
    private void refreshBan(){
        ban.getItems().clear();
        initBanData();
    }

    public void addToBan(ActionEvent event) {
        String name = getData(users);
        try {
            out.writeUTF("/ban "+name);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //method allows to get off the ban from user,
    // also exlude them from the banned list
    public void backToActive(ActionEvent event) {
        String name  = getData(ban);
        try{
            out.writeUTF("/back "+ name);
            out.flush();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void openPrivateChat(ActionEvent event) throws IOException {
        String to = getData(users);
        out.writeUTF("/open " +  getData(users));
        out.flush();
        createPrivateWindow();
        PrivateWindowController.setNickTo(to);
    }

    protected void createPrivateWindow(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    privateWindow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void privateWindow() throws IOException, SQLException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/PrivateWindow.fxml"));
        stage.setTitle("Private Chat");
        stage.setScene(new Scene(root, 300, 300));
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

    protected static Socket getSocket(){
        return socket;
    }

    public void sendMsg(ActionEvent actionEvent) throws IOException {
        String msg = textField.getText();
        out.writeUTF(msg);
        out.flush();
        textField.clear();
        textField.requestFocus();
    }

    public void exit(ActionEvent event) {
        try {
            out.writeUTF("/exit ");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage st = (Stage) (Stage) exit.getScene().getWindow();
        st.close();
        st.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                st.close();
                Platform.exit();
                System.exit(0);
            }
        });
    }
}
