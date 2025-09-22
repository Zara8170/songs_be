package com.example.song_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    JdbcRepositoriesAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
@EnableScheduling
public class SongBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SongBeApplication.class, args);
    }

}
