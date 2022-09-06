package transformers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;

import java.awt.image.BufferedImage;

public class TransformerUtils {
    public void changeTransformer(Webcam w, Object e, boolean isTest, boolean isHaha) {
        WebcamImageTransformer ts = getTransformer((String)e, isTest, isHaha);
        w.setImageTransformer(ts);
    }

    private WebcamImageTransformer getTransformer(String type, boolean isTest, boolean isHaha) {
        return (BufferedImage bufferedImage) -> {
            long start = System.currentTimeMillis();
            BufferedImage bufferedImage1;
            if(type!=null && type.equals("横向外凸（并行）")) {
                bufferedImage1 = new taskFilter().filter(bufferedImage, null);
            } else {
                bufferedImage1 =  new MyEffective().filter(bufferedImage, type, isTest, isHaha);
            }
            long end = System.currentTimeMillis();
            System.out.println("当前特效【"+type+"】处理一张图片所需时间："+(end-start)+"ms");
            return bufferedImage1;
        };
    }
}
