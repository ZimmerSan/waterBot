package com.example.waterbot.bot.web.controller;

import com.example.waterbot.bot.model.Entry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class EntryController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/entry")
    public Entry getEntry(@RequestParam(value="name", defaultValue="World") String name) {
        return new Entry(counter.incrementAndGet(), String.format(template, name));
    }

}
