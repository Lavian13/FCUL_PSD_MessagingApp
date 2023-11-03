import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import java.nio.charset.StandardCharsets;

// Server class
class Server {

    public static void main(String[] args) throws Exception {
        ServerSocket server = null;

        try {


            server = new ServerSocket(1234);
            server.setReuseAddress(true);


            while (true) {


                Socket client = server.accept();

                System.out.println("New client connected" + client.getInetAddress().getHostAddress());


                ClientHandler clientSock = new ClientHandler(client);


                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        // Constructor
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                OutputStream outputStream = clientSocket.getOutputStream();
                InputStream inputStream = clientSocket.getInputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                serverDH_AES(dataOutputStream, dataInputStream);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static void serverDH_AES(DataOutputStream dataOutputStream, DataInputStream dataInputStream) {
            try {// Read the length of the array and then the array itself
                int length = dataInputStream.readInt();
                byte[] alicePubKeyEnc = new byte[length];
                dataInputStream.readFully(alicePubKeyEnc);


                System.out.println(Arrays.toString(alicePubKeyEnc));

                /*
                 * Let's turn over to Bob. Bob has received Alice's public key
                 * in encoded format.
                 * He instantiates a DH public key from the encoded key material.
                 */
                KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);

                PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

                /*
                 * Bob gets the DH parameters associated with Alice's public key.
                 * He must use the same parameters when he generates his own key
                 * pair.
                 */
                DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey) alicePubKey).getParams();

                // Bob creates his own DH key pair

                KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
                bobKpairGen.initialize(dhParamFromAlicePubKey);
                KeyPair bobKpair = bobKpairGen.generateKeyPair();

                // Bob creates and initializes his DH KeyAgreement object

                KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
                bobKeyAgree.init(bobKpair.getPrivate());

                // Bob encodes his public key, and sends it over to Alice.
                byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();
                dataOutputStream.writeInt(bobPubKeyEnc.length);
                dataOutputStream.write(bobPubKeyEnc);


                bobKeyAgree.doPhase(alicePubKey, true);

                // Read the length of the array and then the array itself
                length = dataInputStream.readInt();
                byte[] aliceSharedSecret = new byte[length];
                dataInputStream.readFully(aliceSharedSecret);

                byte[] bobSharedSecret = new byte[length];
                bobKeyAgree.generateSecret(bobSharedSecret, 0);

                if (!java.util.Arrays.equals(aliceSharedSecret, bobSharedSecret))
                    throw new Exception("Shared secrets differ");
                System.out.println("Shared secrets are the same");

                dataOutputStream.write(bobSharedSecret);

                SecretKeySpec bobAesKey = new SecretKeySpec(bobSharedSecret, 0, 16, "AES");
                length = dataInputStream.readInt();
                byte[] ciphertext = new byte[length];
                dataInputStream.readFully(ciphertext);
                String encryptedMessage = new String(ciphertext, StandardCharsets.UTF_8);
                System.out.println("Mensagem cifrada: " + encryptedMessage);

                length = dataInputStream.readInt();
                byte[] encodedParams = new byte[length];
                dataInputStream.readFully(encodedParams);

                AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                aesParams.init(encodedParams);
                Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                aliceCipher.init(Cipher.DECRYPT_MODE, bobAesKey, aesParams);
                byte[] recovered = aliceCipher.doFinal(ciphertext);
                String decryptedMessage = new String(recovered, StandardCharsets.UTF_8);
                System.out.println("Mensagem decifrada: " + decryptedMessage);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
