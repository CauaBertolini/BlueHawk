package com.example.blue_hawk.application.usecase.match;

import com.example.blue_hawk.application.dto.match.CreateMatchCommand;
import com.example.blue_hawk.application.dto.match.CreateMatchOutput;
import com.example.blue_hawk.domain.entity.boardgame.Boardgame;
import com.example.blue_hawk.domain.entity.match.Match;
import com.example.blue_hawk.domain.entity.userboardgame.UserBoardgame;
import com.example.blue_hawk.domain.repository.IBoardgameRepository;
import com.example.blue_hawk.domain.repository.IMatchRepository;
import com.example.blue_hawk.domain.repository.IUserBoardGameRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateMatchUseCaseImpl implements CreateMatchUseCase {

    private final IMatchRepository matchRepository;
    private final IUserBoardGameRepository userBoardGameRepository;
    private final IBoardgameRepository boardgameRepository;

    public CreateMatchUseCaseImpl(IMatchRepository matchRepository,
                                  IUserBoardGameRepository userBoardGameRepository,
                                  IBoardgameRepository boardgameRepository) {
        this.matchRepository = matchRepository;
        this.userBoardGameRepository = userBoardGameRepository;
        this.boardgameRepository = boardgameRepository;
    }

    @Override
    public CreateMatchOutput handle(CreateMatchCommand command) {
        UUID userBoardgameId = UUID.fromString(command.userBoardgameId());

        UserBoardgame userBoardgame = userBoardGameRepository.findById(userBoardgameId)
                .orElseThrow(() -> new RuntimeException("UserBoardgame not found with id: " + userBoardgameId));

        Boardgame boardgame = boardgameRepository.findById(userBoardgame.getBoardgameId())
                .orElseThrow(() -> new RuntimeException("Boardgame not found with id: " + userBoardgame.getBoardgameId()));

        Match match = new Match(command.userBoardgameId());
        Match saved = matchRepository.save(match);

        return new CreateMatchOutput(
                saved.getId().toString(),
                saved.getUserBoardgameId().toString(),
                boardgame.getPlayerNumber()
        );
    }
}
