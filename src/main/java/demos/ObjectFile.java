package demos;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * JAVA对象持久化
 *
 * @author jianggujin
 *
 */
public class ObjectFile
{
    /**
     * 持久化为XML对象
     *
     * @param obj
     * @param out
     */
    public void storeXML(Object obj, OutputStream out)
    {
        XMLEncoder encoder = new XMLEncoder(out);
        encoder.writeObject(obj);
        encoder.flush();
        encoder.close();
    }

    /**
     * 从XML中加载对象
     *
     * @param in
     * @return
     */
    public Object loadXML(InputStream in)
    {
        XMLDecoder decoder = new XMLDecoder(in);
        Object obj = decoder.readObject();
        decoder.close();
        return obj;
    }

    /**
     * 持久化对象
     *
     * @param obj
     * @param out
     * @throws IOException
     */
    public void store(Object obj, OutputStream out) throws IOException
    {
        ObjectOutputStream outputStream = new ObjectOutputStream(out);
        outputStream.writeObject(obj);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 加载对象
     *
     * @param in
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object load(InputStream in) throws IOException,
            ClassNotFoundException
    {
        ObjectInputStream inputStream = new ObjectInputStream(in);
        Object obj = inputStream.readObject();
        inputStream.close();
        return obj;
    }

    public static void main(String[] args) throws Exception
    {
        String storeName = "java object";

        //xml文件形式存储
        File xmlFile = new File("xmlFile.dat");
        ObjectFile serializable = new ObjectFile();
        serializable.storeXML(storeName, new FileOutputStream(xmlFile));
        System.out.println(serializable.loadXML(new FileInputStream(xmlFile)));

        //文件形式存储
        File file = new File("file.dat");
        serializable.store(storeName, new FileOutputStream(file));
        System.out.println(serializable.load(new FileInputStream(file)));
    }
}