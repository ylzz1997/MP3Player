import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Windows extends JFrame {
    public String name;
    public int width;
    public int height;

    public Controller controller;

    public JList musicList = new JList();
    public JPanel musicController = new JPanel();
    public JSlider musicPBar = new JSlider();
    public JLabel musicTime = new JLabel();
    public JButton startB = new JButton();
    public JButton stopB = new JButton();
    public JButton pauseB = new JButton();
    public Thread watch = null;
    public String totalTime ="0:0";

    public Windows(String name, int width, int height, Controller controller)
    {
        super();
        this.controller = controller;
        this.name = name;
        this.width = width;
        this.height = height;
        this.setTitle(name);
        this.setSize(width,height);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setLayout(null);
        musicList.setBounds(10,10,350,740);
        musicList.setModel(controller.musicList);
        musicList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (musicList.getSelectedIndex() != -1) {
                    if (e.getClickCount() == 2){
                        DoubleClickMusicList();
                    }
                }
            }
        });

        this.add(musicList);

        musicController.setLayout(null);
        musicController.setBounds(370,10,400,740);

        musicPBar.setBounds(0,0,400,30);
        musicPBar.setMinimum(0);
        musicPBar.setValue(0);
        musicPBar.setEnabled(false);
        musicPBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                musicPBar.removeChangeListener(musicPBar.getChangeListeners()[0]);
                if(watch!=null) {
                    watch.stop();
                    watch=null;
                }
                controller.start(musicPBar.getValue());
                updateGUI();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(watch!=null) {
                    watch.stop();
                    watch=null;
                }
                musicPBar.addChangeListener((e2)->{
                    musicTime.setText(se2ms(musicPBar.getValue())+"/"+totalTime);
                });
            }
        });


        musicTime.setText("0:0/0:0");
        musicTime.setBounds(300,5,100,60);

        startB.setText("播放");
        startB.setBounds(10,50,100,20);
        startB.addActionListener((e)->{
            if(watch==null) updateGUI();
            controller.start();
            musicPBar.setEnabled(true);
        });

        stopB.setText("停止");
        stopB.setBounds(230,50,100,20);
        stopB.addActionListener((e)->{
            if(watch!=null){
                watch.stop();
                watch=null;
            }
            controller.stop();
            musicTime.setText("0:0/0:0");
            musicPBar.setValue(0);
            musicPBar.setEnabled(false);
        });

        pauseB.setText("暂停");
        pauseB.setBounds(120,50,100,20);
        pauseB.addActionListener((e)->{
            controller.pause();
        });

        musicController.add(musicPBar);
        musicController.add(musicTime);
        musicController.add(startB);
        musicController.add(stopB);
        musicController.add(pauseB);



        this.add(musicController);

        this.setVisible(true);
    }

    private void DoubleClickMusicList(){
        musicPBar.setEnabled(true);
        if(watch!=null) {
            watch.stop();
            watch=null;
        }
        controller.stop();
        Music music = (Music)musicList.getSelectedValue();
        controller.load(music);
        musicPBar.setMaximum((int)controller.getTotalTime());
        totalTime = se2ms(controller.getTotalTime());
        musicTime.setText("0:0/"+totalTime);
        controller.start();
        updateGUI();
    }

    private void updateGUI(){
        watch = new Thread(()->{
            String nowTime = "0:0";
            while (true){
                nowTime = se2ms(controller.getNowTime());
                musicTime.setText(nowTime+"/"+totalTime);
                musicPBar.setValue((int)controller.getNowTime());
            }
        });
        watch.start();
    }

    private String se2ms(float seconds){
        int minutes =((int)seconds / 60);
        int remainingSeconds =((int)seconds % 60);
        return (minutes+":"+remainingSeconds);
    }
}