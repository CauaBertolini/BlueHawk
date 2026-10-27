package com.example.blue_hawk.domain.entity.match;

import java.util.UUID;

public class Match {

    private UUID id;
    private UUID userBoardgameId;

    public Match(String userBoardgameId) {
        this.id = UUID.randomUUID();
        this.userBoardgameId = UUID.fromString(userBoardgameId);
    }

    public Match(UUID id, UUID userBoardgameId) {
        this.id = id;
        this.userBoardgameId = userBoardgameId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserBoardgameId() {
        return userBoardgameId;
    }

    public void setUserBoardgameId(UUID userBoardgameId) {
        this.userBoardgameId = userBoardgameId;
    }
}
