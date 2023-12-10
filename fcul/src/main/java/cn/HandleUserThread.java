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
                System.out.println("message received:"+ read);
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
                    System.out.println(clientIp.concat(":"+splited[1]));
                    System.out.println("Secretkey:" + Server.secretKey);
                    System.out.println("Secretkey param:" +Server.secretKey.getParameters());
                    System.out.println("Secretkey serializes:" +serializeSecretKey(Server.secretKey));
                    Server.linesent=serializeSecretKey(Server.secretKey);
                    ob.writeObject(Server.secretKey);
                    ob.writeObject(Server.publicKey);
                    //writer.println(serializeSecretKey(Server.secretKey));


                }

                if(splited[0].equals("request")){
                    if(splited[1].equals("attribute")) {
                        if(Server.hasAttribute(username,splited[2]))
                            writer.println(Server.getIpsFromAttribute(splited[2]));
                        System.out.println("request attribute has attribute:"+Server.hasAttribute(username,splited[2]));
                        System.out.println("ips"+ Server.getIpsFromAttribute(splited[2]));
                    }
                    else if(splited[1].equals("username")){
                        writer.println(Server.getIpFromUsername(splited[2]));

                    }
                    else if (splited[1].equals("policy")) {
                        if(Server.hasAttribute(username,splited[2])){
                            /*int [][] aux= Server.policyForAttribute(splited[2]);
                            String[] aux2 = Server.rhosForAttribute(splited[2]);
                            String policy="";
                            String rhos="";
                            if(aux!=null)
                                policy = matrixToString(aux);
                            if(aux2!=null)
                                rhos=arrayToString(aux2);

                            System.out.println("policy sent"+ policy);
                            writer.println(policy);
                            writer.println(rhos);*/
                            System.out.println("policystring sent"+ Server.accessStringForAttribute(splited[2]));
                            writer.println(Server.accessStringForAttribute(splited[2]));
                        }
                    }

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String matrixToString(int[][] matrix){
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < matrix.length; i++) {
            System.out.println("lines");
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(matrix[i][j]);
                if (j < matrix[i].length - 1) {
                    sb.append(",");
                }
            }
            if (i < matrix.length - 1) {
                sb.append(";");
            }
        }
        System.out.println(sb.toString());
       return sb.toString();
    }

    private String arrayToString(String[] array){
        String str1 = String.join(",", array);
        return str1;

    }


    public static String serializeSecretKey(PairingKeySerParameter secretKey) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(secretKey);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return Base64.getEncoder().encodeToString(byteArray);
    }
    public static PairingKeySerParameter deserializeSecretKey(String serializedSecretKey) throws IOException, ClassNotFoundException {
        byte[] byteArray = Base64.getDecoder().decode(serializedSecretKey);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        PairingKeySerParameter secretKey = (PairingKeySerParameter) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return secretKey;
    }

}
