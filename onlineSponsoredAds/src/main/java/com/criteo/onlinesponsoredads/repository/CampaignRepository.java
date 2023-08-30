package com.criteo.onlinesponsoredads.repository;

import com.criteo.onlinesponsoredads.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Campaign findFirstByStartDateBeforeAndEndDateAfterOrderByBidDesc(Instant currentDate, Instant currentDate2);
}
