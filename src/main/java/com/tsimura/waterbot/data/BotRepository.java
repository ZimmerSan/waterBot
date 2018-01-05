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
        jdbcTemplate.update("INSERT INTO users (user_id, frequency) VALUES(?, ?) ON CONFLICT (user_id) DO UPDATE set frequency = ?", userId, frequency, frequency);
    }

    public List<String> getUsersByReminder(int frequency) {
        return jdbcTemplate.queryForList("select USER_ID from users where frequency >= ?", String.class, frequency);
    }

    public List<String> getUserFrequency(String id) {
        return jdbcTemplate.queryForList("select FREQUENCY from users where id = ?", String.class, id);
    }

    public List<String> getAllUsers() {
        return jdbcTemplate.queryForList("select USER_ID from users", String.class);
    }

}
