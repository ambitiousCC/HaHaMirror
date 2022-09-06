package entity;

public class Status {
    private boolean curStatus;
    private String curEffect;
    private boolean isHaha;
    private boolean isTest;
    private boolean curVideoStatus;

    public Status() {}

    public Status(boolean curStatus, String curEffect, boolean isHaha, boolean isTest, boolean curVideoStatus) {
        this.curStatus = curStatus;
        this.curEffect = curEffect;
        this.isHaha = isHaha;
        this.isTest = isTest;
        this.curVideoStatus = curVideoStatus;
    }

    public boolean isCurVideoStatus() {
        return curVideoStatus;
    }

    public void setCurVideoStatus(boolean curVideoStatus) {
        this.curVideoStatus = curVideoStatus;
    }

    public boolean isCurStatus() {
        return curStatus;
    }

    public void setCurStatus(boolean curStatus) {
        this.curStatus = curStatus;
    }

    public String getCurEffect() {
        return curEffect;
    }

    public void setCurEffect(String curEffect) {
        this.curEffect = curEffect;
    }

    public boolean isHaha() {
        return isHaha;
    }

    public void setHaha(boolean haha) {
        isHaha = haha;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }
}
