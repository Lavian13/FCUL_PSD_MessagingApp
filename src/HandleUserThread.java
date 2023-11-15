import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class HandleUserThread extends Thread {

    String username;
    BufferedReader reader;
    PrintWriter writer;

    public HandleUserThread(String username, BufferedReader reader, PrintWriter writer){
        this.username = username;
        this.reader=reader;
        this.writer=writer;
    }

    public void run() {
        while(true){
            try {
                String read = reader.readLine();
                if(read.equals("close")) break;
                String[] splited= read.replace(" ","").split(":");
                if(splited[0].equals("register")){
                    if(splited[1].equals("attribute")) Server.registerAttribute(username, splited[2]);

                }

                if(splited[0].equals("request")){
                    if(splited[1].equals("attribute")) {
                        writer.println(Server.getIpsFromAttribute(splited[2]));
                    }
                    else if(splited[1].equals("username")){
                        writer.println(Server.getIpFromUsername(splited[2]));

                    }

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
