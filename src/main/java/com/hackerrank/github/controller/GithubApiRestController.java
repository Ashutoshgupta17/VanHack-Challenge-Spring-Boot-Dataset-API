package com.hackerrank.github.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class GithubApiRestController {

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