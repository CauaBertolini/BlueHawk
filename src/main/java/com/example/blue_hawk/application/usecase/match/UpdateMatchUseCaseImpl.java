package com.example.blue_hawk.application.usecase.match;

import com.example.blue_hawk.application.dto.match.UpdateMatchCommand;
import com.example.blue_hawk.application.dto.match.UpdateMatchOutput;
import com.example.blue_hawk.domain.entity.boardgame.Boardgame;
import com.example.blue_hawk.domain.entity.match.Match;
import com.example.blue_hawk.domain.entity.userboardgame.UserBoardgame;
import com.example.blue_hawk.domain.repository.IBoardgameRepository;
import com.example.blue_hawk.domain.repository.IMatchRepository;
import com.example.blue_hawk.domain.repository.IUserBoardGameRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateMatchUseCaseImpl implements UpdateMatchUseCase {

    private final IMatchRepository matchRepository;
    private final IUserBoardGameRepository userBoardGameRepository;
    private final IBoardgameRepository boardgameRepository;

    public UpdateMatchUseCaseImpl(IMatchRepository matchRepository,
                                  IUserBoardGameRepository userBoardGameRepository,
                                  IBoardgameRepository boardgameRepository) {
        this.matchRepository = matchRepository;
        this.userBoardGameRepository = userBoardGameRepository;
        this.boardgameRepository = boardgameRepository;
    }

    @Override
    public UpdateMatchOutput handle(UpdateMatchCommand command) {
        Match match = matchRepository
                .findById(UUID.fromString(command.id()))
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + command.id()));

        UUID userBoardgameId = UUID.fromString(command.userBoardgameId());
        UserBoardgame userBoardgame = userBoardGameRepository.findById(userBoardgameId)
                .orElseThrow(() -> new RuntimeException("UserBoardgame not found with id: " + userBoardgameId));

        Boardgame boardgame = boardgameRepository.findById(userBoardgame.getBoardgameId())
                .orElseThrow(() -> new RuntimeException("Boardgame not found with id: " + userBoardgame.getBoardgameId()));

        match.setUserBoardgameId(userBoardgameId);

        Match updated = matchRepository.save(match);

        return new UpdateMatchOutput(
                updated.getId().toString(),
                updated.getUserBoardgameId().toString(),
                boardgame.getPlayerNumber()
        );
    }
}
