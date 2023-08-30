package com.criteo.onlinesponsoredads;

import com.criteo.onlinesponsoredads.domain.dto.CampaignCreateRequest;
import com.criteo.onlinesponsoredads.domain.dto.CampaignDto;
import com.criteo.onlinesponsoredads.domain.dto.ProductDto;
import com.criteo.onlinesponsoredads.repository.CampaignRepository;
import com.criteo.onlinesponsoredads.rest.SponsoredAdsRestController;
import com.criteo.onlinesponsoredads.service.CampaignService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class OnlineSponsoredAdsApplicationTests {

    @Value("${campaignActivePeriod:10}")
    private int campaignActivePeriod;

    @Mock
    private BindingResult bindingResult;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private SponsoredAdsRestController controller;

    @BeforeEach
    public void beforeEach() {
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());
        Instant now  = Instant.now();
        Instant startDate = now.minus(campaignActivePeriod-5, ChronoUnit.DAYS);
        CampaignCreateRequest createRequest1 = new CampaignCreateRequest("campaign1", startDate, List.of("bbbb", "cccc", "eeee"), 60.0);
        startDate = now.minus(campaignActivePeriod-3, ChronoUnit.DAYS);
        CampaignCreateRequest createRequest2 = new CampaignCreateRequest("campaign2", startDate, List.of("dddd", "ffff", "hhhh"), 40.0);
        startDate = now.minus(campaignActivePeriod-4, ChronoUnit.DAYS);
        CampaignCreateRequest createRequest3 = new CampaignCreateRequest("campaign3", startDate, List.of("aaaa", "ffff"), 90.0);
        startDate = now.minus(campaignActivePeriod+1, ChronoUnit.DAYS);
        CampaignCreateRequest createRequest4 = new CampaignCreateRequest("campaign4", startDate, List.of("gggg", "rrrr"), 80.0);
        controller.createCampaign(createRequest1, bindingResult);
        controller.createCampaign(createRequest2, bindingResult);
        controller.createCampaign(createRequest3, bindingResult);
        controller.createCampaign(createRequest4, bindingResult);
    }

    @AfterEach
    public void afterEach() {
        campaignRepository.deleteAll();
    }

    @Transactional
    @Test
    void testCreateCampaign() {
        List<CampaignDto> campaigns = campaignService.getAll();
        Map<String, CampaignDto> campaignsMap = campaigns.stream().collect(Collectors.toMap(CampaignDto::getName, d -> d));
        assertEquals(4, campaignsMap.size());
        CampaignDto campaign1 = campaignsMap.get("campaign1");
        assertEquals(60.0, campaign1.getBid());
        assertEquals(3, campaign1.getProducts().size());
        assertEquals(List.of("galaxy tablet","chair","wheelbarrow"), campaign1.getProducts().stream().map(ProductDto::getTitle).collect(Collectors.toList()));
        CampaignDto campaign2 = campaignsMap.get("campaign2");
        assertEquals(40.0, campaign2.getBid());
        assertEquals(3, campaign2.getProducts().size());
        assertEquals(List.of("table","shovel","silverware"), campaign2.getProducts().stream().map(ProductDto::getTitle).collect(Collectors.toList()));
        CampaignDto campaign3 = campaignsMap.get("campaign3");
        assertEquals(90.0, campaign3.getBid());
        assertEquals(2, campaign3.getProducts().size());
        assertEquals(List.of("iPhone", "shovel"), campaign3.getProducts().stream().map(ProductDto::getTitle).collect(Collectors.toList()));
        CampaignDto campaign4 = campaignsMap.get("campaign4");
        assertEquals(80.0, campaign4.getBid());
        assertEquals(2, campaign4.getProducts().size());
        assertEquals(List.of("frying pan", "wall clock"), campaign4.getProducts().stream().map(ProductDto::getTitle).collect(Collectors.toList()));
    }

    @Test
    void testServeAd() {
        ResponseEntity<?> response = controller.serveAd("electronics");
        ProductDto product = (ProductDto) response.getBody();
        assertEquals("iPhone", product.getTitle());
        response = controller.serveAd("furniture");
        product = (ProductDto) response.getBody();
        assertEquals("chair", product.getTitle());
        response = controller.serveAd("gardening");
        product = (ProductDto) response.getBody();
        assertEquals("shovel", product.getTitle());
        response = controller.serveAd("cooking");
        product = (ProductDto) response.getBody();
        assertEquals("silverware", product.getTitle());
    }

    @Transactional
    @Test
    void testServeAdCategoryNotInActiveCampaign() {
        ResponseEntity<?> response = controller.serveAd("decorative");
        ProductDto product = (ProductDto) response.getBody();
        assertEquals("iPhone", product.getTitle());
    }

    @Transactional
    @Test
    void testServeAdCategoryNotExist() {
        ResponseEntity<?> response = controller.serveAd("new categ");
        ProductDto product = (ProductDto) response.getBody();
        assertEquals("iPhone", product.getTitle());
    }

}
