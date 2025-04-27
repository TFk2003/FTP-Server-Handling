package org.example.cnproject.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;

@Configuration
public class FTPConfig {
    @Value("${ftp.server.host}")
    private String host;

    @Value("${ftp.server.port}")
    private int port;

    @Value("${ftp.server.username}")
    private String username;

    @Value("${ftp.server.password}")
    private String password;

    @Bean
    public DefaultFtpSessionFactory ftpSessionFactory() {
        DefaultFtpSessionFactory sessionFactory = new DefaultFtpSessionFactory();
        sessionFactory.setHost(host);
        sessionFactory.setPort(port);
        sessionFactory.setUsername(username);
        sessionFactory.setPassword(password);
        return sessionFactory;
    }
}
