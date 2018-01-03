package com.tsimura.waterbot.web.controller;

import com.github.messenger4j.MessengerPlatform;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.exceptions.MessengerVerificationException;
import com.github.messenger4j.receive.MessengerReceiveClient;
import com.github.messenger4j.receive.handlers.PostbackEventHandler;
import com.github.messenger4j.receive.handlers.QuickReplyMessageEventHandler;
import com.github.messenger4j.receive.handlers.TextMessageEventHandler;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.QuickReply;
import com.github.messenger4j.send.SenderAction;
import com.tsimura.waterbot.service.BotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import static com.github.messenger4j.MessengerPlatform.*;
import static com.tsimura.waterbot.Constants.*;

@Slf4j
@RestController
@RequestMapping("/callback")
public class MessengerCallbackHandler {

    private final MessengerReceiveClient receiveClient;
    private final MessengerSendClient sendClient;
    private final BotService botService;

    @Autowired
    public MessengerCallbackHandler(@Value("${messenger4j.appSecret}") final String appSecret,
                                    @Value("${messenger4j.verifyToken}") final String verifyToken,
                                    final MessengerSendClient sendClient,
                                    final BotService botService) {
        log.debug("Initializing MessengerReceiveClient - appSecret: {} | verifyToken: {}", appSecret, verifyToken);
        this.receiveClient = MessengerPlatform.newReceiveClientBuilder(appSecret, verifyToken)
                .onTextMessageEvent(newTextMessageEventHandler())
                .onPostbackEvent(newPostbackEventHandler())
                .onQuickReplyMessageEvent(newQuickReplyMessageEventHandler())
                .build();
        this.sendClient = sendClient;
        this.botService = botService;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(@RequestParam(MODE_REQUEST_PARAM_NAME) final String mode,
                                                @RequestParam(VERIFY_TOKEN_REQUEST_PARAM_NAME) final String verifyToken,
                                                @RequestParam(CHALLENGE_REQUEST_PARAM_NAME) final String challenge) {

        log.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode, verifyToken, challenge);
        try {
            return ResponseEntity.ok(this.receiveClient.verifyWebhook(mode, verifyToken, challenge));
        } catch (Exception e) {
            log.warn("Webhook verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Void> handleCallback(@RequestBody final String payload,
                                               @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) {
        try {
            this.receiveClient.processCallbackPayload(payload, signature);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (MessengerVerificationException e) {
            log.warn("Processing of callback payload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(HttpStatus.OK).body("Hey!");
    }

    private TextMessageEventHandler newTextMessageEventHandler() {
        return event -> {
            log.debug("Received TextMessageEvent: {}", event);

            final String messageId = event.getMid();
            final String messageText = event.getText();
            final String senderId = event.getSender().getId();
            final Date timestamp = event.getTimestamp();

            log.info("Received message '{}' with text '{}' from user '{}' at '{}'",
                    messageId, messageText, senderId, timestamp);

            try {
                switch (messageText.toLowerCase()) {
                    case "hi":
                    case "hello":
                    case "hey":
                        sendClient.sendTextMessage(senderId, HELLO_MESSAGES.get((int)(HELLO_MESSAGES.size() * Math.random())));
                        break;
                    case "start":
                        getStarted(senderId);
                        break;
                    default:
                        sendClient.sendTextMessage(senderId, String.format(MESSAGE_DEFAULT_ANSWER, botService.getUserName(senderId)));
                        break;
                }
            } catch (MessengerApiException | MessengerIOException e) {
                handleSendException(e);
            }
        };
    }

    private QuickReplyMessageEventHandler newQuickReplyMessageEventHandler() {
        return event -> {
            log.debug("Received QuickReplyEvent: {}, {}", event);

            final String senderId = event.getSender().getId();
            final String payload = event.getQuickReply().getPayload();

            try {
                switch (payload) {
                    case PAYLOAD_GET_STARTED:
                        getStarted(senderId);
                        break;
                    case PAYLOAD_START:
                        start(senderId);
                        break;
                    case PAYLOAD_CAD_1:
                    case PAYLOAD_CAD_3:
                    case PAYLOAD_CAD_6:
                    case PAYLOAD_CAD_DONT_COUNT:
                        reactCupsADay(senderId, payload);
                        break;
                    case PAYLOAD_FRQ_1:
                        botService.setReminder(senderId, 1);
                        sendClient.sendTextMessage(senderId, "Thanks! :) Will remind you");
                        break;
                    case PAYLOAD_FRQ_2:
                        botService.setReminder(senderId, 2);
                        sendClient.sendTextMessage(senderId, "Thanks! :) Will remind you");
                        break;
                    case PAYLOAD_FRQ_3:
                        botService.setReminder(senderId, 3);
                        sendClient.sendTextMessage(senderId, "Thanks! :) Will remind you");
                        break;
                    case PAYLOAD_DONE_1:
                    case PAYLOAD_DONE_3:
                    case PAYLOAD_DONE_6:
                    case PAYLOAD_DONE_8:
                        sendClient.sendTextMessage(senderId, "Thanks! :) progress saved");
                        break;
                    default:
                        log.warn("No scenario for quickReply payload = {}", payload);
                        sendClient.sendSenderAction(senderId, SenderAction.MARK_SEEN);
                        break;
                }
            } catch (MessengerApiException | MessengerIOException e) {
                handleSendException(e);
            }
        };
    }

    private PostbackEventHandler newPostbackEventHandler() {
        return event -> {
            final String senderId = event.getSender().getId();
            final String recipientId = event.getRecipient().getId();
            final String payload = event.getPayload();
            final Date timestamp = event.getTimestamp();

            log.info("Received postback for user '{}' and page '{}' with payload '{}' at '{}'",
                    senderId, recipientId, payload, timestamp);

            try {
                switch (payload) {
                    case PAYLOAD_GET_STARTED:
                        getStarted(senderId);
                        break;
                    default:
                        log.warn("No scenario for postback payload = {}", payload);
                        break;
                }
            } catch (MessengerApiException | MessengerIOException e) {
                handleSendException(e);
            }
        };
    }

    private void reactCupsADay(String senderId, String payload) throws MessengerApiException, MessengerIOException {
        switch (payload) {
            case PAYLOAD_CAD_1:
            case PAYLOAD_CAD_DONT_COUNT:
                sendClient.sendImageAttachment(senderId, IMG_DISAPPOINTED);
                break;
            case PAYLOAD_CAD_3:
                sendClient.sendImageAttachment(senderId, IMG_NOT_SATISFIED);
                break;
            case PAYLOAD_CAD_6:
                sendClient.sendImageAttachment(senderId, IMG_SATISFIED);
                sendClient.sendSenderAction(senderId, SenderAction.TYPING_ON);
                sendClient.sendTextMessage(senderId, MESSAGE_GOOD_FREQUENCY);
                sendClient.sendSenderAction(senderId, SenderAction.TYPING_ON);
                sendClient.sendTextMessage(senderId, MESSAGE_SET_DAILY_REMINDER,
                        QuickReply.newListBuilder()
                                .addTextQuickReply("Once a day", PAYLOAD_FRQ_1).toList()
                                .build());
                return;
        }
        sendClient.sendSenderAction(senderId, SenderAction.TYPING_ON);
        sendClient.sendTextMessage(senderId, MESSAGE_RECOMMENDED_AMOUNT);
        sendClient.sendSenderAction(senderId, SenderAction.TYPING_ON);
        sendClient.sendTextMessage(senderId, MESSAGE_CHOOSE_FREQUENCY,
                QuickReply.newListBuilder()
                        .addTextQuickReply("3 times a day", PAYLOAD_FRQ_3).toList()
                        .addTextQuickReply("Twice a day", PAYLOAD_FRQ_2).toList()
                        .addTextQuickReply("Once a day", PAYLOAD_FRQ_1).toList()
                        .build());
    }

    private void start(String senderId) throws MessengerApiException, MessengerIOException {
        sendClient.sendTextMessage(senderId, MESSAGE_BEFORE_WE_BEGIN);
        sendClient.sendSenderAction(senderId, SenderAction.TYPING_ON);
        sendClient.sendTextMessage(senderId, MESSAGE_CUPS_A_DAY,
                QuickReply.newListBuilder()
                        .addTextQuickReply("1-2 cups", PAYLOAD_CAD_1).toList()
                        .addTextQuickReply("3-5 cups", PAYLOAD_CAD_3).toList()
                        .addTextQuickReply("6 and more", PAYLOAD_CAD_6).toList()
                        .addTextQuickReply("I don't count", PAYLOAD_CAD_DONT_COUNT).toList()
                        .build());
    }

    private void getStarted(String senderId) throws MessengerApiException, MessengerIOException {
        sendClient.sendTextMessage(senderId, String.format(MESSAGE_GREETING, botService.getUserName(senderId)));
        sendClient.sendSenderAction(senderId, SenderAction.TYPING_ON);
        sendClient.sendTextMessage(senderId, MESSAGE_GET_STARTED, QuickReply.newListBuilder().addTextQuickReply("Let's Start!", PAYLOAD_START).toList().build());
    }

    private void handleSendException(Exception e) {
        log.error("Message could not be sent. An unexpected error occurred.", e);
    }

}
