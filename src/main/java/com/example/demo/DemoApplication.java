package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.demo.dao")
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class);
        System.out.println(hammingDistance(0b1011,0b1000));
        System.out.println(hamming(0b1011,0b1000));

        System.out.println(hammingDistance(0b1100,0b1010));
        System.out.println(hamming(0b1100,0b1010));


    }

    public static int hammingDistance(int x, int y) {
        int cnt = 0;
        x = x ^ y;
        while (x != 0) {
            if ((x & 0x01) == 1)
                cnt++;
            x = x >> 1;
        }
        return cnt;
    }

    public static int hamming(int x, int y) {
        int hamming = x ^ y;
        int cnt = 0;
        while (hamming > 0) {
            hamming = hamming & (hamming - 1);
            cnt++;
        }
        return cnt;
    }
}
