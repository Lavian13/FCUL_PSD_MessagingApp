package cn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class HandleUserThread extends Thread {

    String username;
    String clientIp;
    BufferedReader reader;
    PrintWriter writer;

    public HandleUserThread(String username, String clientIp, BufferedReader reader, PrintWriter writer){
        this.username = username;
        this.reader=reader;
        this.writer=writer;
        this.clientIp = clientIp;
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
                    writer.println("ok");

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


}
