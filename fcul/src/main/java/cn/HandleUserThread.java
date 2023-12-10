package cn;

import cn.edu.buaa.crypto.algebra.serparams.PairingKeySerParameter;

import javax.crypto.SecretKey;
import java.io.*;
import java.util.Base64;

public class HandleUserThread extends Thread {

    String username;
    String clientIp;
    BufferedReader reader;
    PrintWriter writer;
    ObjectOutputStream ob;

    public HandleUserThread(String username, String clientIp, BufferedReader reader, PrintWriter writer, ObjectOutputStream obj){
        this.username = username;
        this.reader=reader;
        this.writer=writer;
        this.clientIp = clientIp;
        ob=obj;
    }

    public void run() {
        while(true){
            try {
                String read = reader.readLine();
                System.out.println("Message received:"+ read);
                if(read.equals("close")) break;
                String[] splited= read.replace(" ","").split(":");
                if(splited[0].equals("register")){
                    if(splited[1].equals("attribute")) {
                        Server.registerAttribute(username, splited[2]);
                        writer.println("ok");
                    }

                }
                if(splited[0].equals("port")){
                    Server.username_ip.put(username, clientIp.concat(":"+splited[1]));
                    PairingKeySerParameter secretKey = Server.generateSecretKey(username);
                    ob.writeObject(secretKey);
                    ob.writeObject(Server.attribute_publickey);
                }

                if(splited[0].equals("request")){
                    if(splited[1].equals("attribute")) {
                        if(Server.hasAttribute(username,splited[2]))
                            writer.println(Server.getIpsFromAttribute(splited[2]));
                    }
                    else if(splited[1].equals("username")){
                        writer.println(Server.getIpFromUsername(splited[2]));

                    }
                    else if (splited[1].equals("policy")) {
                        if(Server.hasAttribute(username,splited[2])){
                            writer.println(Server.accessStringForAttribute(splited[2]));
                        }
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
