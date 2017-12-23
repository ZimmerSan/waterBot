package com.example.waterbot.bot.model;

import lombok.Getter;

public class Entry {

    @Getter
    private final long id;
    @Getter
    private final String content;

    public Entry(long id, String content) {
        this.id = id;
        this.content = content;
    }

}
