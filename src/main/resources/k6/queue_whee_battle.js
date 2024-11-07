import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '30s',
};

export default function () {
    const url = 'http://localhost:8080/v1/api/queue/token/check';
    const params = {
        headers: {
            'Authorization': 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInRva2VuIjoiZWE3ZjM2NjAtZGJjNi00NGU4LTk2NzYtYTRlNDVjNDNjNmQ0IiwiZW50ZXJlZER0IjoxNzMwOTkyMTA1MzQ3LCJleHBpcmVkRHQiOjE3MzA5OTI0MDUzNDd9.oE_HLZ3RKrYxy9upxSLSvGrtaavnMcUiGo6SwAjhaSE',
            'Content-Type': 'application/json',
        },
    };

    // POST 요청으로 변경
    let res = http.post(url, null, params);

    // 응답 코드가 200인지 확인
    check(res, { 'status is 200': (r) => r.status === 200 });

    sleep(1); // 1초 대기
}
