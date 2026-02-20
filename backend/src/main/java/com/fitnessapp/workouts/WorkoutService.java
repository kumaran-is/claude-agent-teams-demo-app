package com.fitnessapp.workouts;

import com.fitnessapp.common.ForbiddenException;
import com.fitnessapp.common.PagedResponse;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.common.UserContextService;
import com.fitnessapp.security.FirebaseAuthentication;
import com.fitnessapp.workouts.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutService {

    private final WorkoutSessionRepository sessionRepository;
    private final WorkoutSetRepository setRepository;
    private final UserContextService userContextService;

    @Transactional
    public Mono<SessionResponse> startSession(FirebaseAuthentication auth, CreateSessionRequest request) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> {
                    WorkoutSession session = new WorkoutSession();
                    session.setUserId(userId);
                    session.setName(request != null ? request.name() : null);
                    session.setNotes(request != null ? request.notes() : null);
                    session.setStartedAt(Instant.now());
                    return sessionRepository.save(session);
                })
                .map(session -> toSessionResponse(session, Collections.emptyList()));
    }

    public Mono<SessionResponse> getSession(UUID sessionId, FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> sessionRepository.findById(sessionId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Session not found: " + sessionId)))
                        .flatMap(session -> {
                            if (!session.getUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException("Access denied"));
                            }
                            return setRepository.findAllBySessionIdOrderBySetNumber(sessionId)
                                    .collectList()
                                    .map(sets -> toSessionResponse(session, sets));
                        })
                );
    }

    public Mono<PagedResponse<SessionResponse>> listSessions(
            FirebaseAuthentication auth, int page, int size) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> Mono.zip(
                        sessionRepository.findAllByUserIdOrderByStartedAtDesc(userId,
                                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt")))
                                .flatMap(session ->
                                        setRepository.findAllBySessionIdOrderBySetNumber(session.getId())
                                                .collectList()
                                                .map(sets -> toSessionResponse(session, sets)))
                                .collectList(),
                        sessionRepository.countByUserId(userId)
                ))
                .map(tuple -> new PagedResponse<>(tuple.getT1(), page, size, tuple.getT2()));
    }

    @Transactional
    public Mono<SetResponse> logSet(UUID sessionId, FirebaseAuthentication auth, LogSetRequest request) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> sessionRepository.findById(sessionId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Session not found: " + sessionId)))
                        .flatMap(session -> {
                            if (!session.getUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException("Access denied"));
                            }
                            WorkoutSet set = new WorkoutSet();
                            set.setSessionId(sessionId);
                            set.setExerciseId(request.exerciseId());
                            set.setSetNumber(request.setNumber());
                            set.setReps(request.reps());
                            set.setWeightKg(request.weightKg());
                            set.setCompletedAt(Instant.now());
                            return setRepository.save(set);
                        })
                )
                .map(this::toSetResponse);
    }

    @Transactional
    public Mono<SessionResponse> completeSession(UUID sessionId, FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> sessionRepository.findById(sessionId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Session not found: " + sessionId)))
                        .flatMap(session -> {
                            if (!session.getUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException("Access denied"));
                            }
                            if (session.getCompletedAt() != null) {
                                return Mono.error(new IllegalArgumentException("Session is already completed"));
                            }
                            session.setCompletedAt(Instant.now());
                            return sessionRepository.save(session);
                        })
                        .flatMap(session ->
                                setRepository.findAllBySessionIdOrderBySetNumber(session.getId())
                                        .collectList()
                                        .map(sets -> toSessionResponse(session, sets)))
                );
    }

    public Mono<MetricsResponse> getMetrics(FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> {
                    Instant weekStart = LocalDate.now(ZoneOffset.UTC)
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant();

                    Mono<Long> weeklySessionsMono = sessionRepository.countByUserIdAndStartedAtAfter(userId, weekStart);
                    Mono<BigDecimal> weeklyVolumeMono = setRepository.sumVolumeByUserIdAndStartedAtAfter(userId, weekStart);
                    Mono<List<LocalDate>> sessionDatesMono = sessionRepository.findDistinctSessionDatesByUserId(userId)
                            .collectList();
                    Mono<Map<UUID, BigDecimal>> prsMono = setRepository.findPersonalRecordsByUserId(userId)
                            .collectMap(PersonalRecordProjection::getExerciseId,
                                    PersonalRecordProjection::getMaxWeightKg);

                    return Mono.zip(weeklySessionsMono, weeklyVolumeMono, sessionDatesMono, prsMono)
                            .map(tuple -> new MetricsResponse(
                                    tuple.getT1(),
                                    tuple.getT2() != null ? tuple.getT2() : BigDecimal.ZERO,
                                    calculateStreak(tuple.getT3()),
                                    tuple.getT4()
                            ));
                });
    }

    private int calculateStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);

        LocalDate first = dates.get(0);
        if (!first.isEqual(today) && !first.isEqual(yesterday)) return 0;

        int streak = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).isEqual(dates.get(i - 1).minusDays(1))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private SessionResponse toSessionResponse(WorkoutSession session, List<WorkoutSet> sets) {
        List<SetResponse> setResponses = sets.stream().map(this::toSetResponse).collect(Collectors.toList());
        return new SessionResponse(
                session.getId(), session.getName(), session.getStartedAt(),
                session.getCompletedAt(), session.getNotes(), setResponses
        );
    }

    private SetResponse toSetResponse(WorkoutSet set) {
        return new SetResponse(
                set.getId(), set.getExerciseId(), set.getSetNumber(),
                set.getReps(), set.getWeightKg(), set.getCompletedAt()
        );
    }
}
