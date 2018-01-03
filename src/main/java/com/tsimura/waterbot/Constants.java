package com.tsimura.waterbot;

import java.util.Arrays;
import java.util.List;

public interface Constants {

    /*
    * Payloads
    */

    String PAYLOAD_GET_STARTED = "get_started";
    String PAYLOAD_START = "start";

    String PAYLOAD_CAD_1 = "1_2_a_day";
    String PAYLOAD_CAD_3 = "3_5_a_day";
    String PAYLOAD_CAD_6 = "6_and_more";
    String PAYLOAD_CAD_DONT_COUNT = "dont_count";

    String PAYLOAD_FRQ_3 = "3_reminders";
    String PAYLOAD_FRQ_2 = "2_reminders";
    String PAYLOAD_FRQ_1 = "1_reminder";

    String PAYLOAD_DONE_1 = "1_2_done";
    String PAYLOAD_DONE_3 = "3_5_done";
    String PAYLOAD_DONE_6 = "6_8_done";
    String PAYLOAD_DONE_8 = "8_done";

    String PAYLOAD_BTN_DONE = "btn_done";

    /*
    * Messages
    */

    String MESSAGE_DEFAULT_ANSWER = "Sorry, %s. I am a young WaterBot and still learning. Type \"Start\" to show the start over.";
    String MESSAGE_GREETING = "Hi, %s! I am your personal water trainer :)";
    String MESSAGE_GET_STARTED =    "☑ Daily water reminders\n" +
                                            "☑ Personalized AI recommendations\n" +
                                            "☑ Number of cups of water drank this week\n" +
                                            "☑ Tips about water drinking";
    String MESSAGE_BEFORE_WE_BEGIN = "Before we begin...";
    String MESSAGE_CUPS_A_DAY = "How many cups of water do you drink a day?";

    String MESSAGE_RECOMMENDED_AMOUNT = "The recommended amount of water per day is eight 8-ounce glasses, which equals about 2 liters, or half a gallon.";
    String MESSAGE_CHOOSE_FREQUENCY = "Choose the frequency for water break reminders";
    String MESSAGE_GOOD_FREQUENCY = "You're a real champ! 8 cups is the recommended amount.";
    String MESSAGE_SET_DAILY_REMINDER = "Set a daily reminder to keep track with your good work";

    List<String> HELLO_MESSAGES = Arrays.asList(
            "Hey there :) I am WaterBot! here to help you drink more water and become healthier",
            "Hi! Got your water today?",
            "Hi there am WaterBot"
    );

    /*
    * URLs
    */

    String IMG_SATISFIED = "https://www.dropbox.com/s/uveaewz81vkmy07/17806944_277152209394294_1375427760_n.jpg?raw=1";
    String IMG_NOT_SATISFIED = "https://www.dropbox.com/s/pkkatwedmbtewnu/17821593_277151852727663_561650605_n.jpg?raw=1";
    String IMG_DISAPPOINTED = "https://www.dropbox.com/s/3p7fl6ko5ped1m1/17198292_263277337448448_1721647584_n.jpg?raw=1";
    String IMG_WATER_REMINDER = "https://www.dropbox.com/s/kurac9551n1xx63/16934202_258546177921564_1657735826_n.gif?raw=1";

    /*
    * Mappings
    */

}
