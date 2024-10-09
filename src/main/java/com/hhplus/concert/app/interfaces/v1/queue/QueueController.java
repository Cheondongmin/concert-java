package com.hhplus.concert.app.interfaces.v1.queue;

import com.hhplus.concert.app.interfaces.common.CommonRes;
import com.hhplus.concert.app.interfaces.v1.queue.req.CreateQueueTokenReq;
import com.hhplus.concert.app.interfaces.v1.queue.res.CreateQueueTokenRes;
import com.hhplus.concert.app.interfaces.v1.queue.res.SelectQueueTokenRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/queue")
public class QueueController {

    @PostMapping("/token")
    public CommonRes<CreateQueueTokenRes> createQueueToken(
            @RequestBody CreateQueueTokenReq req
    ) {
        return CommonRes.success(new CreateQueueTokenRes("유저토큰"));
    }

    @GetMapping("/token")
    public CommonRes<SelectQueueTokenRes> getQueueToken(
            @RequestHeader("Authorization") String token
    ) {
        return CommonRes.success(new SelectQueueTokenRes(960430, "WAITING"));
    }

}
