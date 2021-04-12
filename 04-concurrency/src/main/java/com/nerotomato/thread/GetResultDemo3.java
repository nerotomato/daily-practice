package com.nerotomato.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 将FutureTask任务作为参数传给Thread线程执行，获取返回结果
 * 使用单独线程，单独执行任务
 * Created by nero on 2021/4/11.
 */
public class GetResultDemo3 {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        int result = 0;
        FutureTask futureTask = new FutureTask<Integer>(()->{
            return sum();
        });

        Thread t1 = new Thread(futureTask);
        t1.start();
        try {
            result = (int) futureTask.get();
            System.out.println("异步计算结果为：" + result);
            System.out.println("使用时间：" + (System.currentTimeMillis() - start) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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