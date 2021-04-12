package views;

import com.github.sarxos.webcam.Webcam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoCam {
    //TODO 考虑是否使用一个类来切换摄像头
    private Map<String,Webcam> webcamMap;
    private List<Webcam> webcams;
    private Webcam webcam;
    private int nums;
    private String[] device_names;

    public VideoCam() {}

    public VideoCam(List<Webcam> list) {
        this.webcams = list;
        this.webcam = list.get(0);
        this.nums = list.size();
        this.device_names = new String[this.nums];
        for(int i=0;i<this.nums;i++) {
            this.device_names[i] = list.get(i).toString().substring("Webcam ".length());
        }
        this.webcamMap = new HashMap<String, Webcam>();
        for(int i=0;i<this.nums;i++) {
            this.webcamMap.put(this.device_names[i], list.get(i));
        }
    }

    public List<Webcam> getWebcams() {
        return webcams;
    }

    public void setWebcams(List<Webcam> webcams) {
        this.webcams = webcams;
    }

    public Webcam getWebcam() {
        return webcam;
    }

    public void setWebcam(Webcam webcam) {
        this.webcam = webcam;
    }

    public Webcam getWebcamByName(String name) {
        return this.webcamMap.get(name);
    }

    public int getNums() {
        return nums;
    }

    public void setNums(int nums) {
        this.nums = nums;
    }

    public String[] getDevice_names() {
        return device_names;
    }

    public void setDevice_names(String[] device_names) {
        this.device_names = device_names;
    }
}
