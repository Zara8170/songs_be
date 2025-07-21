package com.example.song_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SongBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SongBeApplication.class, args);
    }

}
