package com.kshrd.kroya_api.payload.Address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
    private String addressDetail;
    private String specificLocation;
    private String tag;
    private Double latitude;
    private Double longitude;
}
