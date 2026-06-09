package com.mercadolivre.bootcamp.bib.controller;

import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectConfrontResponseDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.exception.BusinessRuleException;
import com.mercadolivre.bootcamp.bib.exception.ConflictException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.service.ClubService;
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

import java.time.LocalDate;
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
class ClubControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClubService clubService;

    @Mock
    private MatchService matchService;

    private UUID clubId;
    private UUID adversaryId;
    private Club activeClub;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ClubController(clubService, matchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        clubId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        adversaryId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        activeClub = Club.builder()
                .id(clubId)
                .name("Flamengo")
                .state(StateEnum.RJ)
                .foundationDate(LocalDate.of(1895, 11, 17))
                .active(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/clubs
    // -------------------------------------------------------------------------

    @Test
    void create_whenValidBody_returns201WithClub() throws Exception {
        when(clubService.createClub(any())).thenReturn(activeClub);

        mockMvc.perform(post("/api/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Flamengo", "state": "RJ", "foundationDate": "1895-11-17"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(clubId.toString()))
                .andExpect(jsonPath("$.name").value("Flamengo"))
                .andExpect(jsonPath("$.state").value("RJ"));
    }

    @Test
    void create_whenNameIsBlank_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "state": "RJ", "foundationDate": "1895-11-17"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages[0]", containsString("name")));
    }

    @Test
    void create_whenStateIsMissing_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Flamengo", "foundationDate": "1895-11-17"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages[0]", containsString("state")));
    }

    @Test
    void create_whenStateIsInvalidEnum_returns400() throws Exception {
        mockMvc.perform(post("/api/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Flamengo", "state": "XX"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid JSON format or enum value"));
    }

    @Test
    void create_whenNameAlreadyExists_returns409() throws Exception {
        when(clubService.createClub(any()))
                .thenThrow(new ConflictException("Club with name 'Flamengo' already exists."));

        mockMvc.perform(post("/api/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Flamengo", "state": "RJ", "foundationDate": "1895-11-17"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.messages[0]", containsString("Flamengo")));
    }

    // -------------------------------------------------------------------------
    // GET /api/clubs
    // -------------------------------------------------------------------------

    @Test
    void findAll_returns200WithPage() throws Exception {
        when(clubService.findAll(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(activeClub)));

        mockMvc.perform(get("/api/clubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Flamengo"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // -------------------------------------------------------------------------
    // GET /api/clubs/{id}
    // -------------------------------------------------------------------------

    @Test
    void findById_whenClubExists_returns200() throws Exception {
        when(clubService.findById(clubId)).thenReturn(activeClub);

        mockMvc.perform(get("/api/clubs/{id}", clubId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubId.toString()))
                .andExpect(jsonPath("$.name").value("Flamengo"))
                .andExpect(jsonPath("$.state").value("RJ"));
    }

    @Test
    void findById_whenClubNotFound_returns404() throws Exception {
        when(clubService.findById(clubId))
                .thenThrow(new ResourceNotFoundException("Club not found or inactive with ID: " + clubId));

        mockMvc.perform(get("/api/clubs/{id}", clubId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.path").value("/api/clubs/" + clubId));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/clubs/{id}
    // -------------------------------------------------------------------------

    @Test
    void delete_whenClubExists_returns204() throws Exception {
        doNothing().when(clubService).deleteClub(clubId);

        mockMvc.perform(delete("/api/clubs/{id}", clubId))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenClubNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Club not found or inactive with ID: " + clubId))
                .when(clubService).deleteClub(clubId);

        mockMvc.perform(delete("/api/clubs/{id}", clubId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------------------------------------------------------------------------
    // GET /api/clubs/{id}/retrospect
    // -------------------------------------------------------------------------

    @Test
    void retrospect_whenClubExists_returns200WithStats() throws Exception {
        RetrospectResponseDTO dto = new RetrospectResponseDTO(clubId, "Flamengo", 5, 2, 1, 15, 7, 8);
        when(matchService.generalRetrospectCalculate(clubId)).thenReturn(dto);

        mockMvc.perform(get("/api/clubs/{id}/retrospect", clubId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(clubId.toString()))
                .andExpect(jsonPath("$.clubName").value("Flamengo"))
                .andExpect(jsonPath("$.victories").value(5))
                .andExpect(jsonPath("$.draws").value(2))
                .andExpect(jsonPath("$.losses").value(1));
    }

    @Test
    void retrospect_whenClubNotFound_returns404() throws Exception {
        when(matchService.generalRetrospectCalculate(clubId))
                .thenThrow(new ResourceNotFoundException("Club not found with ID: " + clubId));

        mockMvc.perform(get("/api/clubs/{id}/retrospect", clubId))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/clubs/{id}/retrospect/{adversaryId}
    // -------------------------------------------------------------------------

    @Test
    void confrontRetrospect_whenBothClubsExist_returns200WithStats() throws Exception {
        RetrospectConfrontResponseDTO dto = new RetrospectConfrontResponseDTO(
                clubId, "Flamengo", adversaryId, "Vasco", 3L, 1L, 1L, 9L, 4L, 5L, 5L, List.of());
        when(matchService.confrontRetrospectCalculate(clubId, adversaryId)).thenReturn(dto);

        mockMvc.perform(get("/api/clubs/{id}/retrospect/{adversaryId}", clubId, adversaryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubName").value("Flamengo"))
                .andExpect(jsonPath("$.adversaryClubName").value("Vasco"))
                .andExpect(jsonPath("$.totalMatches").value(5));
    }

    @Test
    void confrontRetrospect_whenSameClubIds_returns422() throws Exception {
        when(matchService.confrontRetrospectCalculate(clubId, adversaryId))
                .thenThrow(new BusinessRuleException("Club ID and adversary club ID cannot be the same."));

        mockMvc.perform(get("/api/clubs/{id}/retrospect/{adversaryId}", clubId, adversaryId))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Business Rule Violation"));
    }
}
