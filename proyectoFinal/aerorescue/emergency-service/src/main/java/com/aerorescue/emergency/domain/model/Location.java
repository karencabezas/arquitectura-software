package com.aerorescue.emergency.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Location {
    private String address;
    private String region;
    private double lat;
    private double lng;
}
