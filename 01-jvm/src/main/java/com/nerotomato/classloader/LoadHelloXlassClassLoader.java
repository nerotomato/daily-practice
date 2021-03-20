package com.nerotomato.classloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Created by nero on 2021/3/19.
 */
public class LoadHelloXlassClassLoader extends ClassLoader {
    public static void main(String[] args) {
        try {
            Object hello = new LoadHelloXlassClassLoader().findClass("Hello").newInstance();
            Method helloMethod = hello.getClass().getDeclaredMethod("hello");
            helloMethod.invoke(hello);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        /*String base64Code = "NQFFQf///8v/4/X/+f/x9v/w/+/3/+71/+3/7Pj/6/j/6v7/+cOWkZaLwf7//NfWqf7/+7yQm5r+" +
                "//CzlpGasYqSnZqNq56dk5r+//qXmpOTkP7/9ayQio2cmrmWk5r+//W3mpOTkNGVnome8//4//f4" +
                "/+nz/+j/5/7/7Leak5OQ09+ck56MjLOQnpuajd74/+bz/+X/5P7/+reak5OQ/v/vlZ6JntCTnpGY" +
                "0LCdlZqci/7/75WeiZ7Qk56RmNCshoyLmpL+//yQiov+/+qzlZ6JntCWkNCvjZaRi6yLjZqeksT+" +
                "/+yVnome0JaQ0K+NlpGLrIuNmp6S/v/4j42WkYuTkf7/6tezlZ6JntCTnpGY0KyLjZaRmMTWqf/e" +
                "//r/+f///////f/+//j/9//+//b////i//7//v////rVSP/+Tv////7/9f////n//v////7//v/0" +
                "//f//v/2////2v/9//7////2Tf/97fxJ//tO/////v/1////9f/9////+//3//r//v/z/////f/y";*/
        String base64Code = encodeToBase64("E:\\Java课程\\Geek\\week01\\作业相关\\Hello\\Hello.xlass");
        //byte[] bytes = decodeBase64(base64Code);
        byte[] bytes = getBytes("E:\\Java课程\\Geek\\week01\\作业相关\\Hello\\Hello.xlass");
        byte[] newBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            newBytes[i] = (byte) (255 - bytes[i]);
        }
        for (byte b : newBytes
        ) {
            System.out.println(b);
        }
        return defineClass(name, newBytes, 0, newBytes.length);
    }

    /**
     * 将base64编码的字符串解码成byte字节数组
     * */
    public byte[] decodeBase64(String code) {
        return Base64.getDecoder().decode(code);
    }

    /**
     * 对xlass文件进行base64编码
     * */
    public String encodeToBase64(String filepath) {
        File f = new File(filepath);
        Path path = Paths.get(f.getAbsolutePath());
        byte[] data = null;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String base64Code = null;
        if (data != null && data.length > 0) {
            base64Code = Base64.getEncoder().encodeToString(data);
        }
        return base64Code;
    }

    public byte[] getBytes(String filepath) {
        File f = new File(filepath);
        Path path = Paths.get(f.getAbsolutePath());
        byte[] data = null;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
