package com.project.videofy;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

/*import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;*/
import org.xml.sax.SAXException;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    public static MediaMetadataRetriever mp4Info(Context context, Uri uri, String channelName) throws IOException {

        //Log.e("VIDEO_PATH", path);
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        //mediaMetadataRetriever.setDataSource(path);
        mediaMetadataRetriever.setDataSource(context, uri);

        //Intent in = new Intent();
        return mediaMetadataRetriever;
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
