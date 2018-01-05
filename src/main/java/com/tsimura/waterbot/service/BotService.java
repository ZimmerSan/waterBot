package com.tsimura.waterbot.service;

import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.QuickReply;
import com.github.messenger4j.user.UserProfile;
import com.github.messenger4j.user.UserProfileClient;
import com.tsimura.waterbot.Constants;
import com.tsimura.waterbot.data.BotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.tsimura.waterbot.Constants.*;

@Slf4j
@Service
public class BotService {

    private final BotRepository repository;
    private final MessengerSendClient sendClient;
    private final UserProfileClient userProfileClient;

    @Autowired
    public BotService(final BotRepository repository,
                      final MessengerSendClient sendClient,
                      final UserProfileClient userProfileClient) {
        this.repository = repository;
        this.sendClient = sendClient;
        this.userProfileClient = userProfileClient;
    }

    public void setReminder(String senderId, int frequency) {
        log.debug("senderId = {}, frq = {}", senderId, frequency);
        repository.saveUserReminder(senderId, frequency);
    }

    public String getUserName(String userId) {
        try {
            return userProfileClient.queryUserProfile(userId).getFirstName();
        } catch (MessengerApiException | MessengerIOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Scheduled(cron = "0 0/5 * * * *")
    private void keepAwake() {
        log.debug("Don't sleep, dyno!");
    }

    private boolean checkHour(float hour, int exact) {
        return hour >= exact && hour < (exact + 1);
    }

    @Scheduled(cron = "0 0 * * * *")
    private void sendNotifications() {
        int hour = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);

        repository.getAllUsers().forEach(id -> {
            try {
                UserProfile userProfile = userProfileClient.queryUserProfile(id);
                float userHour = hour + userProfile.getTimezoneOffset();
                int userFrequency = repository.getUserFrequency(id);

                if (id.equals("1630232380376445")) sendNotification(id, "Bro, hour is = " + userHour);

                log.debug("id = {}, user = {}, hour = {}, userFrequency = {}", id, userProfile.getFirstName(), userHour, userFrequency);
                if (userFrequency >= 1 && checkHour(userHour, 10)) {
                    sendNotification(id, "Good morning %s :) don't forget drink water today");
                } else if (userFrequency >= 2 && checkHour(userHour, 14)) {
                    sendNotification(id, "Hey %s ;) don't forget drink water!");
                } else if (userFrequency == 3 && checkHour(userHour, 18)) {
                    sendNotification(id, "Hi %s ;) doing well with your water challenge?");
                } else if (checkHour(userHour, 20)) {
                    sendEveningNotification(id);
                }
            } catch (MessengerApiException | MessengerIOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendEveningNotification(String id) {
        try {
            sendClient.sendImageAttachment(id, Constants.IMG_WATER_REMINDER);
            sendClient.sendTextMessage(id,
                    String.format("So how many glasses of water have you drank today %s?", getUserName(id)),
                    QuickReply.newListBuilder()
                            .addTextQuickReply("1-2", PAYLOAD_DONE_1).toList()
                            .addTextQuickReply("3-5", PAYLOAD_DONE_3).toList()
                            .addTextQuickReply("6-8", PAYLOAD_DONE_6).toList()
                            .addTextQuickReply("8+", PAYLOAD_DONE_8).toList()
                            .build());
        } catch (MessengerApiException | MessengerIOException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String id, String text) {
        try {
            sendClient.sendImageAttachment(id, Constants.IMG_WATER_REMINDER);
            sendClient.sendTextMessage(id, String.format(text, getUserName(id)));
        } catch (MessengerApiException | MessengerIOException e) {
            e.printStackTrace();
        }
    }

}
