package com.example.waterbot.bot.web.controller;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import static com.github.messenger4j.MessengerPlatform.*;

@Slf4j
@RestController
@RequestMapping("/callback")
public class MessengerCallbackHandler {

    public static final String DEFAULT_ANSWER_TEMPLATE = "Sorry, %s. I am a young WaterBot and still learning. Type \"Start\" to show the start over.";

    private final MessengerReceiveClient receiveClient;
    private final MessengerSendClient sendClient;

    @Autowired
    public MessengerCallbackHandler(@Value("${messenger4j.appSecret}") final String appSecret,
                                            @Value("${messenger4j.verifyToken}") final String verifyToken,
                                            final MessengerSendClient sendClient) {
        log.debug("Initializing MessengerReceiveClient - appSecret: {} | verifyToken: {}", appSecret, verifyToken);
        this.receiveClient = MessengerPlatform.newReceiveClientBuilder(appSecret, verifyToken)
                .onTextMessageEvent(newTextMessageEventHandler())
                .onPostbackEvent(newPostbackEventHandler())
                .onQuickReplyMessageEvent(newQuickReplyMessageEventHandler())
                .build();
        this.sendClient = sendClient;
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
            log.debug("Processed callback payload successfully: {}", payload);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (MessengerVerificationException e) {
            log.warn("Processing of callback payload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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
                    case "hm":
                        sendClient.sendTextMessage(senderId, "How are you?", QuickReply.newListBuilder()
                                .addTextQuickReply("One", "1").toList()
                                .addTextQuickReply("Two", "2").toList()
                                .build()
                        );
                        break;
                    case "hi":
                    case "hello":
                        sendClient.sendTextMessage(senderId, "Yo!");
                        break;
                    case "start":
                        break;
                    default:
                        sendClient.sendTextMessage(senderId, String.format(DEFAULT_ANSWER_TEMPLATE, "Bro"));
                }
            } catch (MessengerApiException | MessengerIOException e) {
                handleSendException(e);
            }
        };
    }

    private QuickReplyMessageEventHandler newQuickReplyMessageEventHandler() {
        return event -> {
            log.debug("Received QuickReplyEvent: {}, {}", event, event.getQuickReply());
        };
    }

    private PostbackEventHandler newPostbackEventHandler() {
        return event -> {
            log.debug("Received PostbackEvent: {}", event);

            final String senderId = event.getSender().getId();
            final String recipientId = event.getRecipient().getId();
            final String payload = event.getPayload();
            final Date timestamp = event.getTimestamp();

            log.info("Received postback for user '{}' and page '{}' with payload '{}' at '{}'",
                    senderId, recipientId, payload, timestamp);

            try {
                sendClient.sendTextMessage(senderId, "Postback called");
            } catch (MessengerApiException | MessengerIOException e) {
                handleSendException(e);
            }
        };
    }

    private void handleSendException(Exception e) {
        log.error("Message could not be sent. An unexpected error occurred.", e);
    }
}
