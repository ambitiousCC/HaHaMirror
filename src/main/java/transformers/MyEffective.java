package transformers;

import com.github.sarxos.webcam.util.jh.JHFilter;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.Random;

public class MyEffective extends JHFilter {
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
     * @param dest The <CODE>BufferedImage</CODE> in which to store the results$
     * @return The filtered <CODE>BufferedImage</CODE>.
     * @throws IllegalArgumentException If the source and/or destination
     *                                  image is not compatible with the types of images allowed by the class
     *                                  implementing this filter.
     */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        return null;
    }

    public BufferedImage filter(BufferedImage src, String type, boolean isTest, boolean isHaha) {
        if(type==null) return src;
        if(isHaha) return basicTransformers(src,type,isTest);
        else return basicEffects(src,type, isTest);
    }

    private BufferedImage basicTransformers(BufferedImage src, String type, boolean isTest) {
        switch (type) {
            case "中心内凹":
                return basicTransformer1(src, null, 10.0d);
            case "中心螺旋":
                double min = 0.01d;
                double max = 0.1d;
                double degree = min + new Random().nextDouble() * (max - min);
                return basicTransformer2(src, null, (int)Math.ceil(degree*100)/100.0);
            case "横向外凸":
                return basicTransformer3(src, null);
            case "纵向外凸":
                return basicTransformer4(src, null);
            case "中心外凹":
                return basicTransformer5(src, null);
            case "复合特效":
                return combineTransformer(src, null);
            default:
                System.out.println("error");
                break;
        }
        return src;
    }

    private BufferedImage basicEffects(BufferedImage src, String type, boolean isTest) {
        int height = src.getHeight();
        int width = src.getWidth();
        int[][] ori = new int[width][height];
        int area = 1;
        boolean flag = false;
        boolean location = false; int t = 0;
        switch (type) {
            case "马赛克":
                area = 9;
                break;
            case "珠纹化":
                area = 5;
                break;
            case "油画":
                if (isTest) {
                    for (int i = 0; i < width; i++)
                        for (int j = 0; j < height; j++)
                            ori[i][j] = src.getRGB(i, j);
                    for (int i = width / 4; i < width * 3 / 4; i++)
                        for (int j = 0; j < height; j++) {
                            int left = i - width / 4;
                            src.setRGB(left, j, ori[i][j]);
                        }
                    for (int i = width / 4; i < width * 3 / 4; i++)
                        for (int j = 0; j < height; j++) {
                            int right = i + width / 4;
                            src.setRGB(right, j, ori[i][j]);
                        }
                    Random rand = new Random();
                    int testArea = 10;
                    int boxSize = rand.nextInt(testArea) + 1;
                    for (int i = width / 4; i < width * 3 / 4; i+=boxSize+1)
                        for (int j = 0; j < height; j+=boxSize+1) {
                            int rgb = ori[i][j];
                            rand = new Random();
                            boxSize = rand.nextInt(testArea) + 1;
                            for (int k = i; k <= i + boxSize && k+width/4 < width; k++)
                                for (int l = j; l <= j + boxSize && l < height; l++)
                                    src.setRGB(k+width/4, l, rgb);
                        }
                    return src;
                } else return basicEffect8(src);
            default:
                break;
        }

        if(type.equals("马赛克") || type.equals("珠纹化")) {
            flag = true;
        }
        if(type.equals("垂直翻转") || type.equals("水平翻转")) {
            location = true;
            if(type.equals("水平翻转")) t = 1;
        }

        if(isTest) {
            for (int i = width / 4; i < width * 3 / 4; i++)
                for (int j = 0; j < height; j++)
                    ori[i][j] = src.getRGB(i, j);
            if(flag) {
                for (int i = width / 4; i < width * 3 / 4; i++)
                    for (int j = 0; j < height; j++) {
                        int left = i - width / 4;
                        src.setRGB(left, j, ori[i][j]);
                    }
                for (int i = width / 4; i < width * 3 / 4; i+=area)
                    for (int j = 0; j < height; j+=area) {
                        int c = chooseEffects(ori, i, j, type);
                        int right = i + width / 4;
                        saveToSrc(src, c, flag, right, j, area, width, height);
                    }
            } else if (location) {
                for (int i = width / 4; i < width * 3 / 4; i++)
                    for (int j = 0; j < height; j++) {
                        int left = i - width / 4;
                        src.setRGB(left, j, ori[i][j]);
                    }
                if(t==0) {
                    //上下翻转
                    for(int i = width / 4; i < width * 3 / 4; i++) {
                        for(int j = 0; j< height / 2 ; j++) {
                            int right = i + width / 4;
                            src.setRGB(right, height - j - 1, ori[i][j]);
                            src.setRGB(right, j, ori[i][height - j - 1]);
                        }
                    }
                    return src;
                } else {
                    //左右翻转
                    for(int i = width / 4; i < width / 2; i++) {
                        for(int j = 0; j < height ; j++) {
                            src.setRGB(i + width/4, j, ori[width - i - 1][j]);
                            src.setRGB(width*5/4 - i - 1, j, ori[i][j]);
                        }
                    }
                    return src;

                }
            } else {
                for (int i = width / 4; i < width * 3 / 4; i += area)
                    for (int j = 0; j < height; j += area) {
                        int left = i - width / 4;
                        src.setRGB(left, j, ori[i][j]);
                        //获取方法
                        int c = chooseEffects(ori, i, j, type);

                        int right = i + width / 4;
                        saveToSrc(src, c, flag, right, j, area, width, height);
                    }
            }
        } else {
            for (int i = 0; i < width; i+=area)
                for (int j = 0; j < height; j+=area)
                    ori[i][j] = src.getRGB(i,j);

            if (location) {
                if(t==0) {
                    //上下翻转
                    for(int i = 0; i < width; i++) {
                        for(int j = 0; j< height / 2 ; j++) {
                            int temp = src.getRGB(i, height - j - 1);
                            src.setRGB(i, height - j - 1, src.getRGB(i, j));
                            src.setRGB(i, j, temp);
                        }
                    }
                    return src;
                } else {
                    //左右翻转
                    for(int i = 0; i < width / 2; i++) {
                        for(int j = 0; j< height ; j++) {
                            int temp = src.getRGB(width - i - 1, j);
                            src.setRGB(width - i - 1, j, src.getRGB(i, j));
                            src.setRGB(i, j, temp);
                        }
                    }
                    return src;

                }
            } else {
                for (int i = 0; i < width; i += area)
                    for (int j = 0; j < height; j += area) {
                        //获取方法
                        int c = chooseEffects(ori, i, j, type);
                        saveToSrc(src, c, flag, i, j, area, width, height);
                    }
            }
        }
        return src;
    }

    private void saveToSrc(BufferedImage src, int c, boolean flag, int i, int j, int area, int width, int height) {
        if(flag) {
            for(int k = i; k<=i+area && k < width;k++)
                for(int l=j;l<=j+area && l < height;l++)
                    src.setRGB(k,l,c);
        } else {
            src.setRGB(i, j, c);
        }
    }

    /**
     * 选择对应的滤镜方法,注意滤镜返回改变的rgb值
     * @param ori
     * @param i
     * @param j
     * @param type
     * @return
     */
    private int chooseEffects(int[][] ori, int i, int j, String type) {
        int c = 0;
        switch (type) {
            case "阴间滤镜":
                c = basicEffect1(ori,i,j);
                break;
            case "鬼影滤镜":
                c = basicEffect2(ori,i,j);
                break;
            case "黑白相机":
                c = basicEffect3(ori, i, j);
                break;
            case "连环画":
                c = basicEffect4(ori, i, j);
                break;
            case "马赛克":
                c = basicEffect5(ori, i, j);
                break;
            case "红色物体检测":
                c = basicEffect6(ori, i, j);
                break;
            case "熔炉滤镜":
                c = basicEffect7(ori, i, j);
                break;
            case "珠纹化":
                c = basicEffect9(ori, i, j);
                break;
            case "灰度2":
                c = basicEffect10(ori, i, j);
                break;
            case "灰度1":
                c = basicEffect11(ori, i, j);
                break;
            case "高斯模糊":
                c = basicEffect12(ori, i, j);
                break;
            case "怀旧照片":
                c = basicEffect13(ori, i, j);
                break;
            case "美白":
                c = skinDetection(ori,i,j);
                break;
            default:
                System.out.println("error");
                return ori[i][j];
        }
        return c;
    }

    private int skinDetection(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        if(isSkin(red,green,blue)) {
            int add = 40;
            int newR = red+add>255?255:red+add;
            int newG = green+add>255?255:green+add;
            int newB = blue+add>255?255:blue+add;
            return new Color(newR,newG,newB).getRGB();
        }
        return rgb;
    }

    private boolean isSkin(int r, int g, int b) {
        if(r<=95) return false;
        if(g<=40) return false;
        if(b<=20) return false;
        if(r<=g) return false;
        if(r<=b) return false;
        if(Math.max(r,Math.max(g,b))-Math.min(Math.min(r,g),b)<=15) return false;
        if(Math.abs(r-g)<=15) return false;
        return true;
    }

    /**
     * 阴间滤镜
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect1(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
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
        return c.getRGB();
    }

    /**
     * 鬼影滤镜
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect2(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(-rgb);
        return c.getRGB();
    }

    /**
     * 黑白相机
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect3(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        int val = (red + green + blue) / 3;
        Color af;
        if(val>80) af = Color.WHITE;
        else af = Color.BLACK;
        return af.getRGB();
    }

    /**
     * 连环画滤镜
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect4(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
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
        return af.getRGB();
    }

    /**
     * 马赛克
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect5(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        Color af = new Color(red, green, blue);
        return af.getRGB();
    }

    /**
     * 红色物体检测
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect6(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int green = c.getGreen();
        if (green < 52) {
            return rgb;
        } else {
            return Color.BLACK.getRGB();
        }
    }

    /**
     * 熔炉滤镜
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect7(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();

        red = red * 128 / (green + blue + 1);
        green = green * 128 / (red + blue + 1);
        blue = blue * 128 / (red + green + 1);

        if (red > 255) red = c.getRed();
        if (green > 255) green = c.getGreen();
        if (blue > 255) blue = c.getBlue();
        Color af = new Color(red, green, blue);
        return af.getRGB();
    }

    /**
     * 劣质油画
     * @param src
     * @return
     */
    private BufferedImage basicEffect8(BufferedImage src) {
        int height = src.getHeight();
        int width = src.getWidth();
        Random rand = new Random();
        int boxSize = rand.nextInt(10) + 1;
        for (int i = 0; i < width; i+=boxSize+1)
            for (int j = 0; j < height; j+=boxSize+1) {
                int rgb = src.getRGB(i,j);
                rand = new Random();
                boxSize = rand.nextInt(10) + 1;
                for(int k = i; k<=i+boxSize && k<width;k++)
                    for(int l=j;l<=j+boxSize && l<height;l++)
                        src.setRGB(k,l, rgb);
            }
        return src;
    }

    /**
     * 珠纹化
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect9(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        Color af = new Color(red, green, blue);
        return af.getRGB();
    }

    /**
     * 灰度2
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect10(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        int cc = (Math.max(red, Math.max(green, blue)) +
                Math.min(red, Math.min(green, blue))) / 2;
        return new Color(cc, cc, cc).getRGB();
    }

    /**
     * 灰度1
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect11(int[][] ori, int i, int j) {
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        int cc = (red + green + blue) / 3;
        return new Color(cc, cc, cc).getRGB();
    }

    /**
     * 高斯模糊
     *
     */
    private int[][] martrix = new int[3][3];
    private int[] values = new int[9];
    private int basicEffect12(int[][] ori, int i, int j) {
        readPixel(ori, i, j, values);
        fillMatrix(martrix, values);
        return avgMatrix(martrix);
    }
    private static void readPixel(int[][] ori, int x, int y, int[] pixels) {
        int xStart = x - 1;
        int yStart = y - 1;
        int current = 0;
        for (int i = xStart; i < 3 + xStart; i++)
            for (int j = yStart; j < 3 + yStart; j++) {
                int tx = i;
                if (tx < 0) {
                    tx = -tx;
                } else if (tx >= ori.length) {
                    tx = x;
                }
                int ty = j;
                if (ty < 0) {
                    ty = -ty;
                } else if (ty >= ori[0].length) {
                    ty = y;
                }
                pixels[current++] = ori[tx][ty];
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
     * 老照片
     * @param ori
     * @param i
     * @param j
     * @return
     */
    private int basicEffect13(int[][] ori, int i, int j){
        int rgb = ori[i][j];
        Color c = new Color(rgb);
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        red = (int)(0.393*red + 0.769*green + 0.189*blue);
        green = (int)(0.349*red + 0.686*green + 0.189*blue);
        blue = (int)(0.272*red + 0.534*green + 0.131*blue);
        if(red > 255) red = c.getRed();
        if(green > 255) green = c.getGreen();
        if(blue > 255) blue = c.getBlue();
        return new Color(red, green, blue).getRGB();
    }

    /**
     * 复合哈哈镜
     * @param src
     * @param dest
     * @return
     */
    private BufferedImage combineTransformer(BufferedImage src, BufferedImage dest) {
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
                if(row<=height/2) {
                    // 核心代码：the top trick is to add (degree * radius), generate the swirl effect...
                    if(distance < radius) {
                        newX = col;
                        newY = centerY + (int) ((row-centerY)*distance/radius);
                    } else {
                        newX = col;
                        newY = row;
                    }
                } else {
                    if(distance < radius) {
                        newX = centerX + (int) ((col-centerX)*distance/radius);
                        newY = row;
                    } else {
                        newX = col;
                        newY = row;
                    }
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
     * 哈哈镜特效：横向外凸
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

}