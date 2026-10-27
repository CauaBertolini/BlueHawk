package com.example.blue_hawk.application.usecase.match;

import com.example.blue_hawk.application.dto.match.GetMatchOutput;
import com.example.blue_hawk.application.dto.match.GetMatchQuery;
import com.example.blue_hawk.domain.entity.boardgame.Boardgame;
import com.example.blue_hawk.domain.entity.match.Match;
import com.example.blue_hawk.domain.entity.userboardgame.UserBoardgame;
import com.example.blue_hawk.domain.repository.IBoardgameRepository;
import com.example.blue_hawk.domain.repository.IMatchRepository;
import com.example.blue_hawk.domain.repository.IUserBoardGameRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetMatchUseCaseImpl implements GetMatchUseCase {

    private final IMatchRepository matchRepository;
    private final IUserBoardGameRepository userBoardGameRepository;
    private final IBoardgameRepository boardgameRepository;

    public GetMatchUseCaseImpl(IMatchRepository matchRepository,
                              IUserBoardGameRepository userBoardGameRepository,
                              IBoardgameRepository boardgameRepository) {
        this.matchRepository = matchRepository;
        this.userBoardGameRepository = userBoardGameRepository;
        this.boardgameRepository = boardgameRepository;
    }

    @Override
    public GetMatchOutput handle(GetMatchQuery query) {
        Match match = matchRepository
                .findById(UUID.fromString(query.id()))
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + query.id()));

        UserBoardgame userBoardgame = userBoardGameRepository.findById(match.getUserBoardgameId())
                .orElseThrow(() -> new RuntimeException("UserBoardgame not found with id: " + match.getUserBoardgameId()));

        Boardgame boardgame = boardgameRepository.findById(userBoardgame.getBoardgameId())
                .orElseThrow(() -> new RuntimeException("Boardgame not found with id: " + userBoardgame.getBoardgameId()));

        return new GetMatchOutput(
                match.getId().toString(),
                match.getUserBoardgameId().toString(),
                boardgame.getPlayerNumber()
        );
    }
}
