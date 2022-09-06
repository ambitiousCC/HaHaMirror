package demos;

import java.util.Random;

public class Simple {
    public static void main(String[] args) throws InterruptedException{
        for (int i = 0; i < 100; i++) {double min = 0.01d;
            double max = 0.05d;
            double degree = min + new Random().nextDouble() * (max - min);
            System.out.println(degree);
            System.out.println((int)Math.ceil(degree*100)/100.0);
            Thread.sleep(1000);
        }
    }
}
