package com.criteo.onlinesponsoredads.domain.dto;

import com.criteo.onlinesponsoredads.domain.Campaign;
import com.criteo.onlinesponsoredads.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor
public class CampaignDto {

    private String name;
    private Instant startDate;
    private double bid;
    private List<ProductDto> products;

    public CampaignDto(Campaign campaign) {
        this.name = campaign.getName();
        this.startDate = campaign.getStartDate();
        this.bid = campaign.getBid();
        this.products = campaign.getProducts().stream().map(ProductDto::new).collect(Collectors.toList());
    }

}

