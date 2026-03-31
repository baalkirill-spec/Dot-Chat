from __future__ import annotations

from collections import defaultdict, deque
from datetime import datetime, timedelta, timezone


class RateLimitError(RuntimeError):
    pass


class InMemoryRateLimiter:
    def __init__(self, max_requests: int, window_seconds: int) -> None:
        self._max_requests = max_requests
        self._window_seconds = window_seconds
        self._events: dict[str, deque[datetime]] = defaultdict(deque)

    def consume(self, key: str) -> None:
        now = datetime.now(timezone.utc)
        window_start = now - timedelta(seconds=self._window_seconds)
        queue = self._events[key]
        while queue and queue[0] < window_start:
            queue.popleft()
        if len(queue) >= self._max_requests:
            raise RateLimitError("rate_limit_exceeded")
        queue.append(now)
