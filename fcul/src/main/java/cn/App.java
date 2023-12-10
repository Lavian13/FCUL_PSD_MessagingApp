package cn;


import cn.edu.buaa.crypto.access.parser.ParserUtils;
import cn.edu.buaa.crypto.access.parser.PolicySyntaxException;
import cn.edu.buaa.crypto.algebra.serparams.PairingCipherSerParameter;
import cn.edu.buaa.crypto.algebra.serparams.PairingKeySerPair;
import cn.edu.buaa.crypto.algebra.serparams.PairingKeySerParameter;
import cn.edu.buaa.crypto.encryption.abe.cpabe.bsw07.CPABEBSW07Engine;
import cn.edu.buaa.crypto.encryption.abe.kpabe.gpsw06a.KPABEGPSW06aEngine;
//import com.example.access.AccessPolicyExamples;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;


/**
 * Hello world!
 *
 */
public class App {

    static String pairingParameters = "src/main/java/cn/edu/buaa/crypto/encryption/abe/kpabe/a_80_256.properties";
    static String accessPolicyString; //= "40 and (200 or 430 or 30)";
    static final String[] attributes = new String[]{"40", "200"};
    static int[][] accessPolicy;



    static String[] rhos;



    static PairingParameters pg = PairingFactory.getPairingParameters(pairingParameters);
    static Pairing pairing = PairingFactory.getPairing(pg);
    //static PairingKeySerPair keyPair = KPABEGPSW06aEngine.getInstance().setup(pg, 500);
    static PairingKeySerPair keyPair = CPABEBSW07Engine.getInstance().setup(pg, 500);

    static PairingKeySerParameter publicKey = keyPair.getPublic();
    static PairingKeySerParameter masterKey;
    static PairingKeySerParameter secretKey;


