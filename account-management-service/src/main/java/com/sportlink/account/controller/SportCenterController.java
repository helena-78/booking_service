package com.sportlink.account.controller;

import com.sportlink.account.dto.request.RegisterFacilityRequest;
import com.sportlink.account.dto.request.UpdateFacilityRequest;
import com.sportlink.account.dto.response.FacilityProfileResponse;
import com.sportlink.account.service.SportCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/sport-centers")
@RequiredArgsConstructor
public class SportCenterController {

    private final SportCenterService sportCenterService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityProfileResponse registerFacility(@Valid @RequestBody RegisterFacilityRequest request) {
        return sportCenterService.registerFacility(request);
    }

    @GetMapping("/{id}")
    public FacilityProfileResponse getFacility(@PathVariable("id") UUID id) {
        return sportCenterService.getFacilityById(id);
    }

    @PutMapping("/{id}")
    public FacilityProfileResponse updateFacility(@PathVariable("id") UUID id,
                                                  @Valid @RequestBody UpdateFacilityRequest request) {
        return sportCenterService.updateFacility(id, request);
    }
}
