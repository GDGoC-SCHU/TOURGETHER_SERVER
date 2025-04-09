package com.gdc.tripmate.global.redis;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCacheUtil {

	private final RedisTemplate<String, Object> redisObjectTemplate;

	/**
	 * 캐시에 데이터 저장
	 */
	public void put(String key, Object value, long ttl, TimeUnit timeUnit) {
		redisObjectTemplate.opsForValue().set(key, value, ttl, timeUnit);
	}

	/**
	 * 캐시에서 데이터 조회
	 */
	public <T> T get(String key, Class<T> type) {
		Object value = redisObjectTemplate.opsForValue().get(key);
		if (value == null) {
			return null;
		}
		return type.cast(value);
	}

	/**
	 * 캐시에 키가 존재하는지 확인
	 */
	public boolean hasKey(String key) {
		return Boolean.TRUE.equals(redisObjectTemplate.hasKey(key));
	}

	/**
	 * 캐시에서 데이터 삭제
	 */
	public void delete(String key) {
		redisObjectTemplate.delete(key);
	}

	/**
	 * 패턴과 일치하는 모든 키 삭제
	 */
	public void deletePattern(String pattern) {
		Set<String> keys = redisObjectTemplate.keys(pattern);
		if (keys != null && !keys.isEmpty()) {
			redisObjectTemplate.delete(keys);
		}
	}

	/**
	 * 캐시 데이터 만료시간 설정
	 */
	public void setExpire(String key, long timeout, TimeUnit timeUnit) {
		redisObjectTemplate.expire(key, timeout, timeUnit);
	}

	/**
	 * 캐시 데이터 만료시간 조회
	 */
	public long getExpire(String key, TimeUnit timeUnit) {
		return redisObjectTemplate.getExpire(key, timeUnit);
	}
}