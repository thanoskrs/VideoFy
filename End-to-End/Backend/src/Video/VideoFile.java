package Video;
import java.io.Serializable;
import java.util.ArrayList;

public class VideoFile implements Serializable {

    public String videoURL, videoPublished, videoHashtags;
    private String VideoName;
    private String ChannelName;
    private String dateCreated;
    private long length;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private ArrayList<String> associatedHashTags;
    private byte[] videoFileChunk;

    public VideoFile() {}

    public VideoFile(String videoName, String channelName, String dateCreated, long length, String framerate, String frameWidth, String frameHeight, ArrayList<String> associatedHashTags, byte[] videoFileChunk) {
        VideoName = videoName;
        ChannelName = channelName;
        this.dateCreated = dateCreated;
        this.length = length;
        this.framerate = framerate;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.associatedHashTags = associatedHashTags;
        this.videoFileChunk = videoFileChunk;
    }
    // setters
    public void setVideoName(String videoName) {
        VideoName = videoName;
    }

    public void setChannelName(String channelName) {
        ChannelName = channelName;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setFramerate(String framerate) {
        this.framerate = framerate;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(String frameHeight) {
        this.frameHeight = frameHeight;
    }

    // crysa
    public void setVideoFileChunk(byte[] videoFileChunk) {
        this.videoFileChunk = videoFileChunk;
    }

    // getters

    public String getVideoName() {
        return VideoName;
    }

    public String getChannelName() {
        return ChannelName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public long getLength() {
        return length;
    }

    public String getFramerate() {
        return framerate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public void addHashTag(String hashTag) {
        associatedHashTags.add(hashTag);
    }

    public ArrayList<String> getAssociatedHashTags() {
        return associatedHashTags;
    }

    public String getHashtags() {
        String hashtags = "";
        for (String hashtag:associatedHashTags ) {
            hashtags += "#" + hashtag + " ";
        }
        return hashtags;
    }

    public byte[] getVideoFileChunk() {
        return videoFileChunk;
    }

    public void print_information()
    {
        System.out.println("VideoName : " + getVideoName());
        System.out.println("ChannelName : " + getChannelName());
        System.out.println("DateCreated : " + getDateCreated());
        System.out.println("FrameHeight : " + getFrameHeight());
        System.out.println("Framerate : " + getFramerate());
        System.out.println("FrameWidth : " + getFrameWidth());
        System.out.println("Length : " + getLength());
        System.out.println("AssociatedHashTags : " + getAssociatedHashTags());
        //   System.out.println("VideoFileChunk : " + Arrays.toString(getVideoFileChunk()));
    }
}