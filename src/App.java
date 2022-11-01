public class App {
    public Windows appWindows;
    public Controller controller;
    public void start(){
        this.controller = new Controller();
        this.controller.musicList.addElement(new Music("Richard Clayderman - 水边的阿狄丽娜.mp3"));
        this.controller.musicList.addElement(new Music("土星皇家交响乐团 - 巴赫：G弦上的咏叹调（数字录音）.mp3"));
        appWindows = new Windows("音乐播放器",800,800,this.controller);

    }
}
