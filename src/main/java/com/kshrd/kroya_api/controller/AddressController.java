package com.kshrd.kroya_api.controller;

import com.kshrd.kroya_api.payload.Address.AddressRequest;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.service.Address.AddressService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/address")
@AllArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/create")
    public BaseResponse<?> createAddress(@RequestBody AddressRequest addressRequest){
        return addressService.createAddress(addressRequest);
    }

    @GetMapping("/list")
    public BaseResponse<?> getAllAddresses() {
        return addressService.getAllAddresses();
    }

    @GetMapping("/{id}")
    public BaseResponse<?> getAddressById(@PathVariable Long id) {
        return addressService.getAddressById(id);
    }

    @PutMapping("/update/{id}")
    public BaseResponse<?> updateAddress(@PathVariable Long id, @RequestBody AddressRequest addressRequest) {
        return addressService.updateAddress(id, addressRequest);
    }

    @DeleteMapping("/delete/{id}")
    public BaseResponse<?> deleteAddress(@PathVariable Long id) {
        return addressService.deleteAddress(id);
    }

}
