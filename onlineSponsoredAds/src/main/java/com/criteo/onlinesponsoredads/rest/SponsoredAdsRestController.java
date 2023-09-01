package com.criteo.onlinesponsoredads.rest;

import com.criteo.onlinesponsoredads.domain.Product;
import com.criteo.onlinesponsoredads.domain.dto.CampaignCreateRequest;
import com.criteo.onlinesponsoredads.domain.dto.CampaignDto;
import com.criteo.onlinesponsoredads.domain.dto.ProductDto;
import com.criteo.onlinesponsoredads.service.CampaignService;
import com.criteo.onlinesponsoredads.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "SponsoredAdsRestController", description = "This is the SponsoredAdsRestController")
@Slf4j
@RestController
@RequestMapping("ads/")
public class SponsoredAdsRestController {

    private final ProductService productService;
    private final CampaignService campaignService;

    public SponsoredAdsRestController(ProductService productService, CampaignService campaignService) {
        this.productService = productService;
        this.campaignService = campaignService;
    }

    @Operation(summary = "returns list of all campaigns")
    @GetMapping("campaigns")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(campaignService.getAll());
    }

    @Operation(summary = "creates new campain with the specified params")
    @PostMapping("campaign")
    public ResponseEntity<?> createCampaign(@RequestBody @Valid CampaignCreateRequest campaignCreateRequest, BindingResult bindingResult) {
        List<String> validationErs = checkAndFormatValidationMsg(bindingResult);
        if(!validationErs.isEmpty()){
            return ResponseEntity.badRequest().body(validationErs);
        }
        log.info("received create campaign request {}" , campaignCreateRequest);
        try {
            List<Product> products = productService.getAllProductsBySerialNumbers(campaignCreateRequest.getProductSerialNumbers());
            CampaignDto campaignDto = campaignService.createCampaign(campaignCreateRequest.getName(), campaignCreateRequest.getStartDate(), products, campaignCreateRequest.getBid());
            log.info("campaign {} created successfully", campaignCreateRequest.getName());
            return new ResponseEntity<>(campaignDto, HttpStatus.CREATED);
        }
        catch (Exception e){
            log.error("error occurred while processing create campaign request {} ", campaignCreateRequest, e);
            return ResponseEntity.internalServerError().body(e.toString());
        }

    }

    @Operation(summary = "serves campaign with the specified category")
    @GetMapping("serveAd")
    public ResponseEntity<?> serveAd(@RequestParam String category) {
        log.info("serving ad for category {}", category);
        try {
            Instant now = Instant.now();
            Product productWithHighestBid = productService.getProductWithHighestBidByCategory(category, now);
            productWithHighestBid = productWithHighestBid == null ? campaignService.getProductOfCampaignWithHighestBid(now) : productWithHighestBid;
            return productWithHighestBid !=null ? new ResponseEntity<>(new ProductDto(productWithHighestBid), HttpStatus.OK) : ResponseEntity.notFound().build();
        }
        catch (Exception e){
            log.error("error occurred while serving ad for category {} ", category, e);
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    private List<String> checkAndFormatValidationMsg(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        List<String> validationErs = new ArrayList<>();
        for (FieldError fieldError : fieldErrors) {
            validationErs.add(String.join(" ", "field", "'"+fieldError.getField()+"'", fieldError.getDefaultMessage()));
        }
        return validationErs;
    }
}
