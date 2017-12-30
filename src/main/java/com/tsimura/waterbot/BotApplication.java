package com.tsimura.waterbot;

import com.github.messenger4j.MessengerPlatform;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.user.UserProfileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class BotApplication {

	public static void main(String[] args) {
		SpringApplication.run(BotApplication.class, args);
	}

	@Bean
	public MessengerSendClient messengerSendClient(@Value("${messenger4j.pageAccessToken}") String pageAccessToken) {
		log.debug("Initializing MessengerSendClient - pageAccessToken: {}", pageAccessToken);
		return MessengerPlatform.newSendClientBuilder(pageAccessToken).build();
	}

	@Bean
	public UserProfileClient userProfileClient(@Value("${messenger4j.pageAccessToken}") String pageAccessToken) {
		return MessengerPlatform.newUserProfileClientBuilder(pageAccessToken).build();
	}

}
