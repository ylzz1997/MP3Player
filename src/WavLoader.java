public class WavLoader implements Loader{
    static {
        System.loadLibrary("LoadWavMusicTool");
    }
    @Override
    public void load(String filePath, MusicFormat mf) {
        load_jni(filePath,mf);
    }

    private native int load_jni(String filePath,MusicFormat mf);
}
