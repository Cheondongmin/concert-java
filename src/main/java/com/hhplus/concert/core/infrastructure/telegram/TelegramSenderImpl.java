package com.hhplus.concert.core.infrastructure.telegram;

import com.google.gson.JsonObject;
import com.hhplus.concert.core.domain.telegram.MessageSender;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TelegramSenderImpl implements MessageSender {
    private static String baseUrl;  // URL에서 query parameter 제거
    private static String chatId;   // chat_id를 별도로 관리
    private static String prefix;

    public static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .build();

    @Value("${message.bot-token}")  // application.yml 수정 필요
    public void setTelegramBotToken(String token) {
        baseUrl = "https://api.telegram.org/bot" + token + "/sendMessage";
    }

    @Value("${message.chat-id}")    // application.yml 수정 필요
    public void setChatId(String id) {
        chatId = id;
    }

    @Value("${message.env}")
    public void setPrefix(String val) {
        prefix = val;
    }

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public void sendMessage(String msg) {
        log.info("TelegramSender 에서 텔레그램 메시지 전송 msg:{}", msg);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chat_id", chatId);  // chat_id를 body에 포함
        jsonObject.addProperty("text", prefix + "\n" + "본 메시지는 항플 콘서트에서 알림이 전송되었습니다.\n\n" + msg);
        jsonObject.addProperty("parse_mode", "HTML");  // 선택적: HTML 포맷팅 지원

        String json = jsonObject.toString();

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(baseUrl)  // query parameter 없는 base URL 사용
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Telegram API 에러. 상태 코드: {}, 에러 내용: {}", response.code(), errorBody);
            } else {
                log.info("Telegram 메시지 전송 성공. 응답 코드: {}", response.code());
            }
        } catch (IOException e) {
            log.error("Telegram API 호출 실패", e);
        }
    }
}
