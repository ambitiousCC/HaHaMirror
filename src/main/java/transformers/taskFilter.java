package transformers;

import com.github.sarxos.webcam.util.jh.JHFilter;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class taskFilter extends JHFilter {
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        return taskTransformer(src, dest);
    }

    private BufferedImage taskTransformer(BufferedImage src, BufferedImage dest) {
        BufferedImage[] input = {src, dest};
        int[] outPixels = new int[src.getWidth()*src.getHeight()];
        ForkJoinTask task = new taskEffect(input , outPixels, 0, src.getHeight()-1);
        ForkJoinPool pool = new ForkJoinPool(5);
        pool.submit(task);
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return input[1];  // 对象是地址引用， 所以直接返回就可以了
    }
}

class taskEffect extends RecursiveAction {
    private static final Integer MAX = 50;

    final int width;
    final int height;

    private BufferedImage src;
    private BufferedImage dest;
    private int start = 0, end = 0;
    private int[] inPixels;
    private int[] outPixels;
    private int index;
    private int outIndex;
    private int centerX;
    private int centerY;
    private double theta;
    private double distance;
    private double radius;
    private double newX;
    private double newY;
    private int offsetX;
    private int offsetY;

    public taskEffect(BufferedImage[] input, int[] outPixels, int start, int end) {
        super();
        this.src = input[0];
        this.width = src.getWidth();
        this.height = src.getHeight();
        this.start = start;
        this.end = end;

        if (input[1] == null)
            input[1] = new taskFilter().createCompatibleDestImage( src, null );
        this.dest = input[1];

        inPixels = new int[width*height];
        this.outPixels = outPixels;
        new taskFilter().getRGB( src, 0, 0, width, height, inPixels );
        index = 0;
        outIndex = 0;
        centerX = width/2;
        centerY = height/2;
        radius = Math.max(centerX, centerY);
        offsetX = 0;
        offsetY = 0;
    }

    /**
     * The main computation performed by this task.
     */
    @Override
    protected void compute() {
        if (end - start < MAX) {
            for(int step = this.start; step <= this.end; step++) {
                transform(step);
            }
            new taskFilter().setRGB( dest, 0, 0, src.getWidth(), src.getHeight(), outPixels);
        } else {
            BufferedImage[] input = {src, dest};
            invokeAll(new taskEffect(input, outPixels,  start, (start+end)/2),
                    new taskEffect(input, outPixels,  (start+end)/2+1, end));
        }
    }

    /**
     * 特效处理方法
     */
    private void transform(int row) {
        int ta = 0, tr = 0, tg = 0, tb = 0;
        for(int col=0;col<width;col++) {
            int trueX = col - centerX;
            int trueY = row - centerY;
            distance = Math.sqrt(trueX*trueX + trueY*trueY);
            // 核心代码：the top trick is to add (degree * radius), generate the swirl effect...
            if(distance < radius) {
                newX = centerX + (int) ((col-centerX)*distance/radius);
                newY = row;
            } else {
                newX = col;
                newY = row;
            }

            // 重复代码：更新像素点
            if (newX > 0 && newX < width) {
                offsetX = (int)newX;
            } else {
                offsetX = col;
            }

            if (newY > 0 && newY < height) {
                offsetY = (int)newY;
            } else {
                offsetY = row;
            }
            index = offsetY * width + offsetX;
            ta = (inPixels[index] >> 24) & 0xff;
            tr = (inPixels[index] >> 16) & 0xff;
            tg = (inPixels[index] >> 8) & 0xff;
            tb = inPixels[index] & 0xff;

            // use newX, newY and fill the pixel data now...
            outIndex = row * width + col;
            outPixels[outIndex] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
        }
    }
}
