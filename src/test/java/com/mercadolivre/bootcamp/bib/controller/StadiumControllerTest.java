package com.mercadolivre.bootcamp.bib.controller;

import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.exception.ConflictException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.service.StadiumService;
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
class StadiumControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StadiumService stadiumService;

    private UUID stadiumId;
    private Stadium activeStadium;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new StadiumController(stadiumService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        stadiumId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        activeStadium = Stadium.builder()
                .id(stadiumId)
                .name("Maracanã")
                .city("Rio de Janeiro")
                .state(StateEnum.RJ)
                .active(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/stadiums
    // -------------------------------------------------------------------------

    @Test
    void create_whenValidBody_returns201WithStadium() throws Exception {
        when(stadiumService.createStadium(any())).thenReturn(activeStadium);

        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maracanã", "city": "Rio de Janeiro", "state": "RJ"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(stadiumId.toString()))
                .andExpect(jsonPath("$.name").value("Maracanã"))
                .andExpect(jsonPath("$.city").value("Rio de Janeiro"))
                .andExpect(jsonPath("$.state").value("RJ"));
    }

    @Test
    void create_whenNameIsBlank_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "city": "Rio de Janeiro", "state": "RJ"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages[0]", containsString("name")));
    }

    @Test
    void create_whenCityIsMissing_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maracanã", "state": "RJ"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages[0]", containsString("city")));
    }

    @Test
    void create_whenStateIsInvalidEnum_returns400() throws Exception {
        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maracanã", "city": "Rio de Janeiro", "state": "XX"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid JSON format or enum value"));
    }

    @Test
    void create_whenAlreadyExists_returns409() throws Exception {
        when(stadiumService.createStadium(any()))
                .thenThrow(new ConflictException("Stadium with name 'Maracanã' already exists in Rio de Janeiro, RJ"));

        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maracanã", "city": "Rio de Janeiro", "state": "RJ"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.messages[0]", containsString("Maracanã")));
    }

    // -------------------------------------------------------------------------
    // GET /api/stadiums
    // -------------------------------------------------------------------------

    @Test
    void findAll_returns200WithPage() throws Exception {
        when(stadiumService.findAll(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(activeStadium)));

        mockMvc.perform(get("/api/stadiums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Maracanã"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // -------------------------------------------------------------------------
    // GET /api/stadiums/{id}
    // -------------------------------------------------------------------------

    @Test
    void findById_whenStadiumExists_returns200() throws Exception {
        when(stadiumService.findById(stadiumId)).thenReturn(activeStadium);

        mockMvc.perform(get("/api/stadiums/{id}", stadiumId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stadiumId.toString()))
                .andExpect(jsonPath("$.name").value("Maracanã"))
                .andExpect(jsonPath("$.city").value("Rio de Janeiro"))
                .andExpect(jsonPath("$.state").value("RJ"));
    }

    @Test
    void findById_whenStadiumNotFound_returns404() throws Exception {
        when(stadiumService.findById(stadiumId))
                .thenThrow(new ResourceNotFoundException("Stadium not found with ID: " + stadiumId));

        mockMvc.perform(get("/api/stadiums/{id}", stadiumId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.path").value("/api/stadiums/" + stadiumId));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/stadiums/{id}
    // -------------------------------------------------------------------------

    @Test
    void delete_whenStadiumExists_returns204() throws Exception {
        doNothing().when(stadiumService).deleteStadium(stadiumId);

        mockMvc.perform(delete("/api/stadiums/{id}", stadiumId))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenStadiumNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Stadium not found with ID: " + stadiumId))
                .when(stadiumService).deleteStadium(stadiumId);

        mockMvc.perform(delete("/api/stadiums/{id}", stadiumId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
