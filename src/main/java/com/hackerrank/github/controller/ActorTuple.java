package com.hackerrank.github.controller;

import com.hackerrank.github.model.Actor;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
class ActorTuple {
    Actor actor;
    Integer cEvents;
    Timestamp last;
}
