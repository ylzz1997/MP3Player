public class MusicLoader implements Loader{
    Loader loader;

    public MusicLoader(String type) {
        if(type.equals("mp3")) loader = new Mp3Loader();
        else if(type.equals("wav")) loader = new WavLoader();
    }

    @Override
    public void load(String filePath, MusicFormat mf) {
        loader.load(filePath,mf);
    }
}

