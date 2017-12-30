package com.tsimura.waterbot.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@org.springframework.stereotype.Repository
public class BotRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BotRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveUserReminder(String userId, int frequency) {
        jdbcTemplate.update("INSERT INTO users (user_id, frequency) VALUES(?, ?) ON DUPLICATE KEY UPDATE frequency=?", userId, frequency, frequency);
    }

    public List<String> getUsersByReminder(int frequency) {
        return jdbcTemplate.queryForList("select USER_ID from users where frequency >= ?", String.class, frequency);
    }

    public List<String> getAllUsers() {
        return jdbcTemplate.queryForList("select USER_ID from users", String.class);
    }

}
