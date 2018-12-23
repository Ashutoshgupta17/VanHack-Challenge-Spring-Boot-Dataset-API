package com.hackerrank.github.controller;

import com.hackerrank.github.controller.dto.ActorDTO;
import com.hackerrank.github.controller.dto.EventDTO;
import com.hackerrank.github.controller.dto.RepoDTO;
import com.hackerrank.github.model.Actor;
import com.hackerrank.github.model.Event;
import com.hackerrank.github.model.Repo;
import com.hackerrank.github.repository.ActorRepository;
import com.hackerrank.github.repository.EventRepository;
import com.hackerrank.github.repository.RepoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RestController
public class GithubApiRestController {

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final ActorRepository actorRepository;
    private final RepoRepository repoRepository;

    public GithubApiRestController(EventRepository eventRepository, ActorRepository actorRepository, RepoRepository repoRepository) {
        this.eventRepository = eventRepository;
        this.actorRepository = actorRepository;
        this.repoRepository = repoRepository;
    }

    @DeleteMapping(value = "/erase")
    public ResponseEntity deleteEvents() {
        eventRepository.deleteAll();
        actorRepository.deleteAll();
        re
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/events")
    public ResponseEntity addEvent(@RequestBody EventDTO body) {

        if (Objects.nonNull(eventRepository.findOne(body.getId()))) {
            ResponseEntity.badRequest().build();
        }

        ActorDTO actorDTO = body.getActor();
        Actor actor = new Actor(actorDTO.getId(), actorDTO.getLogin(), actorDTO.getAvatar());
        actorRepository.save(actor);

        RepoDTO repoDTO = body.getRepo();
        Repo repo = new Repo(repoDTO.getId(), repoDTO.getName(), repoDTO.getUrl());
        repoRepository.save(repo);

        Timestamp timestamp;
        try {
            timestamp = new Timestamp(format.parse(body.getCreatedAt()).getTime());
        } catch (ParseException e) {
            timestamp = new Timestamp(Instant.now().toEpochMilli());
        }

        Event event = new Event(body.getId(), body.getType(), actor, repo, timestamp);
        eventRepository.save(event);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(value = "/events", produces = "application/json")
    public ResponseEntity<List<EventDTO>> getAllEvents() {

        List<Event> events = eventRepository.findAll(new Sort(Sort.Direction.ASC, "id"));

        return events.isEmpty() ?
                ResponseEntity.ok(new ArrayList<>()) :
                ResponseEntity.ok(
                        events.stream()
                                .map(EventDTO::convertFrom)
                                .collect(Collectors.toList())
                );
    }

    @GetMapping(value = "/events/actors/{actorID}", produces = "application/json")
    public ResponseEntity<List<EventDTO>> getAllEventsByActorId(@PathVariable Long actorID) {
        Actor actor = actorRepository.findOne(actorID);
        if (isNull(actor)) {
            return ResponseEntity.notFound().build();
        }
        List<Event> events = eventRepository.findAllByActorIdOrderByIdAsc(actorID);

        return events.isEmpty() ?
                ResponseEntity.ok(new ArrayList<>()) :
                ResponseEntity.ok(
                        events.stream()
                                .map(EventDTO::convertFrom)
                                .collect(Collectors.toList())
                );
    }

    @PutMapping(value = "/actors")
    public ResponseEntity<ActorDTO> updateActorAvatarURL(@RequestBody ActorDTO body) {
        Actor actor = actorRepository.findOne(body.getId());

        if (Objects.isNull(actor)) {
            return ResponseEntity.notFound().build();
        }
        if (!body.getLogin().equals(actor.getLogin())) {
            return ResponseEntity.badRequest().build();
        }
        actor.setAvatar(body.getAvatar());
        Actor actorUpdated = actorRepository.save(actor);
        return ResponseEntity.ok(ActorDTO.convertFrom(actorUpdated));
    }

    @GetMapping(value = "/actors")
    public ResponseEntity<List<ActorDTO>> getActors() {
        List<Event> events = eventRepository.findAll();
        List<Actor> actors = actorRepository.findAll();

        List<ActorTuple> actorTuples = new ArrayList<>();

        for (Actor actor : actors) {
            List<Event> collect = events.stream()
                    .filter(event -> event.getActor().equals(actor))
                    .sorted(Comparator.comparing(Event::getCreatedAt).reversed())
                    .collect(Collectors.toList());
            if (!collect.isEmpty()) {
                actorTuples.add(new ActorTuple(actor, collect.size(), collect.get(0).getCreatedAt()));
            }
        }

        List<ActorDTO> actorList = getCollectionWithCriteria(actorTuples);

        return ResponseEntity.ok(actorList);
    }

    @GetMapping(value = "/actors/streak")
    public ResponseEntity<List<ActorDTO>> getActorsStreak() {
        List<Event> events = eventRepository.findAll();
        List<Actor> actors = actorRepository.findAll();

        List<ActorTuple> actorTupleStreaks = new ArrayList<>();

        for (Actor actor : actors) {
            List<Event> collect = events.stream()
                    .filter(event -> event.getActor().equals(actor) && event.getType().equals("PushEvent"))
                    .sorted(Comparator.comparing(Event::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            if (!collect.isEmpty()) {
                if (collect.size() == 1) {
                    actorTupleStreaks.add(new ActorTuple(actor, 0, collect.get(0).getCreatedAt()));
                } else {
                    Integer mayorStreak = getStreak(collect);
                    actorTupleStreaks.add(new ActorTuple(actor, mayorStreak, collect.get(0).getCreatedAt()));
                }
            }
        }
        List<ActorDTO> actorList = getCollectionWithCriteria(actorTupleStreaks);
        return ResponseEntity.ok(actorList);
    }

    private Integer getStreak(List<Event> collect) {
        Integer mayorStreak = 0;
        Integer streak = 0;
        for (int i = collect.size() - 1; i > 0; i--) {
            LocalDateTime currentDate = collect.get(i).getCreatedAt().toLocalDateTime();
            LocalDateTime nextDate = collect.get(i - 1).getCreatedAt().toLocalDateTime();
            if (currentDate.plusDays(1).isBefore(nextDate)) {
                streak = 0;
            } else {
                streak++;
                if (streak > mayorStreak)
                    mayorStreak = streak;
            }
        }
        return mayorStreak;
    }

    private List<ActorDTO> getCollectionWithCriteria(List<ActorTuple> actorTuples) {
        return actorTuples.stream()
                .sorted(Comparator.comparing(o -> o.getActor().getLogin()))
                .sorted(Comparator.comparing(ActorTuple::getLast).reversed())
                .sorted(Comparator.comparing(ActorTuple::getCEvents).reversed())
                .map(ActorTuple::getActor)
                .map(ActorDTO::convertFrom)
                .collect(Collectors.toList());
    }
}
