package cn;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.*;


// Server class for tests
class Server {

    static HashMap<String, String> username_ip = new HashMap<>();
    //static HashMap<String, X509Certificate> username_certificate = new HashMap<>();
    static HashMap<String, List<String>> username_attributes = new HashMap<>();
    static HashMap<String,Integer> attribute_id = new HashMap<>();
    static HashMap<String,int[][]> attribute_accesspolicy = new HashMap<>();
    static HashMap<String,String[]> attribute_rhos = new HashMap<>();
    static HashMap<String,String> attribute_accesspolicystring = new HashMap<>();



    public static void main(String[] args) throws Exception {


        System.setProperty("javax.net.ssl.keyStore", "certs/Server/Serverkeystore.jks");
        System.setProperty("javax.net.ssl.trustStore", "certs/Server/Servertruststore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        SSLContext sslContext = SSLContext.getDefault();
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

        SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(9090);
        sslServerSocket.setNeedClientAuth(true);
        System.out.println("Waiting for client connection...");
        while (true) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

            /*SSLSession session = sslSocket.getSession();
            Certificate[] peerCertificates = session.getPeerCertificates();
            System.out.println(peerCertificates.length + " " + peerCertificates[0] + ","+ peerCertificates);*/

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

            registerAttribute("Alice","Movies");
            registerAttribute("Bob","Movies");
            registerAttribute("Charlie","Movies");
            registerAttribute("Alice","Sports");
            registerAttribute("Bob","Sports");
            registerAttribute("Dave","Sports");
            attribute_id.put("Movies",new Random().nextInt(50));
            attribute_id.put("Sports",new Random().nextInt(50));
            /*App.defineAccessPolicyString("40 and (200 or 430 or 30)");
            attribute_accesspolicy.put("Movies",App.accessPolicy);
            System.out.println("policy1:" + Arrays.deepToString(App.accessPolicy));
            System.out.println("rhos1:" + Arrays.toString(App.rhos));
            attribute_rhos.put("Movies", App.rhos);
            //App.defineAccessPolicyString("20 and (200 or 430 or 30)");
            attribute_accesspolicy.put("Sports",App.accessPolicy);
            System.out.println("policy2:" + Arrays.deepToString(App.accessPolicy));
            System.out.println("rhos2:" + Arrays.toString(App.rhos));
            attribute_rhos.put("Sports", App.rhos);*/
            attribute_accesspolicystring.put("Movies", "40 and (200 or 430 or 30)");




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
        if(username_attributes.containsKey(username)){
            username_attributes.get(username).add(attribute);
        }else{
            username_attributes.put(username, new ArrayList<>());
            username_attributes.get(username).add(attribute);
        }
        /*List<String> attributes = username_attributes.get(username);
        attributes.add(attribute);
        username_attributes.put(username, attributes);*/
    }

    public static String getIpFromUsername(String username){
        if(username_ip.get(username)==null) return "";
        return username_ip.get(username);
    }

    public static String getIpsFromAttribute(String attribute){
        String result="";
        for(String username : username_attributes.keySet()){
            if(username_attributes.get(username).contains(attribute)){
                result=result.concat(username+",");
            }
        }
        if(result.isEmpty()) return result;
        result=result.substring(0,result.length()-1);
        return result;
    }

    public static boolean hasAttribute(String user, String attribute){
        System.out.println(username_attributes.keySet() + ",user," + user + ",attribute," + attribute);
        if(username_attributes.containsKey(user)){
            System.out.println("contains key" + attribute + " user " + user);
            return username_attributes.get(user).contains(attribute);
        }

        return false;
    }
    public static int[][] policyForAttribute(String attribute){
        if(attribute_accesspolicy.containsKey(attribute)){
            return attribute_accesspolicy.get(attribute);
        }
        return null;

    }
    public static String[] rhosForAttribute(String attribute){
        if(attribute_rhos.containsKey(attribute)){
            return attribute_rhos.get(attribute);
        }
        return null;

    }
    public static String accessStringForAttribute(String attribute){
        if(attribute_accesspolicystring.containsKey(attribute)){
            return attribute_accesspolicystring.get(attribute);
        }
        return null;

    }

}