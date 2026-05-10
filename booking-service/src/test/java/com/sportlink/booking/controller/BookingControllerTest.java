package com.sportlink.booking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.sportlink.booking.client.SchedulingClient;
import com.sportlink.booking.client.SearchClient;
import com.sportlink.booking.dto.CreateBookingRequest;
import com.sportlink.booking.dto.FacilityDto;
import com.sportlink.booking.dto.PaymentRequestDto;
import com.sportlink.booking.exception.SchedulingServiceException;
import com.sportlink.booking.exception.SearchServiceException;
import com.sportlink.booking.repository.BookingRepository;
import com.sportlink.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration test for the Booking Service endpoints that depend on
 * external components.
 *    one happy path test
 *    one error case test
 *    tested endpoints depend on other components (SchedulingClient, SearchClient) that are mocked
 */
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private SchedulingClient schedulingClient;

    @MockBean
    private SearchClient searchClient;


    @TestConfiguration
    static class TestConfig {
        @Bean
        BookingService bookingService(BookingRepository br,
                                      SchedulingClient sc,
                                      SearchClient sr) {
            return new BookingService(br, sc, sr);
        }
    }

    // ====================================================================
    // POST /api/bookings - depends on SchedulingClient
    // ====================================================================

    @Test
    void createBooking_happyPath_returns201() throws Exception {
        // SchedulingClient.reserveTimeSlot returns void on success - no stubbing needed.
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateBookingRequest request = CreateBookingRequest.builder()
                .sportCenterId("sc-100")
                .activityId("act-500")
                .timeSlotId("ts-900")
                .payment(PaymentRequestDto.builder()
                        .amount(new BigDecimal("60.00"))
                        .currency("EUR")
                        .paymentMethod("CARD")
                        .build())
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.sportCenterId").value("sc-100"))
                .andExpect(jsonPath("$.activityId").value("act-500"))
                .andExpect(jsonPath("$.timeSlotId").value("ts-900"))
                .andExpect(jsonPath("$.bookingStatus").value("PENDING"))
                .andExpect(jsonPath("$.payment.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.payment.money.amount").value(60.00))
                .andExpect(jsonPath("$.payment.money.currency").value("EUR"));
    }

    @Test
    void createBooking_whenSchedulingServiceFails_returns503() throws Exception {
        doThrow(new SchedulingServiceException(
                "Could not reserve time slot via Scheduling Service"))
                .when(schedulingClient).reserveTimeSlot(anyString(), anyString());

        CreateBookingRequest request = CreateBookingRequest.builder()
                .sportCenterId("sc-100")
                .activityId("act-500")
                .timeSlotId("ts-900")
                .payment(PaymentRequestDto.builder()
                        .amount(new BigDecimal("60.00"))
                        .currency("EUR")
                        .paymentMethod("CARD")
                        .build())
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("Scheduling Service")));
    }

    // ====================================================================
    // GET /api/bookings/facilities/search - depends on SearchClient
    // ====================================================================

    @Test
    void searchFacilities_happyPath_returnsFacilities() throws Exception {
        FacilityDto facility = FacilityDto.builder()
                .entityId("fac-001")
                .facilityName("Tartu Sports Hall")
                .displayName("Tartu Sports Hall - Court A")
                .sportType("BASKETBALL")
                .location("Tartu")
                .build();

        when(searchClient.searchFacilities(anyString(), anyString(), any()))
                .thenReturn(List.of(facility));

        mockMvc.perform(get("/api/bookings/facilities/search")
                        .param("sportType", "BASKETBALL")
                        .param("location", "Tartu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entityId").value("fac-001"))
                .andExpect(jsonPath("$[0].facilityName").value("Tartu Sports Hall"))
                .andExpect(jsonPath("$[0].sportType").value("BASKETBALL"))
                .andExpect(jsonPath("$[0].location").value("Tartu"));
    }

    @Test
    void searchFacilities_whenSearchServiceFails_returns503() throws Exception {
        when(searchClient.searchFacilities(any(), any(), any()))
                .thenThrow(new SearchServiceException("Search Service unavailable"));

        mockMvc.perform(get("/api/bookings/facilities/search")
                        .param("sportType", "FOOTBALL"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("Search Service")));
    }
}
