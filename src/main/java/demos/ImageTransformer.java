package demos;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.jh.JHFilter;
import org.apache.xmlbeans.impl.piccolo.util.RecursionException;


/**
 * 并行实现纵向
 */
class taskFilter extends JHFilter {
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

class MyEffective extends JHFilter {
    /**
     * Performs a single-input/single-output operation on a
     * <CODE>BufferedImage</CODE>.
     * If the color models for the two images do not match, a color
     * conversion into the destination color model is performed.
     * If the destination image is null,
     * a <CODE>BufferedImage</CODE> with an appropriate <CODE>ColorModel</CODE>
     * is created.
     * <p>
     * An <CODE>IllegalArgumentException</CODE> may be thrown if the source
     * and/or destination image is incompatible with the types of images       $
     * allowed by the class implementing this filter.
     *
     * @param src  The <CODE>BufferedImage</CODE> to be filtered
     * @param dst The <CODE>BufferedImage</CODE> in which to store the results$
     * @return The filtered <CODE>BufferedImage</CODE>.
     * @throws IllegalArgumentException If the source and/or destination
     *                                  image is not compatible with the types of images allowed by the class
     *                                  implementing this filter.
     */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
//        dst = basicTransformer(src, dst,15.0d);

//        dst = basicTransformer2(src, dst, 0.02d);

//        dst = basicTransformer3(src, dst);

//        dst = basicTransformer4(src, dst);

//        dst = oldPhotoTransformer(src, dst);

//        dst = basicTransformer7(src, dst);

//        dst = basicEffect11(src,false);

        dst = basicEffect4(src);
        return dst;
    }

    /**
     * 阴间滤镜
     * @param src
     * @return
     */
    private BufferedImage basicEffect11(BufferedImage src, boolean isTest) {
        int height = src.getHeight();
        int width = src.getWidth();

        if(isTest) {

            int[][] ori = new int[width][height];

            for (int i = width / 4; i < width * 3 / 4; i++)
                for (int j = 0; j < height; j++)
                    ori[i][j] = src.getRGB(i, j);

            for (int i = width / 4; i < width * 3 / 4; i++)
                for (int j = 0; j < height; j++) {
                    int rgb = ori[i][j];
                    int left = i - width / 4;
                    src.setRGB(left, j, rgb);
                    Color c = new Color(rgb);
                    int red = c.getRed();
                    int green = c.getGreen();
                    int blue = c.getBlue();
                    red = (red - green - blue) * 3 / 2;
                    green = (green - red - blue) * 3 / 2;
                    blue = (blue - red - green) * 3 / 2;
                    if (red < 0 || red > 255) red = c.getRed();
                    if (green < 0 || green > 255) green = c.getGreen();
                    if (blue < 0 || blue > 255) blue = c.getBlue();
                    c = new Color(red, green, blue);

                    int right = i + width / 4;
                    src.setRGB(right, j, c.getRGB());
                }
        } else {
            for (int i = 0; i < width; i++)
                for (int j = 0; j < height; j++) {
                    int rgb = src.getRGB(i, j);
                    Color c = new Color(rgb);
                    int red = c.getRed();
                    int green = c.getGreen();
                    int blue = c.getBlue();
                    red = (red - green - blue) * 3 / 2;
                    green = (green - red - blue) * 3 / 2;
                    blue = (blue - red - green) * 3 / 2;
                    if (red < 0 || red > 255) red = c.getRed();
                    if (green < 0 || green > 255) green = c.getGreen();
                    if (blue < 0 || blue > 255) blue = c.getBlue();
                    c = new Color(red, green, blue);
                    src.setRGB(i, j, c.getRGB());
                }
        }
        return src;
    }


