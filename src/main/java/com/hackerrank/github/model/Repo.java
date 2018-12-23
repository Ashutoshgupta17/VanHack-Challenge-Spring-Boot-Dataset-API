package com.hackerrank.github.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Repo {

    @Id
    private Long id;
    private String name;
    private String url;
}
