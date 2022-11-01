import javax.sound.sampled.*;
import javax.swing.*;
import java.io.*;

public class Controller {
    public DefaultListModel<Music> musicList = new DefaultListModel<Music>();
    private Music nowMusic;
    private float TotalTime = 0;
    private int TotalFrame = 0;
    private Thread playerThread = null;
    private float nowTime = 0;


    public void load(Music m){
        nowMusic = m;
        m.load();
        this.TotalFrame = m.getMusicBytesNum();
        this.TotalTime = ((float)this.TotalFrame/(float)(nowMusic.getChannelsNum() * (nowMusic.getBytesPerSample() / 8)))/nowMusic.getSamplesPerSec();
    }

    public float getTotalTime(){
        return this.TotalTime;
    }

    public float getNowTime() {
        return nowTime;
    }

    public void start(){
        if(playerThread != null){
            if(playerThread.getState()== Thread.State.TIMED_WAITING){
                playerThread.resume();
                return;
            }
            playerThread.stop();
            playerThread=null;
        }
        playerThread = new Thread(()->{
            Controller.this.pStart();
        });
        playerThread.start();
    }

    public void start(float time){
        if(playerThread != null){
            playerThread.stop();
            playerThread=null;
        }
        playerThread = new Thread(()->{
           Controller.this.pStart(time);
        });
        playerThread.start();
    }

    public void pause(){
        if(playerThread != null){
            playerThread.suspend();
        }
    }

    public void stop(){
        if(playerThread != null){
            playerThread.stop();
            playerThread=null;
        }
        if(nowMusic !=null){
            nowMusic.close();
            nowMusic = null;
        }
    }

  private void pStart(){
      AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,nowMusic.getSamplesPerSec(),nowMusic.getBytesPerSample(), nowMusic.getChannelsNum(), nowMusic.getChannelsNum() * (nowMusic.getBytesPerSample()/8), nowMusic.getSamplesPerSec(), false);
      DataLine.Info dinfo = new DataLine.Info(SourceDataLine.class,af, AudioSystem.NOT_SPECIFIED);
      SourceDataLine line = null;
      InputStream music=new ByteArrayInputStream(nowMusic.getMusicBytes());
      int len = -1;
      try {
          line = (SourceDataLine) AudioSystem.getLine(dinfo);
          line.open(af);
          line.start();
          byte[] buffer = new byte[1024];
          while ((len = music.read(buffer)) > 0) {
              line.write(buffer, 0, len);
              nowTime = (float)line.getMicrosecondPosition()/1000000;
          }
          line.drain();
      } catch (Exception e) {
          e.printStackTrace();
      }finally {
          line.stop();
          line.close();
      }
  }

    private void pStart(float time) {
        int frame = (int)(time*this.nowMusic.getSamplesPerSec()*nowMusic.getChannelsNum() * (nowMusic.getBytesPerSample() / 8));
        if(frame>TotalFrame){
            frame = TotalFrame-1;
        }
        AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, nowMusic.getSamplesPerSec(), nowMusic.getBytesPerSample(), nowMusic.getChannelsNum(), nowMusic.getChannelsNum() * (nowMusic.getBytesPerSample() / 8), nowMusic.getSamplesPerSec(), false);
        DataLine.Info dinfo = new DataLine.Info(SourceDataLine.class, af, AudioSystem.NOT_SPECIFIED);
        SourceDataLine line = null;
        InputStream music = new ByteArrayInputStream(nowMusic.getMusicBytes(), frame, (TotalFrame - frame + 1));
        int len = -1;
        try {
            line = (SourceDataLine) AudioSystem.getLine(dinfo);
            line.open(af);
            line.start();
            byte[] buffer = new byte[1024];
            while ((len = music.read(buffer)) > 0) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                line.write(buffer, 0, len);
                nowTime =  time + (float) line.getMicrosecondPosition() / 1000000;
            }
            line.drain();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            line.stop();
            line.close();
        }
    }
}