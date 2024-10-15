package com.hhplus.concert.app.interfaces.v1.queue;

import com.hhplus.concert.app.facade.QueueFacade;
import com.hhplus.concert.app.interfaces.common.CommonRes;
import com.hhplus.concert.app.interfaces.v1.queue.req.CreateQueueTokenReq;
import com.hhplus.concert.app.interfaces.v1.queue.res.CreateQueueTokenRes;
import com.hhplus.concert.app.interfaces.v1.queue.res.SelectQueueTokenRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "대기열 API", description = "콘서트 대기열을 발급받는 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/queue")
public class QueueController {

    private final QueueFacade queueFacade;

    @Operation(summary = "유저 대기열 토큰 발급 API")
    @PostMapping("/token")
    public CommonRes<CreateQueueTokenRes> createQueueToken(
            @RequestBody CreateQueueTokenReq req
    ) {
        return CommonRes.success(new CreateQueueTokenRes(queueFacade.enterQueue(req.userId())));
    }

    @Operation(summary = "유저 대기열 토큰 체크 API")
    @PostMapping("/token/check")
    public CommonRes<SelectQueueTokenRes> getQueueToken(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token
    ) {
        return CommonRes.success(new SelectQueueTokenRes(960430, "WAITING"));
    }
}
