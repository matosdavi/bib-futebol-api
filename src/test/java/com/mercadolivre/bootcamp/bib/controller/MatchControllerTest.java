package com.mercadolivre.bootcamp.bib.controller;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.entity.Match;
import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.exception.BusinessRuleException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MatchService matchService;

    private UUID matchId;
    private UUID homeClubId;
    private UUID awayClubId;
    private UUID stadiumId;
    private Match savedMatch;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MatchController(matchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        matchId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        homeClubId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        awayClubId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        stadiumId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        Club homeClub = Club.builder().id(homeClubId).name("Flamengo").state(StateEnum.RJ).active(true).build();
        Club awayClub = Club.builder().id(awayClubId).name("Vasco").state(StateEnum.RJ).active(true).build();
        Stadium stadium = Stadium.builder().id(stadiumId).name("Maracanã").city("Rio de Janeiro").state(StateEnum.RJ).active(true).build();

        savedMatch = Match.builder()
                .id(matchId)
                .homeClubId(homeClub)
                .awayClubId(awayClub)
                .stadiumId(stadium)
                .homeClubGoals(2)
                .awayClubGoals(1)
                .matchDateTime(LocalDateTime.of(2024, 6, 15, 16, 0))
                .build();
    }

    private String validMatchJson() {
        return """
                {
                  "homeClubId": "%s",
                  "awayClubId": "%s",
                  "homeClubGoals": 2,
                  "awayClubGoals": 1,
                  "stadiumId": "%s",
                  "matchDateTime": "2024-06-15T16:00:00"
                }
                """.formatted(homeClubId, awayClubId, stadiumId);
    }

    // -------------------------------------------------------------------------
    // POST /api/matches
    // -------------------------------------------------------------------------

    @Test
    void create_whenValidBody_returns201WithMatch() throws Exception {
        when(matchService.createMatch(any())).thenReturn(savedMatch);

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMatchJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(matchId.toString()))
                .andExpect(jsonPath("$.homeClub.name").value("Flamengo"))
                .andExpect(jsonPath("$.awayClub.name").value("Vasco"))
                .andExpect(jsonPath("$.homeClubGoals").value(2))
                .andExpect(jsonPath("$.awayClubGoals").value(1))
                .andExpect(jsonPath("$.stadium.name").value("Maracanã"));
    }

    @Test
    void create_whenHomeClubIdIsMissing_returns400WithFieldError() throws Exception {
        String body = """
                {
                  "awayClubId": "%s",
                  "homeClubGoals": 2,
                  "awayClubGoals": 1,
                  "stadiumId": "%s",
                  "matchDateTime": "2024-06-15T16:00:00"
                }
                """.formatted(awayClubId, stadiumId);

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages[0]", containsString("homeClubId")));
    }

    @Test
    void create_whenGoalsAreNegative_returns400WithFieldError() throws Exception {
        String body = """
                {
                  "homeClubId": "%s",
                  "awayClubId": "%s",
                  "homeClubGoals": -1,
                  "awayClubGoals": 1,
                  "stadiumId": "%s",
                  "matchDateTime": "2024-06-15T16:00:00"
                }
                """.formatted(homeClubId, awayClubId, stadiumId);

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages[0]", containsString("homeClubGoals")));
    }

    @Test
    void create_whenBodyIsMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid JSON format or enum value"));
    }

    @Test
    void create_whenHomeAndAwayClubAreTheSame_returns422() throws Exception {
        when(matchService.createMatch(any()))
                .thenThrow(new BusinessRuleException("Home club and away club cannot be the same."));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMatchJson()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Business Rule Violation"))
                .andExpect(jsonPath("$.messages[0]", containsString("same")));
    }

    @Test
    void create_whenClubNotFound_returns404() throws Exception {
        when(matchService.createMatch(any()))
                .thenThrow(new ResourceNotFoundException("Home club not found with ID: " + homeClubId));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMatchJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    // -------------------------------------------------------------------------
    // GET /api/matches
    // -------------------------------------------------------------------------

    @Test
    void findAll_returns200WithPage() throws Exception {
        when(matchService.findAll(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(savedMatch)));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].homeClub.name").value("Flamengo"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // -------------------------------------------------------------------------
    // GET /api/matches/{id}
    // -------------------------------------------------------------------------

    @Test
    void findById_whenMatchExists_returns200() throws Exception {
        when(matchService.findById(matchId)).thenReturn(savedMatch);

        mockMvc.perform(get("/api/matches/{id}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchId.toString()))
                .andExpect(jsonPath("$.homeClubGoals").value(2))
                .andExpect(jsonPath("$.awayClubGoals").value(1));
    }

    @Test
    void findById_whenMatchNotFound_returns404() throws Exception {
        when(matchService.findById(matchId))
                .thenThrow(new ResourceNotFoundException("Match not found with ID: " + matchId));

        mockMvc.perform(get("/api/matches/{id}", matchId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.path").value("/api/matches/" + matchId));
    }

    // -------------------------------------------------------------------------
    // GET /api/matches/blowouts
    // -------------------------------------------------------------------------

    @Test
    void findBlowouts_returns200WithList() throws Exception {
        when(matchService.findBlowouts(any())).thenReturn(new PageImpl<>(List.of(savedMatch)));

        mockMvc.perform(get("/api/matches/blowouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].homeClub.name").value("Flamengo"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/matches/{id}
    // -------------------------------------------------------------------------

    @Test
    void delete_whenMatchExists_returns204() throws Exception {
        doNothing().when(matchService).deleteMatch(matchId);

        mockMvc.perform(delete("/api/matches/{id}", matchId))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMatchNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Match not found with ID: " + matchId))
                .when(matchService).deleteMatch(matchId);

        mockMvc.perform(delete("/api/matches/{id}", matchId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
