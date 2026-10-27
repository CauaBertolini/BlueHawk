package com.example.blue_hawk.infrastructure.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BoardgameModel {
    private UUID id;

    private String name;

    private String description;

    private int releaseYear;

    private Integer playerNumber;

    private int playTime;
}
