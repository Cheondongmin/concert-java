import http from 'k6/http';
import { check, sleep, fail } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8080/v1/api/queue';

function fetchJwtToken(userId) {
    const payload = JSON.stringify({ userId: userId });
    const res = http.post(`${BASE_URL}/token`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    console.log('Token response:', JSON.stringify(res.json()));
    check(res, {
        'Token created successfully': (r) => r.status === 200,
    });

    return res.json().data.queueToken;  // 올바른 토큰 키 사용
}

export default function () {
    const userId = __VU + Math.floor(Math.random() * 1000);
    const token = fetchJwtToken(userId);
    console.log(`Generated JWT token: ${token}`);

    const checkQueueRes = http.post(`${BASE_URL}/token/check`, null, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': token,  // `Bearer` 없이 토큰만 사용
        },
    });

    console.log('Check Queue Response:', checkQueueRes.status, checkQueueRes.json());
    const checks = check(checkQueueRes, {
        'Check token status is 200': (r) => r.status === 200,
        'Queue position is defined': (r) => r.json().data.queuePosition !== undefined,
        'Queue status is defined': (r) => r.json().data.status !== undefined,
    });

    if (!checks) {
        fail('Failed to get expected response from checkQueue API');
    }

    sleep(1);
}