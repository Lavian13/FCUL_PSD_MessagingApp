package cn;

import cn.edu.buaa.crypto.access.parser.PolicySyntaxException;
import cn.edu.buaa.crypto.algebra.serparams.PairingKeySerParameter;

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
    static HashMap<String, PairingKeySerParameter> attribute_publickey = new HashMap<>();
    static PairingKeySerParameter secretKey;
    static PairingKeySerParameter publicKey;
    static String linesent;




    public static void main(String[] args) throws Exception {
        registerAttribute("Alice","Movies");
        registerAttribute("Bob","Movies");
        registerAttribute("Charlie","Movies");
        registerAttribute("Alice","Sports");
        registerAttribute("Bob","Sports");
        registerAttribute("Dave","Sports");
        String str= "Movies and (";
        String str2 = "Sports and (";
        for (String username : username_attributes.keySet()){
            if(username_attributes.get(username).contains("Movies")){
                str=str.concat(username + " or ");
            }
            if(username_attributes.get(username).contains("Sports")){
                str2=str2.concat(username + " or ");
            }
        }
        str = str.substring(0,str.length()-4) + ")";
        str2=str2.substring(0,str.length()-4) + ")";
        System.out.println(str);
        attribute_accesspolicystring.put("Movies", str);
        attribute_accesspolicystring.put("Sports", str2);

        for (String attribute : attribute_accesspolicystring.keySet()){
            System.out.println(attribute_accesspolicystring.get(attribute));
            App.defineAccessPolicyString(attribute_accesspolicystring.get(attribute));
            publicKey=App.setup();
            attribute_publickey.put(attribute, publicKey);
        }
        /*String [] attributes= new String[]{"40", "120"};
        secretKey= App.keyGen(attributes);
        String text = "Teste";
        text= App.encryptString(text, str);
        System.out.println("Encrypted"+text);
        String aux = HandleUserThread.serializeSecretKey(secretKey);
        secretKey=HandleUserThread.deserializeSecretKey(aux);
        System.out.println("SecretkeyServer"+secretKey);*/
        //System.out.println("decrypted"+App.decryptString(text,secretKey, "40 and (200 or 430 or 30)"));


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


            //attribute_id.put("Movies",new Random().nextInt(50));
            //attribute_id.put("Sports",new Random().nextInt(50));
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



            ObjectOutputStream objectOutputStream = new ObjectOutputStream(sslSocket.getOutputStream());


            HandleUserThread myThread = new HandleUserThread(subjectCN,clientIp, reader, writer,objectOutputStream);
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
    public static PairingKeySerParameter generateSecretKey(String username){
        List<String> attributes = username_attributes.get(username);
        attributes.add(username);
        System.out.println(Arrays.toString(attributes.toArray(new String[0])));
        try {
            return App.keyGen(attributes.toArray(new String[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (PolicySyntaxException e) {
            throw new RuntimeException(e);
        }

    }



}