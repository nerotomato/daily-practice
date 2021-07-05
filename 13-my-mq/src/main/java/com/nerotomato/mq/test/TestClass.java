package com.nerotomato.mq.test;

public class TestClass {
    public static void main(String[] args) {
        int num = 10;
        int rightNum = num >> 1;
        int leftNUm = num << 4;
        System.out.println(Integer.toBinaryString(num));
        System.out.println(Integer.toBinaryString(rightNum));
        System.out.println(Integer.toBinaryString(leftNUm));
    }
}