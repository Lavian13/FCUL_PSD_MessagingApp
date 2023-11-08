public class RetrieveIPThread extends Thread {

    String ip;
    int port;

    public RetrieveIPThread(String ip, int port) {
        this.ip=ip;
        this.port=port;
    }

    //por enquanto realizo sequencialmente por isso n está a ser usada mas vai ser testada eventualmente
    public void run() {
        connectServer(ip,port);
    //VAI SER BASEADO NO CLIENTE DO CODIGO JA FEITO DE TLS
    }

    private static void connectServer(String ip, int port){

    }
}