    /**
     * 阴间滤镜
     * @param src
     * @return
     */
    private BufferedImage basicEffect10(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(-rgb);
                src.setRGB(i,j, c.getRGB());
            }
        return src;
    }


    /**
     * 黑白处理
     * @param src
     * @return
     */
    private BufferedImage basicEffect9(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                int val = (red + green + blue) / 3;
                Color af;
                if(val>80) af = Color.WHITE;
                else af = Color.BLACK;
                src.setRGB(i,j, af.getRGB());
            }
        return src;
    }


    /**
     * 连环画
     * @param src
     * @return
     */
    private BufferedImage basicEffect8(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();

                red = Math.abs(green - blue + green + red) * red / 256;
                green = Math.abs(blue - green + blue + red) * red /256;
                blue = Math.abs(blue - green + blue + red) * green / 256;
                if(red < 0 || red > 255) red = c.getRed();
                if(green < 0 || green > 255) green = c.getGreen();
                if(blue < 0 || blue > 255) blue = c.getBlue();

                Color af = new Color(red,green,blue);
                src.setRGB(i,j, af.getRGB());
            }
        return src;
    }

    /**
     * 马赛克
     * @param src
     * @return
     */
    private BufferedImage basicEffect7(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i+=9)
            for (int j = 0; j < height; j+=9) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                Color af = new Color(red,green,blue);
                for(int k = i; k<=i+8 && k < width;k++)
                    for(int l=j;l<=j+8 && l < height;l++)
                        src.setRGB(k,l, af.getRGB());
            }
        return src;
    }

    /**
     * 红色检测?去色
     * @param src
     * @return
     */
    private BufferedImage basicEffect6(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int green = c.getGreen();
                if(green < 52) {
                    src.setRGB(i,j, rgb);
                } else {
                    src.setRGB(i,j, Color.WHITE.getRGB());
                }
            }
        return src;
    }


    /**
     * 红色检测
     * @param src
     * @return
     */
    private BufferedImage basicEffect5(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();

                red = red*128 / (green + blue + 1);
                green = green*128 / (red+blue+1);
                blue = blue*128 / (red + green + 1);

                if(red>255) red = c.getRed();
                if(green>255) green = c.getGreen();
                if(blue>255) blue = c.getBlue();
                Color af = new Color(red,green,blue);
                src.setRGB(i,j, af.getRGB());
            }
        return src;
    }

    /**
     * 劣质油画
     * @param src
     * @return
     */
    private BufferedImage basicEffect4(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        Random rand = new Random();
        int boxSize = rand.nextInt(3) + 1;
        for (int i = 0; i < width; i+=boxSize+1)
            for (int j = 0; j < height; j+=boxSize+1) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                Color af = new Color(red,green,blue);
                rand = new Random();
                boxSize = rand.nextInt(3) + 1;
                for(int k = i; k<=i+boxSize && k<width;k++)
                    for(int l=j;l<=j+boxSize && l<height;l++)
                        src.setRGB(k,l, af.getRGB());
            }
        return src;
    }


    /**
     * 珠纹化
     * @param src
     * @return
     */
    private BufferedImage basicEffect3(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i+=5)
            for (int j = 0; j < height; j+=5) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                Color af = new Color(red,green,blue);
                for(int k = i; k<=i+4;k++)
                    for(int l=j;l<=j+4;l++)
                        src.setRGB(k,l, af.getRGB());
            }
        return src;
    }

    /**
     * 灰度化2
     * @param src
     * @return
     */
    private BufferedImage basicEffect2(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                int cc = (Math.max(red, Math.max(green, blue)) +
                        Math.min(red, Math.min(green, blue))) / 2;
                src.setRGB(i, j, new Color(cc,cc,cc).getRGB());
            }
        return src;
    }

    /**
     * 灰度化
     * @param src
     * @return
     */
    private BufferedImage basicEffect1(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int rgb = src.getRGB(i,j);
                Color c = new Color(rgb);
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                int cc = (red + green + blue) / 3;
                src.setRGB(i, j, new Color(cc,cc,cc).getRGB());
            }
        return src;
    }

    /**
     * 效果不怎么好的高斯滤波
     * @param src
     * @return
     */
    private BufferedImage gaussianBlur(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        int[][] martrix = new int[3][3];
        int[] values = new int[9];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                readPixel(src, i, j, values);
                fillMatrix(martrix, values);
                src.setRGB(i, j, avgMatrix(martrix));
            }
        return src;
    }

    private static void readPixel(BufferedImage img, int x, int y, int[] pixels) {
        int xStart = x - 1;
        int yStart = y - 1;
        int current = 0;
        for (int i = xStart; i < 3 + xStart; i++)
            for (int j = yStart; j < 3 + yStart; j++) {
                int tx = i;
                if (tx < 0) {
                    tx = -tx;
                } else if (tx >= img.getWidth()) {
                    tx = x;
                }
                int ty = j;
                if (ty < 0) {
                    ty = -ty;
                } else if (ty >= img.getHeight()) {
                    ty = y;
                }
                pixels[current++] = img.getRGB(tx, ty);
            }
    }

    private static void fillMatrix(int[][] matrix, int[] values) {
        int filled = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                x[j] = values[filled++];
            }
        }
    }

    private static int avgMatrix(int[][] matrix) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                if (j == 1) {
                    continue;
                }
                Color c = new Color(x[j]);
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }
        return new Color(r / 8, g / 8, b / 8).getRGB();
    }

    /**
     * 哈哈镜特效：老照片
     * @param src
     * @param dest
     * @return
     */
    private BufferedImage basicTransformer6(BufferedImage src, BufferedImage dest){
        int width = src.getWidth();
        int height = src.getHeight();

        if(dest == null){
            dest = createCompatibleDestImage(src,null);
        }

        int[] inpixels = new int[width*height];
        int[] outpixels = new int[width*height];
        getRGB(src,0,0,width,height,inpixels);
        int index = 0;
        for(int row=0;row<height;row++){
            int ta=0, tr=0, tg=0, tb=0;
            for(int col=0;col<width;col++){
                index = row*width+col;
                ta = (inpixels[index] >> 24) & 0xff;
                tr = (inpixels[index] >> 16) & 0xff;
                tg = (inpixels[index] >> 8) & 0xff;
                tb = (inpixels[index]) & 0xff;

                int fr = (int) colorBlend(noise(),(tr*0.393)+(tg*0.769)+(tb*0.189),tr);
                int fg = (int) colorBlend(noise(),(tr*0.349)+(tg*0.686)+(tb*0.189),tg);
                int fb = (int) colorBlend(noise(),(tr*0.272)+(tg*0.534)+(tb*0.131),tb);

                outpixels[index] = (ta<<24) | clamp(fr) << 16 | clamp(fg) << 8 | clamp(fb);
            }
        }
        setRGB(dest,0,0,width,height,outpixels);
        return dest;
    }

    //设置随机权重系数
    private double noise(){
        return Math.random()*0.5+0.5;
    }

    //计算新的像素值=新的像素值+原来像素值-原来像素值*权重系数
    private double colorBlend(double scale, double dest, double src){
        return (scale*dest+(1-scale)*src);
    }

    private int clamp(int c){
        return c>255 ? 255:((c<0) ? 0:c);
    }

    /**
     * 哈哈镜特效：复合哈哈镜
     * @param src
     * @param dest
     * @return
     */
    private BufferedImage basicTransformer5(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0, outIndex = 0;
        int centerX = width/2;
        int centerY = height/2;
        double theta, distance, radius = Math.max(centerX, centerY);
        double newX, newY;
        int offsetX = 0, offsetY = 0;
        for(int row=0; row<height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for(int col=0; col<width; col++) {

                int trueX = col - centerX;
                int trueY = row - centerY;
                distance = Math.sqrt(trueX*trueX + trueY*trueY);
                // 核心代码：the top trick is to add (degree * radius), generate the swirl effect...
                if(distance < radius) {
                    newX = centerX + (int) ((col-centerX)*distance/radius);
                    newY = centerY + (int) ((row-centerY)*distance/radius);
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

        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
    }


    /**
     * 哈哈镜特效：纵向拉伸
     * @param src
     * @param dest
     * @return
     */
    private BufferedImage basicTransformer4(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0, outIndex = 0;
        int centerX = width/2;
        int centerY = height/2;
        double theta, distance, radius = Math.max(centerX, centerY);
        double newX, newY;
        int offsetX = 0, offsetY = 0;
        for(int row=0; row<height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for(int col=0; col<width; col++) {

                int trueX = col - centerX;
                int trueY = row - centerY;
                distance = Math.sqrt(trueX*trueX + trueY*trueY);
                // 核心代码：the top trick is to add (degree * radius), generate the swirl effect...
                if(distance < radius) {
                    newX = col;
                    newY = centerY + (int) ((row-centerY)*distance/radius);
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

        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
    }

    /**
     * 哈哈镜特效：中间凸
     * @param src
     * @param dest
     * @return
     */
    private BufferedImage basicTransformer3(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0, outIndex = 0;
        int centerX = width/2;
        int centerY = height/2;
        double theta, distance, radius = Math.max(centerX, centerY);
        double newX, newY;
        int offsetX = 0, offsetY = 0;
        for(int row=0; row<height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for(int col=0; col<width; col++) {

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

        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
    }

    /**
     * 哈哈镜特效，中心螺旋
     * @param src
     * @param dest
     * @param degree
     * @return
     */
    private BufferedImage basicTransformer2(BufferedImage src, BufferedImage dest, double degree) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0, outIndex = 0;
        int centerX = width/2;
        int centerY = height/2;
        double theta, radius;
        double newX, newY;
        int offsetX = 0, offsetY = 0;
        for(int row=0; row<height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for(int col=0; col<width; col++) {

                int trueX = col - centerX;
                int trueY = row - centerY;
                theta = Math.atan2((trueY),(trueX));
                radius = Math.sqrt(trueX*trueX + trueY*trueY);

                // 核心代码：the top trick is to add (degree * radius), generate the swirl effect...
                newX = centerX + (radius * Math.cos(theta + degree * radius));
                newY = centerY + (radius * Math.sin(theta + degree * radius));

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

        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
    }


    /**
     * 哈哈镜特效：内嗦
     * @param src
     * @param dest
     * @param factor
     * @return
     */
    private BufferedImage basicTransformer1(BufferedImage src, BufferedImage dest, double factor) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0, outIndex = 0;
        int centerX = width/2;
        int centerY = height/2;
        double theta, radius;
        double newX, newY;
        int offsetX = 0, offsetY = 0;
        for(int row=0; row<height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for(int col=0; col<width; col++) {

                int trueX = col - centerX;
                int trueY = row - centerY;
                theta = Math.atan2((trueY),(trueX));
                radius = Math.sqrt(trueX*trueX + trueY*trueY);
                double newRadius = Math.sqrt(radius) * factor;
                newX = centerX + (newRadius * Math.cos(theta));
                newY = centerY + (newRadius * Math.sin(theta));

                if (newX > 0 && newX < width) {
                    offsetX = (int)newX;
                } else {
                    newX = 0;
                }

                if (newY > 0 && newY < height) {
                    offsetY = (int)newY;
                } else {
                    newY = 0;
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

        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
    }

    /**
     * 哈哈镜特效：左右翻转
     * @param src
     * @param dest
     * @return
     */
    private BufferedImage foldTransformer2(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        for(int i = 0; i < width / 2; i++) {
            for(int j = 0; j< height ; j++) {
                int temp = src.getRGB(width - i - 1, j);
                src.setRGB(width - i - 1, j, src.getRGB(i, j));
                src.setRGB(i, j, temp);
            }
        }
        return src;
    }

    /**
     * 哈哈镜特效：上下翻转
     * @param src
     * @param dest
     * @return
     */
    private BufferedImage foldTransformer1(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        for(int i = 0; i < width; i++) {
            for(int j = 0; j< height / 2 ; j++) {
                int temp = src.getRGB(i, height - j - 1);
                src.setRGB(i, height - j - 1, src.getRGB(i, j));
                src.setRGB(i, j, temp);
            }
        }
        return src;
    }

    /**
     * 视频模糊化
     * @param src
     * @param dst
     * @return
     */
    private BufferedImage blurTransformer(BufferedImage src, BufferedImage dst){
        if(src.getType() != BufferedImage.TYPE_INT_RGB){
            src = convertType(src,BufferedImage.TYPE_INT_RGB);
        }

        float ninth = 1.0f/9.0f;
        //卷积核操作
        float[] blurKernal = {
                ninth,ninth,ninth,
                ninth,ninth,ninth,
                ninth,ninth,ninth
        };
        // 卷积
        BufferedImageOp blurFilter = new ConvolveOp(new Kernel(3,3,blurKernal));
        return blurFilter.filter(src,dst);

    }

    private BufferedImage convertType(BufferedImage src,int type){
        ColorConvertOp cco = new ColorConvertOp(null);
        BufferedImage dest = new BufferedImage(src.getWidth(),src.getHeight(),type);
        return cco.filter(src,dest);

    }

    /**
     * 灰度化
     * @param src
     * @param dst
     */
    private BufferedImage blackTransformer(BufferedImage src, BufferedImage dst) {
        src = grayTransformer(src, dst);//图像灰度化
        byte[] threshold = new byte[256];
        for (int i = 0; i<256;i++){
            threshold[i] = (byte) ((i<128)? 0:255);

        }
        BufferedImageOp thresholdOp = new LookupOp(new ByteLookupTable(0,threshold),null);

        return thresholdOp.filter(src,dst);
//      //源码
//        int width = src.getWidth();
//        int height = src.getHeight();
//        int type = src.getType();
//        WritableRaster srcRaster = src.getRaster();
//        if (dst == null) {
//            dst = this.createCompatibleDestImage(src, (ColorModel)null);
//        }
//
//        WritableRaster dstRaster = dst.getRaster();
//        int[] inPixels = new int[width];
//
//        for(int y = 0; y < height; ++y) {
//            int x;
//            if (type == 2) {
//                srcRaster.getDataElements(0, y, width, 1, inPixels);
//
//                for(x = 0; x < width; ++x) {
//                    inPixels[x] = this.filterRGB(inPixels[x]);
//                }
//
//                dstRaster.setDataElements(0, y, width, 1, inPixels);
//            } else {
//                src.getRGB(0, y, width, 1, inPixels, 0, width);
//
//                for(x = 0; x < width; ++x) {
//                    inPixels[x] = this.filterRGB(inPixels[x]);
//                }
//
//                dst.setRGB(0, y, width, 1, inPixels, 0, width);
//            }
//        }
//        return dst;
    }

    /**
     * 黑白化
     * @param src
     * @param dst
     */
    private BufferedImage grayTransformer(BufferedImage src, BufferedImage dst) {
        ColorConvertOp filterObj = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),null
        );

        return filterObj.filter(src,dst);
    }
}

public class ImageTransformer {
    public static void main(String[] args) {

        String name = "";
        try {
            List<Webcam> webcams = Webcam.getDiscoveryService().getWebcams(10, TimeUnit.SECONDS);
            name = webcams.get(1).getName();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        Webcam webcam = Webcam.getWebcamByName(name);
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        //设置特效类
        WebcamImageTransformer transformer = new WebcamImageTransformer() {
            public BufferedImage transform(BufferedImage bufferedImage) {
                return new MyEffective().filter(bufferedImage, null);
//                return new taskFilter().filter(bufferedImage, null);
            }
        };

        webcam.setImageTransformer(transformer);

        webcam.open();

        JFrame window = new JFrame("Test Transformer");

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setFillArea(true);

        window.add(panel);
        window.pack();
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}