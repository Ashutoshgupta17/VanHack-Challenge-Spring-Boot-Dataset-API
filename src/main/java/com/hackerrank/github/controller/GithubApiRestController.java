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

    }