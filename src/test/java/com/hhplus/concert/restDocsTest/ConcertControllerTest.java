package com.hhplus.concert.restDocsTest;

import com.google.gson.Gson;
import com.hhplus.concert.app.interfaces.v1.concert.req.PaymentConcertReq;
import com.hhplus.concert.app.interfaces.v1.concert.req.ReserveConcertReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})  // JUnit 5 확장
public class ConcertControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    private Gson gson;

    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentation) {
        gson = new Gson();
        RestDocumentationResultHandler documentationHandler = document("{method-name}");

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation)) // REST Docs 설정
                .alwaysDo(documentationHandler)
                .build();
    }

    // 콘서트 스케줄 조회
    @Test
    public void testSelectConcert() throws Exception {
        this.mockMvc.perform(get("/v1/api/concerts/schedule")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andDo(document("select-concert",
                        responseFields(
                                fieldWithPath("resultType").description("응답 결과 타입 (성공 또는 실패)"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.scheduleId").description("콘서트 스케줄 ID"),
                                fieldWithPath("data.concertTitle").description("콘서트 제목"),
                                fieldWithPath("data.openDate").description("콘서트 오픈 날짜"),
                                fieldWithPath("data.startTime").description("콘서트 시작 시간"),
                                fieldWithPath("data.endTime").description("콘서트 종료 시간"),
                                fieldWithPath("data.seatStatus").description("좌석 상태")
                        )
                ));
    }

    // 좌석 조회
    @Test
    public void testSelectSeat() throws Exception {
        this.mockMvc.perform(get("/v1/api/concerts/seat")
                        .header("Authorization", "Bearer token")
                        .param("scheduleId", "1"))
                .andExpect(status().isOk())
                .andDo(document("select-seat",
                        responseFields(
                                fieldWithPath("resultType").description("응답 결과 타입 (성공 또는 실패)"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.seatId").description("좌석 ID"),
                                fieldWithPath("data.position").description("좌석 위치"),
                                fieldWithPath("data.amount").description("좌석 가격"),
                                fieldWithPath("data.seatStatus").description("좌석 상태")
                        )
                ));
    }

    // 콘서트 예약
    @Test
    public void testReserveConcert() throws Exception {
        ReserveConcertReq req = new ReserveConcertReq(1, 1);
        String jsonReq = gson.toJson(req);

        this.mockMvc.perform(post("/v1/api/concerts/reserve")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonReq))
                .andExpect(status().isOk())
                .andDo(document("reserve-concert",
                        requestFields(
                                fieldWithPath("scheduleId").description("콘서트 스케줄 ID"),
                                fieldWithPath("seatId").description("좌석 ID")
                        ),
                        responseFields(
                                fieldWithPath("resultType").description("응답 결과 타입 (성공 또는 실패)"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.reservationId").description("예약 ID"),
                                fieldWithPath("data.seatStatus").description("좌석 상태"),
                                fieldWithPath("data.reservedDate").description("예약 시간"),
                                fieldWithPath("data.reservedUntilDate").description("예약 만료 시간")
                        )
                ));
    }

    // 결제 API
    @Test
    public void testPaymentConcert() throws Exception {
        PaymentConcertReq req = new PaymentConcertReq(1234);
        String jsonReq = gson.toJson(req);

        this.mockMvc.perform(post("/v1/api/concerts/payment")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonReq))
                .andExpect(status().isOk())
                .andDo(document("payment-concert",
                        requestFields(
                                fieldWithPath("reservationId").description("예약 ID")
                        ),
                        responseFields(
                                fieldWithPath("resultType").description("응답 결과 타입 (성공 또는 실패)"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.message").description("결제 완료 메시지"),
                                fieldWithPath("data.paymentAmount").description("결제 금액"),
                                fieldWithPath("data.seatStatus").description("좌석 상태"),
                                fieldWithPath("data.queueStatus").description("대기열 상태")
                        )
                ));
    }
}
