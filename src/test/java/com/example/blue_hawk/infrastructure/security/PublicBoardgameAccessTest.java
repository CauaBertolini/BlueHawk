package com.example.blue_hawk.infrastructure.security;

import com.example.blue_hawk.application.dto.boardgame.ListBoardgameOutput;
import com.example.blue_hawk.application.usecase.boardgame.CreateBoardgameUseCase;
import com.example.blue_hawk.application.usecase.boardgame.DeleteBoardgameUseCase;
import com.example.blue_hawk.application.usecase.boardgame.GetBoardgameUseCase;
import com.example.blue_hawk.application.usecase.boardgame.ListBoardgameUseCase;
import com.example.blue_hawk.application.usecase.boardgame.UpdateBoardgameUseCase;
import com.example.blue_hawk.presentation.boardgame.BoardgameController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = BoardgameController.class,
        properties = {
                "security.jwt.secret=test-secret-with-at-least-32-characters!!",
                "security.jwt.expiration-seconds=3600",
                "app.cors.allowed-origins=http://localhost:5173"
        })
@Import({SecurityConfig.class, JwtTokenService.class, TokenBlacklistService.class})
class PublicBoardgameAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateBoardgameUseCase createBoardgameUseCase;
    @MockitoBean
    private GetBoardgameUseCase getBoardgameUseCase;
    @MockitoBean
    private ListBoardgameUseCase listBoardgameUseCase;
    @MockitoBean
    private UpdateBoardgameUseCase updateBoardgameUseCase;
    @MockitoBean
    private DeleteBoardgameUseCase deleteBoardgameUseCase;

    @Test
    void shouldListBoardgamesWithoutToken() throws Exception {
        when(listBoardgameUseCase.handle(any())).thenReturn(List.of(
                new ListBoardgameOutput("1", "Marés de Ferro", "Estratégia naval", 2024, 4, 90)));

        mockMvc.perform(get("/boardgames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Marés de Ferro"));
    }

    @Test
    void shouldKeepBoardgameDetailProtected() throws Exception {
        mockMvc.perform(get("/boardgames/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldKeepBoardgameCreationProtected() throws Exception {
        mockMvc.perform(post("/boardgames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novo\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAnswerPreflightFromAllowedOrigin() throws Exception {
        mockMvc.perform(options("/boardgames")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    void shouldRejectPreflightFromUnknownOrigin() throws Exception {
        mockMvc.perform(options("/boardgames")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
