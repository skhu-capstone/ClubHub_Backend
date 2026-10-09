package com.skhu.skhucapstone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// 보관 기간이 지난 알림을 주기적으로 정리하기 위해 스케줄링을 켠다.
@EnableScheduling
@SpringBootApplication
public class SkhuCapstoneApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkhuCapstoneApplication.class, args);
    }

}
