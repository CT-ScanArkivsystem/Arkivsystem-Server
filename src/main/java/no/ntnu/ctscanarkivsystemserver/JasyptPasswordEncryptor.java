package no.ntnu.ctscanarkivsystemserver;

import org.jasypt.util.text.AES256TextEncryptor;

/**
 * With this class you can encrypt values to use in application.properties.
 * Put the value/word/text as textToEncrypt, then set password as the encryption password.
 * Take the encrypted value and set the application.properties value as: ENC(Value here)
 */
public class JasyptPasswordEncryptor {
    public static void main(String[] args) {

        String textToEncrypt = "appserver";
        String password = "Encryption Password here";

        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(password);
        String myEncryptedText = encryptor.encrypt(textToEncrypt);
        System.out.println("Encrypted: "+myEncryptedText);

        String plainText = encryptor.decrypt(myEncryptedText);
        System.out.println("Decrypted: "+plainText);
    }
}
