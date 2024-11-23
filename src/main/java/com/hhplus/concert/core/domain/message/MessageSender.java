package com.hhplus.concert.core.domain.message;

public interface MessageSender {
    void sendMessage(String message) throws Exception;
}
