package com.tsimura.waterbot.service;

import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.QuickReply;
import com.github.messenger4j.user.UserProfileClient;
import com.tsimura.waterbot.Constants;
import com.tsimura.waterbot.data.BotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private void sendNotifications() {
        log.debug("sendNotifications invoked");
        try {
            sendClient.sendTextMessage("1630232380376445", "test");
        } catch (MessengerApiException | MessengerIOException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 3 8 * * *")
    private void sendDaily() {
        sendReminders(1, "Good morning %s :) don't forget drink water today");
    }

    @Scheduled(cron = "0 3 12 * * *")
    private void sendTwice() {
        sendReminders(2, "Hey %s ;) don't forget drink water!");
    }

    @Scheduled(cron = "0 3 15 * * *")
    private void sendThreeTimes() {
        sendReminders(3, "Hi %s ;) doing well with your water challenge?");
    }

    @Scheduled(cron = "0 3 17 * * *")
    private void sendEveningNotification() {
        repository.getAllUsers().forEach(id -> {
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
        });
    }

    private void sendReminders(int minFrequency, String text) {
        repository.getUsersByReminder(minFrequency).forEach(id -> {
            try {
                sendClient.sendImageAttachment(id, Constants.IMG_WATER_REMINDER);
                sendClient.sendTextMessage(id, String.format(text, getUserName(id)));
            } catch (MessengerApiException | MessengerIOException e) {
                e.printStackTrace();
            }
        });
    }

}
