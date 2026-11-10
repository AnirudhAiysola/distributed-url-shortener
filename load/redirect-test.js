import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    redirects: {
      executor: 'ramping-arrival-rate',   // open model: fixed requests/sec
      startRate: 50,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 500,
            stages: [
              { target: 200, duration: '30s' },  // warm-up
              { target: 500, duration: '30s' },
              { target: 500, duration: '2m' },   // hold at 500 req/s
              { target: 0,   duration: '15s' },
            ],
    },
  },
  thresholds: {
    'http_req_duration{type:redirect}': ['p(99)<50'],  // our SLO: p99 under 50 ms
    'http_req_failed': ['rate<0.01'],                  // under 1% errors
  },
};

// Runs once: create 100 links to redirect to
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
    throw new Error('Could not create any links. Is the app running?');
  }
  return { codes };
}

// Runs for every request: redirect to a random link, without following it
export default function (data) {
  const code = data.codes[Math.floor(Math.random() * data.codes.length)];
  const res = http.get(`${BASE}/${code}`, {
    redirects: 0,
    tags: { type: 'redirect' },
  });
  check(res, { 'status is 302': (r) => r.status === 302 });
}