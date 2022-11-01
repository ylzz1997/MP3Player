import java.io.File;

public class Music {
    private File file;
    private String name;
    private boolean enable = false;
    private MusicFormat mf = null;
    private Loader load;


    public Music(String path){
        this.file = new File(path);
        this.name = this.file.getName();
        this.load = new MusicLoader(this.name.split("\\.")[1]);
    }

    public Music(String path,String name){
        this.file = new File(path);
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getMusicBytes() {
        return mf.musicBytesArray;
    }

    public int getMusicBytesNum() {
        return mf.nBytes;
    }

    public int getChannelsNum() {
        return mf.nChannels;
    }

    public int getSamplesPerSec() {
        return mf.nSamplesPerSec;
    }

    public int getBytesPerSample() {
        return mf.bytesPerSample;
    }

    @Override
    public String toString() {
        return name;
    }

    public void load(){
        enable = false;
        mf = new MusicFormat();

        enable = true;
    }

    public void close(){
        mf = null;
        enable = false;
        System.gc();
    }


}