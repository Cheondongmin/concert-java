package com.hhplus.concert.core.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UsersChargeUnitTest {
    @Test
    void 유저의_잔액_추가_로직을_검증한다(){
        // given
        Users user = new Users(1L, 1000L);

        // when
        user.addAmount(3000L);

        // then
        assertThat(user.getUserAmount()).isEqualTo(4000L);
    }
}