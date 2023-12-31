package com.criteo.onlinesponsoredads.repository;

import com.criteo.onlinesponsoredads.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Query("SELECT c FROM Campaign c " +
            "WHERE :currentDate >= c.startDate " +
            "AND :currentDate <= FUNCTION('DATEADD', 'DAY', 10, c.startDate) " +
            "ORDER BY c.bid DESC ")
    List<Campaign> findActiveCampaignsOrderByBidDesc(Instant currentDate, Pageable pageable);
}
