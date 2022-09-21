package com.nio.demo;

public class AClient {

    public static void main(String[] args) {
        try {
            new NioClient().start("大熊");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
