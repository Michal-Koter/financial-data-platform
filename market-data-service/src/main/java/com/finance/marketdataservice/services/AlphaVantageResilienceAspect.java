package com.finance.marketdataservice.services;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Aspect
@Component
@Log4j2
public class AlphaVantageResilienceAspect {
    private final RateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    @Autowired
    public AlphaVantageResilienceAspect(
            RateLimiterRegistry limiterRegistry,
            CircuitBreakerRegistry breakerRegistry,
            RetryRegistry retryRegistry
    ) {
        this.rateLimiter = limiterRegistry.rateLimiter("alphaVantageRateLimit");
        this.circuitBreaker = breakerRegistry.circuitBreaker("alphaVantageBreak");
        this.retry = retryRegistry.retry("alphaVantageRetry");

        // Register retry event listener
        this.retry.getEventPublisher()
                .onRetry(event ->
                        log.warn("Retry attempt #{} for method [{}] due to: {}",
                                event.getNumberOfRetryAttempts(),
                                event.getName(),
                                event.getLastThrowable() != null ? event.getLastThrowable().toString() : "unknown"))
                .onSuccess(event ->
                        log.info("Retry successful for [{}] after {} attempts",
                                event.getName(),
                                event.getNumberOfRetryAttempts()))
                .onError(event ->
                        log.error("Retry exhausted for [{}]. Attempts: {}, Error: {}",
                                event.getName(),
                                event.getNumberOfRetryAttempts(),
                                event.getLastThrowable() != null ? event.getLastThrowable().toString() : "unknown"));
    }

    @Around("execution(* com.finance.marketdataservice.services.AlphaVantageService.*(..))")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        Supplier<Object> original = () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
        Supplier<Object> protectedCall = RateLimiter
                .decorateSupplier(rateLimiter,
                        CircuitBreaker.decorateSupplier(circuitBreaker,
                                Retry.decorateSupplier(retry, original)));

        try {
            return protectedCall.get();
        } catch (RequestNotPermitted e) {
            log.warn("RATE LIMIT triggered for method: {}", methodName);
            throw e;
        } catch (CallNotPermittedException e) {
            log.warn("CIRCUIT OPEN triggered for method: {}", methodName);
            throw e;
        }
    }


}
