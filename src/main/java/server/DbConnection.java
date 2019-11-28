package server;

import java.sql.*;
import java.util.ArrayList;

public class DbConnection {
    private static Connection connection;// соединение
    private static Statement stmt;// запрос в базу данных

    protected static String url = "jdbc:mysql://localhost:8889/users?serverTimezone=Europe/Moscow&useSSL=false";
    protected static String username = "root";
    protected static String password = "root";

    protected static boolean connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            if (connection != null) {
                stmt = connection.createStatement();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected static void disconnect() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected  static boolean checkUser (String login, String password) throws SQLException {
        String sqlPass = String.format("SELECT password FROM users WHERE login='%s'", login);
        try {
            ResultSet rs = stmt.executeQuery(sqlPass);
            if ((rs.next() && rs.getString(1).equals(password))) {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    protected static String getNickAuth(String login, String password){
        String nick = new String();
        String sql = String.format("SELECT nick FROM users WHERE login='%s' AND password ='%s'", login, password);
        try{
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()){
                nick = rs.getString(1);
                return nick;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } return null;
    }

    protected static void addToBan(String user, String nick){
      //  int id = getUserId(user);
        String sql = String.format("INSERT INTO ban(user, nick) VALUES (%d,'%s')", getUserIdFromUsers(user) ,nick);
        try{
            System.out.println(sql);
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void removeFromBan(String user, String nick){
        String sql = String.format("DELETE FROM ban WHERE user = %d AND nick = '%s'", getUserIdFromUsers(user), nick);
        try{
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static String getBanedListByNick(String user){
        StringBuilder sb = new StringBuilder();
        int id =  getUserIdFromUsers(user);
        String sql = String.format("SELECT nick FROM ban WHERE user = %d", id);
        try{
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                sb.append(rs.getString("nick"));
                sb.append(" ");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    protected static String getWhoBannedMe(String user){
        ArrayList<Integer> ids = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String sql = String.format("SELECT DISTINCT user FROM ban WHERE nick = '%s'", user);
        try{
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                ids.add(rs.getInt("user"));
                while (rs.next()) {
                    ids.add(rs.getInt("user"));
                }
                if (!ids.isEmpty()) {
                    for (Integer i : ids) {
                        String nick = getUserNickFromUsers(i);
                        sb.append(nick);
                        sb.append(" ");
                    }
                }
                System.out.println(sb.toString() + "getWhoBannedMe");
                return sb.toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static Integer getUserIdFromUsers(String user){
         int id=0;
         String sql = String.format("SELECT id FROM users WHERE nick = '%s'", user);
         try{
             ResultSet rs = stmt.executeQuery(sql);
             if(rs.next()){
                 id= rs.getInt(1);
                 return id;
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
    }

    protected static String getUserNickFromUsers(int id){
        String result = new String();
        String sql = String.format("SELECT nick FROM users WHERE id= %d",id);
        try{
            ResultSet rs = stmt.executeQuery(sql);
               if (rs.next()){
                    result = rs.getString("nick");
                }
        return result;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
