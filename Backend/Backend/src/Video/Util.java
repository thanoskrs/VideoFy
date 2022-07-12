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
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import java.nio.file.*;

public class Util {

    public static Metadata mp4Info(String path, String channelName) throws IOException,SAXException, TikaException {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(new File(path));
        ParseContext pcontext = new ParseContext();

        // auto edw dinei sto video to onoma tou channel name pou tha exei dimiourgisei to video
        metadata.set(TikaCoreProperties.CREATOR, channelName);

        //Html parser
        MP4Parser MP4Parser = new MP4Parser();
        MP4Parser.setMaxRecordSize(Long.MAX_VALUE);
        MP4Parser.parse(inputstream, handler, metadata, pcontext);

        return metadata;
    }

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
