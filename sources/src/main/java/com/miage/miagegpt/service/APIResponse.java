package com.miage.miagegpt.service;

public class APIResponse {
    public String content;
    public long pingMs;

    public APIResponse(String content, long pingMs) {
        this.content = content;
        this.pingMs = pingMs;
    }
}
