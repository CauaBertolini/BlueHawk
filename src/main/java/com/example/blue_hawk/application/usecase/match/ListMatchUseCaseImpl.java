package com.example.blue_hawk.application.usecase.match;

import com.example.blue_hawk.application.dto.match.ListMatchOutput;
import com.example.blue_hawk.application.dto.match.ListMatchQuery;
import com.example.blue_hawk.domain.entity.boardgame.Boardgame;
import com.example.blue_hawk.domain.entity.userboardgame.UserBoardgame;
import com.example.blue_hawk.domain.repository.IBoardgameRepository;
import com.example.blue_hawk.domain.repository.IMatchRepository;
import com.example.blue_hawk.domain.repository.IUserBoardGameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListMatchUseCaseImpl implements ListMatchUseCase {

    private final IMatchRepository matchRepository;
    private final IUserBoardGameRepository userBoardGameRepository;
    private final IBoardgameRepository boardgameRepository;

    public ListMatchUseCaseImpl(IMatchRepository matchRepository,
                                IUserBoardGameRepository userBoardGameRepository,
                                IBoardgameRepository boardgameRepository) {
        this.matchRepository = matchRepository;
        this.userBoardGameRepository = userBoardGameRepository;
        this.boardgameRepository = boardgameRepository;
    }

    @Override
    public List<ListMatchOutput> handle(ListMatchQuery query) {
        UUID userBoardgameId = query.userBoardgameId() != null ? UUID.fromString(query.userBoardgameId()) : null;

        return matchRepository
                .findAll(userBoardgameId, query.page(), query.size())
                .getContent()
                .stream()
                .map(m -> {
                    Integer maxUsers = userBoardGameRepository.findById(m.getUserBoardgameId())
                            .flatMap(ub -> boardgameRepository.findById(ub.getBoardgameId()))
                            .map(Boardgame::getPlayerNumber)
                            .orElse(null);

                    return new ListMatchOutput(
                            m.getId().toString(),
                            m.getUserBoardgameId().toString(),
                            maxUsers);
                })
                .toList();
    }
}
