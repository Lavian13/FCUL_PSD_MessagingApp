import javax.crypto.KeyAgreement;
import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import java.util.Scanner;

// Client class
class Client {


    // driver code
    public static void main(String[] args) throws Exception {
        // establish a connection by providing host and port
        // number

        try (Socket socket = new Socket("localhost", 1234)) {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            clientDiffieHellman(dataOutputStream, dataInputStream);

        }
    }

    public static void clientDiffieHellman(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws Exception {
        Scanner scanner = new Scanner(System.in);
        /*
         * Alice creates her own DH key pair with 2048-bit key size
         */
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("EC");
        aliceKpairGen.initialize(521);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

        // Alice creates and initializes her DH KeyAgreement object
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("ECDH");
        aliceKeyAgree.init(aliceKpair.getPrivate());

        // Alice encodes her public key, and sends it over to Bob.
        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();

        dataOutputStream.writeInt(alicePubKeyEnc.length);
        dataOutputStream.write(alicePubKeyEnc);
        System.out.println(Arrays.toString(alicePubKeyEnc));

        /*
         * Alice uses Bob's public key for the first (and only) phase
         * of her version of the DH
         * protocol.
         * Before she can do so, she has to instantiate a DH public key
         * from Bob's encoded key material.
         */

        // Read the length of the array and then the array itself
        int length = dataInputStream.readInt();
        byte[] bobPubKeyEnc = new byte[length];
        dataInputStream.readFully(bobPubKeyEnc);

        KeyFactory aliceKeyFac = KeyFactory.getInstance("EC");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
        PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
        aliceKeyAgree.doPhase(bobPubKey, true);

        /*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
        int aliceLen = aliceSharedSecret.length;
        dataOutputStream.writeInt(aliceLen);
        dataOutputStream.write(aliceSharedSecret);
        byte[] bobSharedSecret = new byte[aliceLen];
        dataInputStream.readFully(bobSharedSecret);

        if (!java.util.Arrays.equals(aliceSharedSecret, bobSharedSecret))
            throw new Exception("Shared secrets differ");
        System.out.println("Shared secrets are the same");

        System.out.print("Enter a message: ");
        String userInput = scanner.nextLine();
        SecretKeySpec aliceAesKey = new SecretKeySpec(aliceSharedSecret, 0, 16, "AES");
        Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        bobCipher.init(Cipher.ENCRYPT_MODE, aliceAesKey);
        byte[] cleartext = userInput.getBytes();
        byte[] ciphertext = bobCipher.doFinal(cleartext);
        byte[] encodedParams = bobCipher.getParameters().getEncoded();
        dataOutputStream.writeInt(ciphertext.length);
        dataOutputStream.write(ciphertext);
        dataOutputStream.writeInt(encodedParams.length);
        dataOutputStream.write(encodedParams);

    }


}


