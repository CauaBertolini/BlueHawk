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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMatchParticipantUseCaseImplTest {

    @Mock
    private IMatchParticipantRepository matchParticipantRepository;

    @Mock
    private IMatchRepository matchRepository;

    @Mock
    private IUserBoardGameRepository userBoardGameRepository;

    @Mock
    private IBoardgameRepository boardgameRepository;

    @InjectMocks
    private CreateMatchParticipantUseCaseImpl useCase;

    private UUID matchId;
    private UUID userId;
    private UUID userBoardgameId;
    private UUID boardgameId;
    private Match match;
    private UserBoardgame userBoardgame;
    private Boardgame boardgame;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userBoardgameId = UUID.randomUUID();
        boardgameId = UUID.randomUUID();

        match = new Match(matchId, userBoardgameId);
        userBoardgame = new UserBoardgame(userBoardgameId, UUID.randomUUID(), boardgameId);
        boardgame = new Boardgame(boardgameId, "Catan", "Trade build settle", 1995, 4, 90);
    }

    @Test
    void shouldAddParticipantWhenUnderPlayerLimit() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userBoardGameRepository.findById(userBoardgameId)).thenReturn(Optional.of(userBoardgame));
        when(boardgameRepository.findById(boardgameId)).thenReturn(Optional.of(boardgame));
        when(matchParticipantRepository.countByMatchId(matchId)).thenReturn(3L);
        when(matchParticipantRepository.save(any(MatchParticipant.class))).thenAnswer(i -> i.getArgument(0));

        CreateMatchParticipantOutput output = useCase.handle(
                new CreateMatchParticipantCommand(matchId.toString(), userId.toString())
        );

        assertNotNull(output.id());
        assertEquals(matchId.toString(), output.matchId());
        assertEquals(userId.toString(), output.userId());
        verify(matchParticipantRepository).save(any(MatchParticipant.class));
    }

    @Test
    void shouldThrowExceptionWhenMatchReachesPlayerLimit() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userBoardGameRepository.findById(userBoardgameId)).thenReturn(Optional.of(userBoardgame));
        when(boardgameRepository.findById(boardgameId)).thenReturn(Optional.of(boardgame));
        when(matchParticipantRepository.countByMatchId(matchId)).thenReturn(4L);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                useCase.handle(new CreateMatchParticipantCommand(matchId.toString(), userId.toString()))
        );

        assertEquals("Match has reached maximum player limit: 4", ex.getMessage());
        verify(matchParticipantRepository, never()).save(any());
    }
}
