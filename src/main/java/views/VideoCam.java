package views;

import com.github.sarxos.webcam.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoCam {
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

//class NewWebCam extends Webcam {
//
//    public NewWebCam(WebcamDevice webcamDevice) {
//        super(webcamDevice);
//    }
//
//    @Override
//    protected void notifyWebcamImageAcquired(BufferedImage image) {
//        super.notifyWebcamImageAcquired(image);
//    }
//
//    @Override
//    public boolean open() {
//        return super.open();
//    }
//
//    @Override
//    public boolean open(boolean async) {
//        return super.open(async);
//    }
//
//    @Override
//    public boolean open(boolean async, WebcamUpdater.DelayCalculator delayCalculator) {
//        return super.open(async, delayCalculator);
//    }
//
//    @Override
//    public boolean close() {
//        return super.close();
//    }
//
//    @Override
//    public WebcamDevice getDevice() {
//        return super.getDevice();
//    }
//
//    @Override
//    protected void dispose() {
//        super.dispose();
//    }
//
//    @Override
//    protected BufferedImage transform(BufferedImage image) {
//        return super.transform(image);
//    }
//
//    @Override
//    public boolean isOpen() {
//        return super.isOpen();
//    }
//
//    @Override
//    public Dimension getViewSize() {
//        return super.getViewSize();
//    }
//
//    @Override
//    public Dimension[] getViewSizes() {
//        return super.getViewSizes();
//    }
//
//    @Override
//    public void setCustomViewSizes(Dimension... sizes) {
//        super.setCustomViewSizes(sizes);
//    }
//
//    @Override
//    public Dimension[] getCustomViewSizes() {
//        return super.getCustomViewSizes();
//    }
//
//    @Override
//    public void setViewSize(Dimension size) {
//        super.setViewSize(size);
//    }
//
//    @Override
//    public BufferedImage getImage() {
//        return super.getImage();
//    }
//
//    @Override
//    public boolean isImageNew() {
//        return super.isImageNew();
//    }
//
//    @Override
//    public double getFPS() {
//        return super.getFPS();
//    }
//
//    @Override
//    public ByteBuffer getImageBytes() {
//        return super.getImageBytes();
//    }
//
//    @Override
//    public void getImageBytes(ByteBuffer target) {
//        super.getImageBytes(target);
//    }
//
//    @Override
//    public void setParameters(Map<String, ?> parameters) {
//        super.setParameters(parameters);
//    }
//
//    @Override
//    public String getName() {
//        return super.getName();
//    }
//
//    @Override
//    public String toString() {
//        return super.toString();
//    }
//
//    @Override
//    public boolean addWebcamListener(WebcamListener l) {
//        return super.addWebcamListener(l);
//    }
//
//    @Override
//    public WebcamListener[] getWebcamListeners() {
//        return super.getWebcamListeners();
//    }
//
//    @Override
//    public int getWebcamListenersCount() {
//        return super.getWebcamListenersCount();
//    }
//
//    @Override
//    public boolean removeWebcamListener(WebcamListener l) {
//        return super.removeWebcamListener(l);
//    }
//
//    @Override
//    public WebcamImageTransformer getImageTransformer() {
//        return super.getImageTransformer();
//    }
//
//    @Override
//    public void setImageTransformer(WebcamImageTransformer transformer) {
//        super.setImageTransformer(transformer);
//    }
//
//    @Override
//    public WebcamLock getLock() {
//        return super.getLock();
//    }
//}