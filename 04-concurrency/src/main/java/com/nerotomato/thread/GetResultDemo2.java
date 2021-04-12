package com.nerotomato.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 使用线程池executorService.submit方法
 * 获取future.get()的值
 * Created by nero on 2021/4/11.
 */
public class GetResultDemo2 {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        long start = System.currentTimeMillis();

        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        int result = 0;
        Future<?> future = executorService.submit(() -> {
            int num = sum();
            return num;
        });
        // 确保拿到result 并输出
        try {
            result = (Integer) future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("异步计算结果为：" + result);
        System.out.println("使用时间：" + (System.currentTimeMillis() - start) + " ms");

        //退出主线程
        while (true) {
            if (result != 0) {
                executorService.shutdown();
                break;
            }
        }
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if (a < 2)
            return 1;
        return fibo(a - 1) + fibo(a - 2);
    }
}
