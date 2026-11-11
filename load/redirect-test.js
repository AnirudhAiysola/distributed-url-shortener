import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const RATE = Number(__ENV.RATE || 500);   // requests per second in the measured phase
const HOLD = __ENV.HOLD || '2m';          // how long to measure

export const options = {
  scenarios: {
    warmup: {
      executor: 'constant-arrival-rate',
      rate: Math.max(1, Math.floor(RATE / 2)),
      timeUnit: '1s',
      duration: '1m',
      preAllocatedVUs: 50,
      maxVUs: 500,
      tags: { phase: 'warmup' },        // not judged
    },
    measure: {
      executor: 'constant-arrival-rate',
      rate: RATE,
      timeUnit: '1s',
      duration: HOLD,
      startTime: '1m',                  // starts after warm-up
      preAllocatedVUs: 100,
      maxVUs: 1000,
      tags: { phase: 'measure' },       // judged by the thresholds
    },
  },
  thresholds: {
    'http_req_duration{phase:measure}': ['p(99)<50'],
    'http_req_failed{phase:measure}': ['rate<0.01'],
    'dropped_iterations{scenario:measure}': ['count<1'],
  },
};

export function setup() {
  const codes = [];
  for (let i = 0; i < 100; i++) {
    const res = http.post(
      `${BASE}/api/v1/urls`,
      JSON.stringify({ longUrl: `https://example.com/page/${i}` }),
      { headers: { 'Content-Type': 'application/json' } },
    );
    if (res.status === 201) {
      codes.push(res.json('code'));
    }
  }
  if (codes.length === 0) {
    throw new Error('Could not create any links. Is BASE_URL right?');
  }
  return { codes };
}

export default function (data) {
  const code = data.codes[Math.floor(Math.random() * data.codes.length)];
  const res = http.get(`${BASE}/${code}`, { redirects: 0, tags: { type: 'redirect' } });
  check(res, { 'status is 302': (r) => r.status === 302 });
}