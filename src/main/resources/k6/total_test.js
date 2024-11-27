import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        concert_reservation: {
            executor: 'ramping-vus',
            startVUs: 0, // 시작 VU 수
            stages: [
                { duration: '15s', target: 50 },
                { duration: '15s', target: 150 },
                { duration: '15s', target: 50 },
                { duration: '15s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95%의 요청이 2초 이내에 완료
        http_req_failed: ['rate<0.01'], // 실패율이 1% 미만
    },
    ext: {
        influxdb: {
            enabled: true,
            address: 'http://localhost:8086', // InfluxDB 주소
            database: 'k6',
            tags: { environment: 'staging' },
        },
    },
};

const BASE_URL = 'http://host.docker.internal:8080/v1/api';

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
    const userId = Math.floor(Math.random() * 100000) + 1; // 사용자 ID 범위: 1 ~ 100,000

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
    const maxAttempts = 15; // 최대 재시도 횟수
    const waitTime = 2; // 대기 시간(초)

    while (queueStatus !== 'PROGRESS' && attempts < maxAttempts) {
        const queueRes = http.post(`${BASE_URL}/queue/token/check`, null, {
            headers: { 'Content-Type': 'application/json', 'Authorization': token }
        });

        check(queueRes, { '대기열 확인 성공': (r) => r.status === 200 });
        queueStatus = queueRes.status === 200 ? queueRes.json().data.status : queueStatus;

        if (queueStatus === 'PROGRESS') break;

        attempts++;
        sleep(waitTime); // 대기
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
        }
    } else {
        console.warn(`사용자 ${userId}은 PROGRESS 상태에 도달하지 못했습니다.`);
    }

    sleep(1);
}

export function teardown(data) {
    console.log('테스트 완료');
}
