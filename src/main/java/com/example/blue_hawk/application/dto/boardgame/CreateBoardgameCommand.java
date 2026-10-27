package com.example.blue_hawk.application.dto.boardgame;

public record CreateBoardgameCommand(
        String name,
        String description,
        Integer releaseYear,
        Integer playerNumber,
        Integer playTime
) {
}
