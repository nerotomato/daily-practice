package com.nerotomato.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * 开启一个新线程，实现Callable接口，获取该线程的返回值
 * Created by nero on 2021/4/11.
 */
public class GetResultDemo1 {
    /**
     * 在main函数启动一个新线程，运行一个方法，拿到这
     * 个方法的返回值后，退出主线程
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        int result = 0;
        MyCallableTask task = new MyCallableTask();
        try {
            //这是得到的返回值
            result = (int) task.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 确保  拿到result 并输出
        System.out.println("异步计算结果为：" + result);
        System.out.println("使用时间：" + (System.currentTimeMillis() - start) + " ms");
        // 然后退出main线程
    }
}

class MyCallableTask implements Callable {

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if (a < 2)
            return 1;
        return fibo(a - 1) + fibo(a - 2);
    }

    @Override
    public Object call() throws Exception {
        int result = sum();
        return result;
    }
}