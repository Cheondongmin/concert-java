package com.hhplus.concert.core.domain.user;

import com.hhplus.concert.core.interfaces.api.surppot.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class UsersUnitTest {
    @Test
    void 유저의_잔액_추가_로직을_검증한다(){
        // given
        Users user = new Users(1L, 1000L);

        // when
        user.addAmount(3000L);

        // then
        assertThat(user.getUserAmount()).isEqualTo(4000L);
    }

    @Test
    void 유저의_잔액차감시_잔액이_부족할_경우_예외가_발생한다(){
        // given
        Users user = new Users(1L, 1000L);

        // when & then
        assertThatThrownBy(() ->  user.checkConcertAmount(2000L))
                .isInstanceOf(ApiException.class)
                .hasMessage("유저의 잔액이 부족합니다 잔액을 다시 확인해주세요!");
    }
}