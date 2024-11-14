import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        concert_reservation: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 5 },
                { duration: '30s', target: 5 },
                { duration: '10s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8080/v1/api';

export function setup() {
    const initialUserId = 1;
    const setupRes = http.post(`${BASE_URL}/queue/token`,
        JSON.stringify({ userId: initialUserId }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    console.log('초기 설정 완료');
    return { setupToken: setupRes.json().data.queueToken };
}

export default function (data) {
    const userId = Math.floor(Math.random() * 1000000) + 1;

    // 1. 토큰 발급
    const tokenRes = http.post(`${BASE_URL}/queue/token`,
        JSON.stringify({ userId: userId }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(tokenRes, { '토큰 생성 성공': (r) => r.status === 200 });
    const token = tokenRes.json().data.queueToken;

    // 2. 대기열 확인
    let queueStatus = 'WAITING';
    let attempts = 0;
    const maxAttempts = 15;  // 재시도 횟수 증가
    const waitTime = 2;  // 대기 시간을 늘려 서버 부하 완화

    while (queueStatus !== 'PROGRESS' && attempts < maxAttempts) {
        const queueRes = http.post(`${BASE_URL}/queue/token/check`, null, {
            headers: { 'Content-Type': 'application/json', 'Authorization': token }
        });

        check(queueRes, { '대기열 확인 성공': (r) => r.status === 200 });
        queueStatus = queueRes.status === 200 ? queueRes.json().data.status : queueStatus;

        if (queueStatus === 'PROGRESS') break;

        attempts++;
        sleep(waitTime);  // 대기 시간 조정
    }

    if (queueStatus === 'PROGRESS') {
        // 3. 콘서트 스케줄 조회
        const scheduleRes = http.get(`${BASE_URL}/concerts/schedule`, {
            headers: { 'Content-Type': 'application/json', 'Authorization': token }
        });

        check(scheduleRes, { '스케줄 조회 성공': (r) => r.status === 200 });

        if (scheduleRes.status === 200 && scheduleRes.json().data.length > 0) {
            const scheduleId = scheduleRes.json().data[0].scheduleId;

            // 4. 좌석 조회
            const seatRes = http.get(`${BASE_URL}/concerts/seat?scheduleId=${scheduleId}`, {
                headers: { 'Content-Type': 'application/json', 'Authorization': token }
            });

            check(seatRes, { '좌석 조회 성공': (r) => r.status === 200 });

            if (seatRes.status === 200 && seatRes.json().data.length > 0) {
                const seatId = seatRes.json().data[0].seatId;

                // 5. 좌석 임시 예약
                const reserveRes = http.post(`${BASE_URL}/concerts/reserve`,
                    JSON.stringify({ scheduleId: scheduleId, seatId: seatId }),
                    { headers: { 'Content-Type': 'application/json', 'Authorization': token } }
                );

                check(reserveRes, { '좌석 예약 성공': (r) => r.status === 200 });

                if (reserveRes.status === 200) {
                    const reservationId = reserveRes.json().data.reservationId;

                    // 6. 결제 처리
                    const paymentRes = http.post(`${BASE_URL}/concerts/payment`,
                        JSON.stringify({ reservationId: reservationId }),
                        { headers: { 'Content-Type': 'application/json', 'Authorization': token } }
                    );

                    check(paymentRes, { '결제 성공': (r) => r.status === 200 });
                }
            }
        }
    } else {
        console.warn(`사용자 ${userId}은 PROGRESS 상태에 도달하지 못했습니다.`);
    }

    sleep(1);
}

export function teardown(data) {
    console.log('테스트 완료');
}