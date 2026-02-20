package com.fitnessapp.exercises;

import com.fitnessapp.common.ForbiddenException;
import com.fitnessapp.common.PagedResponse;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.common.UserContextService;
import com.fitnessapp.exercises.dto.*;
import com.fitnessapp.security.FirebaseAuthentication;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final MuscleGroupRepository muscleGroupRepository;
    private final UserContextService userContextService;

    /**
     * In-memory cache of all muscle groups keyed by ID.
     * Muscle groups are stable reference data (seeded at migration time).
     * Loaded once at startup — eliminates N+1 DB roundtrips on every exercise list/get.
     */
    private Map<UUID, MuscleGroup> muscleGroupCache = new ConcurrentHashMap<>();

    @PostConstruct
    void loadMuscleGroupCache() {
        // Blocking at startup is acceptable — we are not inside a reactive chain here.
        this.muscleGroupCache = muscleGroupRepository.findAll()
                .collectMap(MuscleGroup::getId)
                .block();
        log.info("Muscle group cache loaded: {} entries", muscleGroupCache.size());
    }

    public Mono<PagedResponse<ExerciseResponse>> listExercises(
            UUID muscleGroupId, String search, int page, int size) {

        long offset = (long) page * size;
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasFilter = muscleGroupId != null;

        Flux<Exercise> exercises;
        Mono<Long> total;

        if (hasSearch && hasFilter) {
            exercises = exerciseRepository.searchByNameAndMuscleGroup(search, muscleGroupId, size, offset);
            total = exerciseRepository.countByNameSearchAndMuscleGroup(search, muscleGroupId);
        } else if (hasSearch) {
            exercises = exerciseRepository.searchByName(search, size, offset);
            total = exerciseRepository.countByNameSearch(search);
        } else if (hasFilter) {
            exercises = exerciseRepository.findAllByMuscleGroupId(muscleGroupId,
                    PageRequest.of(page, size, Sort.by("name")));
            total = exerciseRepository.countByMuscleGroupId(muscleGroupId);
        } else {
            exercises = exerciseRepository.findAllBy(PageRequest.of(page, size, Sort.by("name")));
            total = exerciseRepository.count();
        }

        return Mono.zip(
                exercises.map(this::enrichWithMuscleGroup).collectList(),
                total
        ).map(tuple -> new PagedResponse<>(tuple.getT1(), page, size, tuple.getT2()));
    }

    public Mono<ExerciseResponse> getById(UUID id) {
        return exerciseRepository.findById(id)
                .map(this::enrichWithMuscleGroup)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Exercise not found: " + id)));
    }

    public Mono<ExerciseResponse> create(FirebaseAuthentication auth, CreateExerciseRequest request) {
        if (!muscleGroupCache.containsKey(request.muscleGroupId())) {
            return Mono.error(new ResourceNotFoundException(
                    "MuscleGroup not found: " + request.muscleGroupId()));
        }
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> {
                    Exercise exercise = new Exercise();
                    exercise.setName(request.name());
                    exercise.setDescription(request.description());
                    exercise.setMuscleGroupId(request.muscleGroupId());
                    exercise.setInstructions(request.instructions());
                    exercise.setCreatedByUserId(userId);
                    exercise.setCreatedAt(Instant.now());
                    return exerciseRepository.save(exercise);
                })
                .map(this::enrichWithMuscleGroup);
    }

    public Mono<ExerciseResponse> update(UUID id, FirebaseAuthentication auth, UpdateExerciseRequest request) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> exerciseRepository.findById(id)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Exercise not found: " + id)))
                        .flatMap(exercise -> {
                            if (exercise.getCreatedByUserId() == null ||
                                    !exercise.getCreatedByUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException(
                                        "You can only edit exercises you created"));
                            }
                            if (request.name() != null && !request.name().isBlank()) {
                                exercise.setName(request.name());
                            }
                            if (request.description() != null) {
                                exercise.setDescription(request.description());
                            }
                            if (request.instructions() != null) {
                                exercise.setInstructions(request.instructions());
                            }
                            if (request.muscleGroupId() != null) {
                                if (!muscleGroupCache.containsKey(request.muscleGroupId())) {
                                    return Mono.error(new ResourceNotFoundException(
                                            "MuscleGroup not found: " + request.muscleGroupId()));
                                }
                                exercise.setMuscleGroupId(request.muscleGroupId());
                            }
                            return exerciseRepository.save(exercise);
                        })
                )
                .map(this::enrichWithMuscleGroup);
    }

    public Mono<Void> delete(UUID id, FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> exerciseRepository.findById(id)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Exercise not found: " + id)))
                        .flatMap(exercise -> {
                            if (exercise.getCreatedByUserId() == null ||
                                    !exercise.getCreatedByUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException(
                                        "You can only delete exercises you created"));
                            }
                            return exerciseRepository.delete(exercise);
                        })
                );
    }

    public Flux<MuscleGroupResponse> listMuscleGroups() {
        return Flux.fromIterable(muscleGroupCache.values())
                .map(mg -> new MuscleGroupResponse(mg.getId(), mg.getName(), mg.getDescription()));
    }

    /**
     * Synchronous enrichment using the in-memory muscle group cache.
     * No DB roundtrip — eliminates the former N+1 query.
     */
    private ExerciseResponse enrichWithMuscleGroup(Exercise exercise) {
        MuscleGroup mg = muscleGroupCache.get(exercise.getMuscleGroupId());
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getMuscleGroupId(),
                mg != null ? mg.getName() : null,
                exercise.getInstructions(),
                exercise.getCreatedByUserId(),
                exercise.getCreatedAt()
        );
    }
}
