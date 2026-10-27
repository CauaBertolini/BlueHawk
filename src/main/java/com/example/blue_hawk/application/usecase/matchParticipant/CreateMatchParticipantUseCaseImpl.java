package com.example.blue_hawk.application.usecase.matchParticipant;

import com.example.blue_hawk.application.dto.matchParticipant.CreateMatchParticipantCommand;
import com.example.blue_hawk.application.dto.matchParticipant.CreateMatchParticipantOutput;
import com.example.blue_hawk.domain.entity.boardgame.Boardgame;
import com.example.blue_hawk.domain.entity.match.Match;
import com.example.blue_hawk.domain.entity.matchparticipant.MatchParticipant;
import com.example.blue_hawk.domain.entity.userboardgame.UserBoardgame;
import com.example.blue_hawk.domain.repository.IBoardgameRepository;
import com.example.blue_hawk.domain.repository.IMatchParticipantRepository;
import com.example.blue_hawk.domain.repository.IMatchRepository;
import com.example.blue_hawk.domain.repository.IUserBoardGameRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateMatchParticipantUseCaseImpl implements CreateMatchParticipantUseCase {

    private final IMatchParticipantRepository matchParticipantRepository;
    private final IMatchRepository matchRepository;
    private final IUserBoardGameRepository userBoardGameRepository;
    private final IBoardgameRepository boardgameRepository;

    public CreateMatchParticipantUseCaseImpl(IMatchParticipantRepository matchParticipantRepository,
                                             IMatchRepository matchRepository,
                                             IUserBoardGameRepository userBoardGameRepository,
                                             IBoardgameRepository boardgameRepository) {
        this.matchParticipantRepository = matchParticipantRepository;
        this.matchRepository = matchRepository;
        this.userBoardGameRepository = userBoardGameRepository;
        this.boardgameRepository = boardgameRepository;
    }

    @Override
    public CreateMatchParticipantOutput handle(CreateMatchParticipantCommand command) {
        UUID matchId = UUID.fromString(command.matchId());

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + command.matchId()));

        UserBoardgame userBoardgame = userBoardGameRepository.findById(match.getUserBoardgameId())
                .orElseThrow(() -> new RuntimeException("UserBoardgame not found with id: " + match.getUserBoardgameId()));

        Boardgame boardgame = boardgameRepository.findById(userBoardgame.getBoardgameId())
                .orElseThrow(() -> new RuntimeException("Boardgame not found with id: " + userBoardgame.getBoardgameId()));

        long currentParticipants = matchParticipantRepository.countByMatchId(matchId);
        if (boardgame.getPlayerNumber() != null && currentParticipants >= boardgame.getPlayerNumber()) {
            throw new RuntimeException("Match has reached maximum player limit: " + boardgame.getPlayerNumber());
        }

        MatchParticipant matchParticipant = new MatchParticipant(
                command.matchId(),
                command.userId()
        );

        MatchParticipant saved = matchParticipantRepository.save(matchParticipant);

        return new CreateMatchParticipantOutput(
                saved.getId().toString(),
                saved.getMatchId().toString(),
                saved.getUserId().toString()
        );
    }
}
