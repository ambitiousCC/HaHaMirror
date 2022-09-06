package webcapTest;

public class timeTest {
    public static void main(String[] args) throws InterruptedException{
        long start = System.currentTimeMillis();
        for(int i=0;i<100000;i++) {
            long pass = (System.currentTimeMillis() - start)/1000;
            //获取小时数
            long hour = pass / 3600;
            //获取分钟数
            long min = pass / 60 - hour * 60;
            //获取秒数
            long sec = pass - 3600 * hour - 60 * min;

            String hours, mins, secs;
            if(sec/10<1) secs = "0" + String.valueOf(sec);
            else secs = String.valueOf(sec);
            if(min/10<1) mins = "0" + String.valueOf(min);
            else mins = String.valueOf(min);
            if(hour/10<1) hours = "0" + String.valueOf(hour);
            else hours = String.valueOf(hour);

            System.out.println(hours+":"+mins+":"+secs);
            Thread.sleep(1000);
        }
    }
}
