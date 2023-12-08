package cn;


import cn.edu.buaa.crypto.access.parser.ParserUtils;
import cn.edu.buaa.crypto.access.parser.PolicySyntaxException;
import cn.edu.buaa.crypto.algebra.serparams.PairingCipherSerParameter;
import cn.edu.buaa.crypto.algebra.serparams.PairingKeySerPair;
import cn.edu.buaa.crypto.algebra.serparams.PairingKeySerParameter;
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


/**
 * Hello world!
 *
 */
public class App {

    static String pairingParameters = "src/main/java/cn/edu/buaa/crypto/encryption/abe/kpabe/a1_2_128.properties";
    static final String accessPolicyString = "40 and (200 or 430 or 30)";
    static final String[] attributes = new String[]{"40", "200"};
    static int[][] accessPolicy;

    static {
        try {
            accessPolicy = ParserUtils.GenerateAccessPolicy(accessPolicyString);
        } catch (PolicySyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static String[] rhos;

    static {
        try {
            rhos = ParserUtils.GenerateRhos(accessPolicyString);
        } catch (PolicySyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static PairingParameters pg = PairingFactory.getPairingParameters(pairingParameters);
    static Pairing pairing = PairingFactory.getPairing(pg);
    static PairingKeySerPair keyPair = KPABEGPSW06aEngine.getInstance().setup(pg, 500);
    static PairingKeySerParameter publicKey = keyPair.getPublic();


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

        byte[] byteArrayPublicKey = SerCipherParameter(publicKey);
        CipherParameters anPublicKey = deserCipherParameters(byteArrayPublicKey);

        System.out.println(publicKey.equals(anPublicKey));
        publicKey = (PairingKeySerParameter) anPublicKey;

        PairingKeySerParameter masterKey = keyPair.getPrivate();
        byte[] byteArrayMasterKey = SerCipherParameter(masterKey);
        CipherParameters anMasterKey = deserCipherParameters(byteArrayMasterKey);
        System.out.println(masterKey.equals(anMasterKey));
        masterKey = (PairingKeySerParameter) anMasterKey;

        //KeyGen and serialization
        PairingKeySerParameter secretKey = KPABEGPSW06aEngine.getInstance().keyGen(publicKey, masterKey, accessPolicy, rhos);
        byte[] byteArraySecretKey = SerCipherParameter(secretKey);
        CipherParameters anSecretKey = deserCipherParameters(byteArraySecretKey);
        System.out.println(secretKey.equals(anSecretKey));
        secretKey = (PairingKeySerParameter) anSecretKey;

        //Encryption and serialization
        String messageString = "This is a confidential message.";
        //Message needed
        Element message = pairing.getGT().newElementFromBytes(messageString.getBytes(StandardCharsets.UTF_8));
        PairingCipherSerParameter encrypted =  encrypt(message);
        //Decryption
        Element decrypted = decrypt(encrypted, secretKey);


        //String s = Arrays.toString(decrypted.toBytes());
        //s=s.replaceAll("0","").trim().replaceAll(",","").trim();



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

    public static Element decrypt(PairingCipherSerParameter encrypted, PairingKeySerParameter secretKey) throws InvalidCipherTextException {
        Element anMessage = KPABEGPSW06aEngine.getInstance().decryption(publicKey, secretKey, attributes, encrypted);
        //System.out.println(message.equals(anMessage));
        String decryptedMessage = new String(anMessage.toBytes(), StandardCharsets.UTF_8);
        System.out.println("Decrypted Message: " + (new String(anMessage.toBytes())).replace("\0", "").trim());
        System.out.println(anMessage);
        return anMessage;

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

