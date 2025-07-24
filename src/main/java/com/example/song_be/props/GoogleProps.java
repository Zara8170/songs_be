package com.example.song_be.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app.props.social.google")
public class GoogleProps {
    private String clientId;
    private String tokenUri;
    private String userInfoUri;
}
