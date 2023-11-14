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
            X509Certificate[] clientCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
            if (clientCertificates.length > 0) {
                X509Certificate clientCertificate = clientCertificates[0]; // Assuming the client provides a certificate
                String subjectCN = extractSubjectCommonName(clientCertificate);
                System.out.println("The first name of the person's certificate -> " + subjectCN);

                String clientIp = sslSocket.getInetAddress().getHostAddress();
                username_ip.put(subjectCN, clientIp); //DOES SUBJECTCN MAKE SENSE.
                //WHAT HAPPENS IF THERE IS NO CERTIFICATE; WE STILL KEEP THE CONNECTION WHY?
            }

            // Create a BufferedReader to read the client's messages
            BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            String line = reader.readLine();
            System.out.println("Received from client: " + line);
            String serverCipherSuite = sslSocket.getSession().getCipherSuite();
            System.out.println("Server Cipher Suite: " + serverCipherSuite);
            String serverTLSVersion = sslSocket.getSession().getProtocol();
            System.out.println("Server TLS Version: " + serverTLSVersion);
            // Create a PrintWriter to send a message to the client
            PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);
            writer.println("Hello, client!");
            writer.println("close");
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

}