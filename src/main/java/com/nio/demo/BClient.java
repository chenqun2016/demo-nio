package com.nio.demo;

public class BClient {

    public static void main(String[] args) {
        try {
            new NioClient().start("天使");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
