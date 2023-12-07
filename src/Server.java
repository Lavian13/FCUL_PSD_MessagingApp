import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


// Server class for tests
class Server {

    static HashMap<String, String> username_ip = new HashMap<>();
    //static HashMap<String, X509Certificate> username_certificate = new HashMap<>();
    static HashMap<String, List<String>> username_attributes = new HashMap<>();

    public static void main(String[] args) throws Exception {


        System.setProperty("javax.net.ssl.keyStore", "src/server_tls/server-keystore.jks");
        System.setProperty("javax.net.ssl.trustStore", "src/server_tls/server-truststore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        SSLContext sslContext = SSLContext.getDefault();
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

        SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(9090);
        sslServerSocket.setNeedClientAuth(true);
        System.out.println("Waiting for client connection...");
        while (true) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

            System.out.println("Client connected!");
            String subjectCN = null;
            String clientIp = null;

            X509Certificate[] clientCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
            if (clientCertificates.length > 0) {
                X509Certificate clientCertificate = clientCertificates[0]; // Assuming the client provides a certificate
                subjectCN = extractSubjectCommonName(clientCertificate);
                System.out.println("The first name of the person's certificate -> " + subjectCN);

                clientIp = sslSocket.getInetAddress().getHostAddress();
                System.out.println(clientIp);
                //username_ip.put(subjectCN, clientIp);
               /* if(username_certificate.keySet().contains(subjectCN)){
                    if(username_certificate.get(subjectCN).equals(clientCertificate)) continue;
                    else sslSocket.close();
                }
                else username_certificate.put(subjectCN, clientCertificate);*/
            }

            // Create a BufferedReader to read the client's messages
            BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            //String line = reader.readLine();
            //System.out.println("Received from client: " + line);
            String serverCipherSuite = sslSocket.getSession().getCipherSuite();
            System.out.println("Server Cipher Suite: " + serverCipherSuite);
            String serverTLSVersion = sslSocket.getSession().getProtocol();
            System.out.println("Server TLS Version: " + serverTLSVersion);
            // Create a PrintWriter to send a message to the client
            PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);
            /*writer.println("Hello, client!");
            writer.println("close");
            System.out.println(reader.readLine());*/

            HandleUserThread myThread = new HandleUserThread(subjectCN,clientIp, reader, writer);
            myThread.start();

        }
    }

    private static String extractSubjectCommonName(X509Certificate certificate) {
        String subjectDN = certificate.getSubjectX500Principal().getName();
        String[] dnComponents = subjectDN.split(",");
        for (String component : dnComponents) {
            if (component.trim().startsWith("CN=")) {
                // Extract the CN value
                return component.trim().substring(3);
            }
        }
        return "Unknown";
    }

    public static void registerAttribute(String username, String attribute){
        List<String> attributes = username_attributes.get(username);
        attributes.add(attribute);
        username_attributes.put(username, attributes);
    }

    public static String getIpFromUsername(String username){
        return username_ip.get(username);
    }

    public static String getIpsFromAttribute(String attribute){
        String result="";
        for(String username : username_attributes.keySet()){
            if(username_attributes.get(username).contains(attribute)){
                result=result.concat(username+",");
            }
        }
        result.substring(0,result.length()-1);
        return result;
    }

}