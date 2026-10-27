package com.example.blue_hawk.application.usecase.match;

import com.example.blue_hawk.application.dto.match.CreateMatchCommand;
import com.example.blue_hawk.application.dto.match.CreateMatchOutput;
import com.example.blue_hawk.application.dto.match.GetMatchOutput;
import com.example.blue_hawk.application.dto.match.GetMatchQuery;
import com.example.blue_hawk.application.dto.match.ListMatchOutput;
import com.example.blue_hawk.application.dto.match.ListMatchQuery;
import com.example.blue_hawk.application.dto.match.UpdateMatchCommand;
import com.example.blue_hawk.application.dto.match.UpdateMatchOutput;
import com.example.blue_hawk.domain.entity.boardgame.Boardgame;
import com.example.blue_hawk.domain.entity.match.Match;
import com.example.blue_hawk.domain.entity.userboardgame.UserBoardgame;
import com.example.blue_hawk.domain.repository.IBoardgameRepository;
import com.example.blue_hawk.domain.repository.IMatchRepository;
import com.example.blue_hawk.domain.repository.IUserBoardGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchUseCaseImplTest {

    @Mock
    private IMatchRepository matchRepository;

    @Mock
    private IUserBoardGameRepository userBoardGameRepository;

    @Mock
    private IBoardgameRepository boardgameRepository;

    private CreateMatchUseCaseImpl createMatchUseCase;
    private GetMatchUseCaseImpl getMatchUseCase;
    private UpdateMatchUseCaseImpl updateMatchUseCase;
    private ListMatchUseCaseImpl listMatchUseCase;

    private UUID userBoardgameId;
    private UUID boardgameId;
    private UserBoardgame userBoardgame;
    private Boardgame boardgame;

    @BeforeEach
    void setUp() {
        createMatchUseCase = new CreateMatchUseCaseImpl(matchRepository, userBoardGameRepository, boardgameRepository);
        getMatchUseCase = new GetMatchUseCaseImpl(matchRepository, userBoardGameRepository, boardgameRepository);
        updateMatchUseCase = new UpdateMatchUseCaseImpl(matchRepository, userBoardGameRepository, boardgameRepository);
        listMatchUseCase = new ListMatchUseCaseImpl(matchRepository, userBoardGameRepository, boardgameRepository);

        userBoardgameId = UUID.randomUUID();
        boardgameId = UUID.randomUUID();
        userBoardgame = new UserBoardgame(userBoardgameId, UUID.randomUUID(), boardgameId);
        boardgame = new Boardgame(boardgameId, "Catan", "Trade build settle", 1995, 4, 90);
    }

    @Test
    void shouldCreateMatchAndGetMaxUsersFromBoardgame() {
        when(userBoardGameRepository.findById(userBoardgameId)).thenReturn(Optional.of(userBoardgame));
        when(boardgameRepository.findById(boardgameId)).thenReturn(Optional.of(boardgame));
        when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

        CreateMatchOutput output = createMatchUseCase.handle(new CreateMatchCommand(userBoardgameId.toString()));

        assertNotNull(output.id());
        assertEquals(userBoardgameId.toString(), output.userBoardgameId());
        assertEquals(4, output.maxUsers());
    }

    @Test
    void shouldGetMatchAndReturnMaxUsersFromBoardgame() {
        UUID matchId = UUID.randomUUID();
        Match match = new Match(matchId, userBoardgameId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userBoardGameRepository.findById(userBoardgameId)).thenReturn(Optional.of(userBoardgame));
        when(boardgameRepository.findById(boardgameId)).thenReturn(Optional.of(boardgame));

        GetMatchOutput output = getMatchUseCase.handle(new GetMatchQuery(matchId.toString()));

        assertEquals(matchId.toString(), output.id());
        assertEquals(userBoardgameId.toString(), output.userBoardgameId());
        assertEquals(4, output.maxUsers());
    }

    @Test
    void shouldUpdateMatchAndReturnMaxUsersFromBoardgame() {
        UUID matchId = UUID.randomUUID();
        Match match = new Match(matchId, userBoardgameId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userBoardGameRepository.findById(userBoardgameId)).thenReturn(Optional.of(userBoardgame));
        when(boardgameRepository.findById(boardgameId)).thenReturn(Optional.of(boardgame));
        when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

        UpdateMatchOutput output = updateMatchUseCase.handle(
                new UpdateMatchCommand(matchId.toString(), userBoardgameId.toString())
        );

        assertEquals(matchId.toString(), output.id());
        assertEquals(userBoardgameId.toString(), output.userBoardgameId());
        assertEquals(4, output.maxUsers());
    }

    @Test
    void shouldListMatchesWithMaxUsersFromBoardgame() {
        UUID matchId = UUID.randomUUID();
        Match match = new Match(matchId, userBoardgameId);

        when(matchRepository.findAll(userBoardgameId, 0, 10))
                .thenReturn(new PageImpl<>(List.of(match)));
        when(userBoardGameRepository.findById(userBoardgameId)).thenReturn(Optional.of(userBoardgame));
        when(boardgameRepository.findById(boardgameId)).thenReturn(Optional.of(boardgame));

        List<ListMatchOutput> outputs = listMatchUseCase.handle(
                new ListMatchQuery(userBoardgameId.toString(), 0, 10)
        );

        assertEquals(1, outputs.size());
        assertEquals(4, outputs.get(0).maxUsers());
    }
}
