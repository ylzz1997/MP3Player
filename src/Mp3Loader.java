public class Mp3Loader implements Loader{
    static {
        System.loadLibrary("LoadMusicTool");
    }
    @Override
    public void load(String filePath, MusicFormat mf) {
        load_jni(filePath,mf);
    }

    private native int load_jni(String filePath,MusicFormat mf);
}