    public static byte[] SerCipherParameter(CipherParameters cipherParameters) throws IOException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(cipherParameters);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return byteArray;
    }

    public static CipherParameters deserCipherParameters(byte[] byteArrays) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrays);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        CipherParameters cipherParameters = (CipherParameters)objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return cipherParameters;
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException, PolicySyntaxException, InvalidCipherTextException {
        System.out.println("Hello World!");


        //setup();

        //defineAccessPolicyString("40 and (200 or 430 or 30)");


        //KeyGen and serialization
        keyGen();

        String encrypted = encryptString("teste",accessPolicyString);
        //encryptString("ola", accessPolicyString);
        //Encryption and serialization
        String messageString = "This is a confidential message.";
        //Message needed
        Element message = pairing.getGT().newElementFromBytes(messageString.getBytes(StandardCharsets.UTF_8));
        //String encrypted =  encryptString(messageString,accessPolicyString);
        System.out.println("encrypted message:" + encrypted);
        //Decryption
        String decrypted = decryptString(encrypted, secretKey, accessPolicyString);

        System.out.println("mensagem desencriptada"+decrypted);
        //String s = Arrays.toString(decrypted.toBytes());
        //s=s.replaceAll("0","").trim().replaceAll(",","").trim();



    }

    public static void defineAccessPolicyString(String s) throws PolicySyntaxException {
        accessPolicyString=s;
        accessPolicy = ParserUtils.GenerateAccessPolicy(accessPolicyString);
        rhos = ParserUtils.GenerateRhos(accessPolicyString);
    }

    public static PairingKeySerParameter setup() throws IOException, ClassNotFoundException {

        byte[] byteArrayPublicKey = SerCipherParameter(publicKey);
        CipherParameters anPublicKey = deserCipherParameters(byteArrayPublicKey);

        System.out.println(publicKey.equals(anPublicKey));
        publicKey = (PairingKeySerParameter) anPublicKey;

        masterKey = keyPair.getPrivate();
        byte[] byteArrayMasterKey = SerCipherParameter(masterKey);
        CipherParameters anMasterKey = deserCipherParameters(byteArrayMasterKey);
        System.out.println(masterKey.equals(anMasterKey));
        masterKey = (PairingKeySerParameter) anMasterKey;
        return publicKey;
    }

    public static PairingKeySerParameter keyGen() throws IOException, ClassNotFoundException, PolicySyntaxException {
        //secretKey = KPABEGPSW06aEngine.getInstance().keyGen(publicKey, masterKey, accessPolicy, rhos);
        secretKey = CPABEBSW07Engine.getInstance().keyGen(publicKey,masterKey,attributes);
        byte[] byteArraySecretKey = SerCipherParameter(secretKey);
        CipherParameters anSecretKey = deserCipherParameters(byteArraySecretKey);
        System.out.println(secretKey.equals(anSecretKey));
        secretKey = (PairingKeySerParameter) anSecretKey;
        return secretKey;
    }

    public static PairingCipherSerParameter encrypt(Element message) throws IOException, ClassNotFoundException {

        PairingCipherSerParameter ciphertext = KPABEGPSW06aEngine.getInstance().encryption(publicKey, attributes, message);
        byte[] byteArrayCiphertext = SerCipherParameter(ciphertext);
        CipherParameters anCiphertext = deserCipherParameters(byteArrayCiphertext);
        System.out.println(ciphertext.equals(anCiphertext));
        ciphertext = (PairingCipherSerParameter) anCiphertext;
        System.out.println(ciphertext instanceof PairingCipherSerParameter);
        return ciphertext;

    }
    public static String encryptString(String message, String accessString) throws IOException, ClassNotFoundException, PolicySyntaxException {
        System.out.println("Mensagem antes de encriptar" + message);
        System.out.println("access antes de encriptar" + Arrays.deepToString(accessPolicy));
        System.out.println("rhos antes de encriptar" + Arrays.toString(rhos));
        //String messageTest = "Test";
        //PairingCipherSerParameter ciphertextTest = KPABEGPSW06aEngine.getInstance().encryption(publicKey, attributes, elementTest);

        Element elementTest = pairing.getGT().newElementFromBytes(message.getBytes(StandardCharsets.UTF_8));
        System.out.println("Elementenc: " + elementTest);
        System.out.println("publickey: " + publicKey.getParameters());
        System.out.println("accessstrin: " + accessString);
        PairingCipherSerParameter ciphertextTest = CPABEBSW07Engine.getInstance().encryption(publicKey, accessString, elementTest);
        byte[] byteArrayCiphertextTest = SerCipherParameter(ciphertextTest);
        String str = Base64.getEncoder().encodeToString(byteArrayCiphertextTest);
        System.out.println(str);
        return str;
    }
    public static String encryptStringPublic(String message, String accessString, PairingKeySerParameter publicKey) throws IOException, ClassNotFoundException, PolicySyntaxException {
        System.out.println("Mensagem antes de encriptar" + message);
        System.out.println("access antes de encriptar" + Arrays.deepToString(accessPolicy));
        System.out.println("rhos antes de encriptar" + Arrays.toString(rhos));
        //String messageTest = "Test";
        //PairingCipherSerParameter ciphertextTest = KPABEGPSW06aEngine.getInstance().encryption(publicKey, attributes, elementTest);

        Element elementTest = pairing.getGT().newElementFromBytes(message.getBytes(StandardCharsets.UTF_8));
        System.out.println("Elementenc: " + elementTest);
        System.out.println("publickey: " + publicKey.getParameters());
        System.out.println("accessstrin: " + accessString);
        PairingCipherSerParameter ciphertextTest = CPABEBSW07Engine.getInstance().encryption(publicKey, accessString, elementTest);
        byte[] byteArrayCiphertextTest = SerCipherParameter(ciphertextTest);
        String str = Base64.getEncoder().encodeToString(byteArrayCiphertextTest);
        System.out.println(str);
        return str;
    }

    public static Element decrypt(PairingCipherSerParameter encrypted, PairingKeySerParameter secretKey) throws InvalidCipherTextException, PolicySyntaxException {
        //Element anMessage = KPABEGPSW06aEngine.getInstance().decryption(publicKey, secretKey, attributes, encrypted);
        Element anMessage = CPABEBSW07Engine.getInstance().decryption(publicKey, secretKey, accessPolicyString, encrypted);
        // System.out.println(message.equals(anMessage));
        String decryptedMessage = new String(anMessage.toBytes(), StandardCharsets.UTF_8);
        System.out.println("Decrypted Message: " + (decryptedMessage).replace("\0", "").trim());
        System.out.println(anMessage);
        return anMessage;



    }
    public static String decryptString(String str, PairingKeySerParameter secretkey,String accessString) throws InvalidCipherTextException, IOException, ClassNotFoundException, PolicySyntaxException {
        /*byte[] receivedBytes = Base64.getDecoder().decode(str);
        PairingCipherSerParameter TestCiphertext = (PairingCipherSerParameter) deserCipherParameters(receivedBytes);
        Element TestMessage = CPABEBSW07Engine.getInstance().decryption(publicKey, secretKey, policy, rhos, TestCiphertext);

        String decryptedReceived = new String(TestMessage.toBytes(), StandardCharsets.UTF_8);
        decryptedReceived=decryptedReceived.replace("\0", "").trim();
        System.out.println(decryptedReceived);
        return decryptedReceived;*/

        System.out.println("Message to decrypt: " + str);

        byte[] receivedBytes = Base64.getDecoder().decode(str);
        System.out.println("Message to decrypt bytes: " + Arrays.toString(receivedBytes));
        PairingCipherSerParameter TestCiphertext = (PairingCipherSerParameter) deserCipherParameters(receivedBytes);
        System.out.println("publickey: " + publicKey.getParameters());
        //System.out.println("secret: " + secretKey);
        System.out.println("accessstrin: " + accessString);
        Element TestMessage = CPABEBSW07Engine.getInstance().decryption(publicKey, secretkey, accessString, TestCiphertext);
        System.out.println("Elementde: " + TestMessage);
        String decryptedReceived = new String(TestMessage.toBytes(), StandardCharsets.UTF_8);
        System.out.println("Decrypted Message: " + decryptedReceived);
        decryptedReceived=decryptedReceived.replace("\0", "").trim();
        System.out.println("Decrypted Message: " + decryptedReceived.replace("\0", "").trim());
        return decryptedReceived;
    }
    public static String decryptStringPublic(String str, PairingKeySerParameter secretkey,String accessString, PairingKeySerParameter publicKey) throws InvalidCipherTextException, IOException, ClassNotFoundException, PolicySyntaxException {
        /*byte[] receivedBytes = Base64.getDecoder().decode(str);
        PairingCipherSerParameter TestCiphertext = (PairingCipherSerParameter) deserCipherParameters(receivedBytes);
        Element TestMessage = CPABEBSW07Engine.getInstance().decryption(publicKey, secretKey, policy, rhos, TestCiphertext);

        String decryptedReceived = new String(TestMessage.toBytes(), StandardCharsets.UTF_8);
        decryptedReceived=decryptedReceived.replace("\0", "").trim();
        System.out.println(decryptedReceived);
        return decryptedReceived;*/

        System.out.println("Message to decrypt: " + str);

        byte[] receivedBytes = Base64.getDecoder().decode(str);
        System.out.println("Message to decrypt bytes: " + Arrays.toString(receivedBytes));
        PairingCipherSerParameter TestCiphertext = (PairingCipherSerParameter) deserCipherParameters(receivedBytes);
        System.out.println("publickey: " + publicKey.getParameters());
        //System.out.println("secret: " + secretKey);
        System.out.println("accessstrin: " + accessString);
        Element TestMessage = CPABEBSW07Engine.getInstance().decryption(publicKey, secretkey, accessString, TestCiphertext);
        System.out.println("Elementde: " + TestMessage);
        String decryptedReceived = new String(TestMessage.toBytes(), StandardCharsets.UTF_8);
        System.out.println("Decrypted Message: " + decryptedReceived);
        decryptedReceived=decryptedReceived.replace("\0", "").trim();
        System.out.println("Decrypted Message: " + decryptedReceived.replace("\0", "").trim());
        return decryptedReceived;
    }
}
        /*
        //Encapsulation and serialization
        PairingKeyEncapsulationSerPair encapsulationPair = KPABEGPSW06aEngine.getInstance().encapsulation(publicKey, attributes);
        byte[] sessionKey = encapsulationPair.getSessionKey();
        PairingCipherSerParameter header = encapsulationPair.getHeader();
        byte[] byteArrayHeader = SerCipherParameter(header);
        CipherParameters anHeader = deserCipherParameters(byteArrayHeader);
        System.out.println(header.equals(anHeader));
        header = (PairingCipherSerParameter) anHeader;

        //Decryption
        byte[] anSessionKey = KPABEGPSW06aEngine.getInstance().decapsulation(publicKey, secretKey, attributes, header);
        System.out.println(sessionKey.equals(anSessionKey));
        */

