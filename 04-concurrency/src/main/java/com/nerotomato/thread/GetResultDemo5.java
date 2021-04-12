package com.nerotomato.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 使用CompletableFuture.supplyAsync方法
 *
 * Created by nero on 2021/4/11.
 */
public class GetResultDemo5 {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        int result = 0;
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            return sum();
        });

        try {
            result = completableFuture.get();
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
