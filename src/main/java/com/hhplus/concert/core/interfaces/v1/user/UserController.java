package com.hhplus.concert.core.interfaces.v1.user;

import com.hhplus.concert.core.domain.user.UserService;
import com.hhplus.concert.core.interfaces.common.CommonRes;
import com.hhplus.concert.core.interfaces.v1.user.req.UserAmountChargeReq;
import com.hhplus.concert.core.interfaces.v1.user.res.SelectUserAmountRes;
import com.hhplus.concert.core.interfaces.v1.user.res.UserAmountChargeRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저 API", description = "유저와 관련된 API 입니다. 모든 API는 대기열 토큰 헤더(Authorization) 가 필요합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "유저 요금 충전 API")
    @PostMapping("/amount")
    public CommonRes<UserAmountChargeRes> UserAmountCharge(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token,
            @RequestBody UserAmountChargeReq req
    ) {
        return CommonRes.success(new UserAmountChargeRes(userService.chargeUserAmount(token, req.amount())));
    }

    @Operation(summary = "유저 요금 조회 API")
    @GetMapping("/amount")
    public CommonRes<SelectUserAmountRes> selectUserAmount(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token
    ) {
        return CommonRes.success(new SelectUserAmountRes(userService.selectUserAmount(token)));
    }
}
