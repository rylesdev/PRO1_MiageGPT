package com.miage.miagegpt.model;

import java.time.LocalDateTime;

public class Conversation {
    private String name;
    private LocalDateTime date;
    private String history;
    private String language;
    private int messageCount;

    public Conversation() {}

    public Conversation(String name, LocalDateTime date, String history, String language, int messageCount) {
        this.name = name;
        this.date = date;
        this.history = history;
        this.language = language;
        this.messageCount = messageCount;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getHistory() { return history; }
    public void setHistory(String history) { this.history = history; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
}
