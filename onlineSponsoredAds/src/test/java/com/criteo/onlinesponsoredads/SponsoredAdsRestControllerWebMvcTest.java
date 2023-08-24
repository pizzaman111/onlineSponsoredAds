package com.criteo.onlinesponsoredads;

import com.criteo.onlinesponsoredads.domain.Campaign;
import com.criteo.onlinesponsoredads.domain.Product;
import com.criteo.onlinesponsoredads.domain.dto.CampaignCreateRequest;
import com.criteo.onlinesponsoredads.domain.dto.CampaignDto;
import com.criteo.onlinesponsoredads.rest.SponsoredAdsRestController;
import com.criteo.onlinesponsoredads.service.CampaignService;
import com.criteo.onlinesponsoredads.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SponsoredAdsRestController.class)
public class SponsoredAdsRestControllerWebMvcTest {

    static final String SERVE_AD_ENDPOINT = "/ads/serveAd";
    static final String CREATE_CAMPAIGN_ENDPOINT = "/ads/campaign";
    private CampaignCreateRequest createRequest;

    @MockBean
    private ProductService productService;

    @MockBean
    private CampaignService campaignService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() {
        createRequest = new CampaignCreateRequest("campaign", Instant.now(), List.of("a", "b", "c"), 50.0);
    }

    @Test
    public void testCreateCampaignOk() throws Exception {
        Campaign campaign = new Campaign("camp-name", Instant.now(), Instant.now(), 50d, Collections.emptyList());
        List<Product> products = List.of(new Product(), new Product(), new Product());
        when(productService.getAllProductsBySerialNumbers(anyList())).thenReturn(products);
        when(campaignService.createCampaign(anyString(), any(Instant.class), anyList(), anyLong())).thenReturn(new CampaignDto(campaign));
        String requestAsStr = objectMapper.writeValueAsString(createRequest);
        MockHttpServletRequestBuilder createCmpn = createPostRequestWithBody(CREATE_CAMPAIGN_ENDPOINT, requestAsStr);
        mockMvc.perform(createCmpn).andDo(print()).andExpect(status().isCreated());
        verify(campaignService).createCampaign(createRequest.getName(), createRequest.getStartDate(), products, createRequest.getBid());
    }

    @Test
    public void testCreateCampaignMissingName() throws Exception {
        createRequest.setName("");
        String requestAsStr = objectMapper.writeValueAsString(createRequest);
        MockHttpServletRequestBuilder createCmpn = createPostRequestWithBody(CREATE_CAMPAIGN_ENDPOINT, requestAsStr);
        mockMvc.perform(createCmpn).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(List.of("\"field 'name' must not be empty\"").toString()));
    }

    @Test
    public void testCreateCampaignMissingStartDate() throws Exception {
        createRequest.setStartDate(null);
        String requestAsStr = objectMapper.writeValueAsString(createRequest);
        MockHttpServletRequestBuilder createCmpn = createPostRequestWithBody(CREATE_CAMPAIGN_ENDPOINT, requestAsStr);
        mockMvc.perform(createCmpn).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(List.of("\"field 'startDate' must not be null\"").toString()));
    }

    @Test
    public void testCreateCampaignInvalidBid() throws Exception {
        createRequest.setBid(0.0);
        String requestAsStr = objectMapper.writeValueAsString(createRequest);
        MockHttpServletRequestBuilder createCmpn = createPostRequestWithBody(CREATE_CAMPAIGN_ENDPOINT, requestAsStr);
        mockMvc.perform(createCmpn).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(List.of("\"field 'bid' must be greater than or equal to 1\"").toString()));
    }

    @Test
    public void testCreateCampaignMissingProductSerialNums() throws Exception {
        createRequest.setProductSerialNumbers(Collections.emptyList());
        String requestAsStr = objectMapper.writeValueAsString(createRequest);
        MockHttpServletRequestBuilder createCmpn = createPostRequestWithBody(CREATE_CAMPAIGN_ENDPOINT, requestAsStr);
        mockMvc.perform(createCmpn).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(List.of("\"field 'productSerialNumbers' must not be empty\"").toString()));
    }

    @Test
    public void testCreateCampaignGeneralError() throws Exception {
        RuntimeException runtimeException = new RuntimeException();
        when(productService.getAllProductsBySerialNumbers(anyList())).thenThrow(runtimeException);
        String requestAsStr = objectMapper.writeValueAsString(createRequest);
        MockHttpServletRequestBuilder createCmpn = createPostRequestWithBody(CREATE_CAMPAIGN_ENDPOINT, requestAsStr);
        mockMvc.perform(createCmpn).andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(runtimeException.toString()));
    }

    @Test
    public void testServeAdOk() throws Exception {
        when(productService.findProductWithHighestBid(anyString(), any(Instant.class))).thenReturn(new Product());
        MockHttpServletRequestBuilder serveAd = createGetRequestWithGivenParams(SERVE_AD_ENDPOINT, Map.of("category", "category"));
        mockMvc.perform(serveAd).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testServeAdGeneralError() throws Exception {
        RuntimeException runtimeException = new RuntimeException();
        when(productService.findProductWithHighestBid(anyString(), any(Instant.class))).thenThrow(runtimeException);
        MockHttpServletRequestBuilder serveAd = createGetRequestWithGivenParams(SERVE_AD_ENDPOINT, Map.of("category", "category"));
        mockMvc.perform(serveAd).andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(runtimeException.toString()));
    }

    private MockHttpServletRequestBuilder createPostRequestWithBody(String url, String content) {
        return createRequest(post(url).content(content), APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder createGetRequestWithGivenParams(String url, Map<String, String> requestParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            params.add(entry.getKey(), entry.getValue());
        }
        return createRequest(get(url).params(params), APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder createRequest(MockHttpServletRequestBuilder requestBuilder, MediaType mediaType) {
        return requestBuilder.characterEncoding(UTF_8).contentType(mediaType);
    }
}
