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
        CampaignCreateRequest createRequest1 = new CampaignCreateRequest("campaign1", startDate, List.of("aaaa", "cccc", "ffff"), 50.0);
        startDate = now.minus(campaignActivePeriod-3, ChronoUnit.DAYS);
        CampaignCreateRequest createRequest2 = new CampaignCreateRequest("campaign2", startDate, List.of("dddd", "eeee", "hhhh"), 80.0);
        startDate = now.minus(campaignActivePeriod+1, ChronoUnit.DAYS);
        CampaignCreateRequest createRequest3 = new CampaignCreateRequest("campaign3", startDate, List.of("bbbb", "gggg", "rrrr"), 90.0);
        controller.createCampaign(createRequest1, bindingResult);
        controller.createCampaign(createRequest2, bindingResult);
        controller.createCampaign(createRequest3, bindingResult);
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
        assertEquals(3, campaignsMap.size());
        CampaignDto campaign1 = campaignsMap.get("campaign1");
        assertEquals(50.0, campaign1.getBid());
        assertEquals(3, campaign1.getProducts().size());
        assertEquals(List.of("iPhone","chair","shovel"), campaign1.getProducts().stream().map(ProductDto::getTitle).collect(Collectors.toList()));
        CampaignDto campaign2 = campaignsMap.get("campaign2");
        assertEquals(80.0, campaign2.getBid());
        assertEquals(3, campaign2.getProducts().size());
        assertEquals(List.of("table","wheelbarrow","silverware"), campaign2.getProducts().stream().map(ProductDto::getTitle).collect(Collectors.toList()));
        CampaignDto campaign3 = campaignsMap.get("campaign3");
        assertEquals(90.0, campaign3.getBid());
        assertEquals(3, campaign3.getProducts().size());
        assertEquals(List.of("galaxy tablet","frying pan","wall clock"), campaign3.getProducts().stream().map(ProductDto::getTitle).collect(Collectors.toList()));
    }

    @Test
    void testServeAd() {
        ResponseEntity<?> response = controller.serveAd("furniture");
        ProductDto product = (ProductDto) response.getBody();
        assertEquals("table", product.getTitle());
        response = controller.serveAd("gardening");
        product = (ProductDto) response.getBody();
        assertEquals("wheelbarrow", product.getTitle());
    }

    @Test
    void testServeAdCategoryNotInActiveCampaign() {
        ResponseEntity<?> response = controller.serveAd("decorative");
        ProductDto product = (ProductDto) response.getBody();
        assertEquals("diamond ring", product.getTitle());
    }

    @Test
    void testServeAdCategoryNotExist() {
        ResponseEntity<?> response = controller.serveAd("new categ");
        ProductDto product = (ProductDto) response.getBody();
        assertEquals("diamond ring", product.getTitle());
    }

}
