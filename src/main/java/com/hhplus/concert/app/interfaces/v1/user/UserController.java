package com.hhplus.concert.app.interfaces.v1.user;

import com.hhplus.concert.app.interfaces.common.CommonRes;
import com.hhplus.concert.app.interfaces.v1.user.req.UserAmountChargeReq;
import com.hhplus.concert.app.interfaces.v1.user.res.SelectUserAmountRes;
import com.hhplus.concert.app.interfaces.v1.user.res.UserAmountChargeRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/user")
public class UserController {

    @PostMapping("/amount")
    public CommonRes<UserAmountChargeRes> UserAmountCharge(
            @RequestHeader("Authorization") String token,
            @RequestBody UserAmountChargeReq req
    ) {
        return CommonRes.success(
                new UserAmountChargeRes(
                        50000
                )
        );
    }

    @GetMapping("/amount")
    public CommonRes<SelectUserAmountRes> selectUserAmount(
            @RequestHeader("Authorization") String token
    ) {
        return CommonRes.success(
                new SelectUserAmountRes(
                        50000
                )
        );
    }
}
