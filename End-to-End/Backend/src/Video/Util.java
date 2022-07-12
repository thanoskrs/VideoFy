package Video;


import java.io.*;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.xml.sax.SAXException;
import java.nio.file.*;

public class Util {


    public static BigInteger hash (String toBeHashed) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] messageDigest = md.digest(toBeHashed.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        return no;
    }

}
