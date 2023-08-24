package com.criteo.onlinesponsoredads.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class CampaignCreateRequest {
    @NotEmpty
    private String name;
    @NotNull
    private Instant startDate;
    @NotEmpty
    private List<String> productSerialNumbers;
    @Min(1)
    private Double bid;
}