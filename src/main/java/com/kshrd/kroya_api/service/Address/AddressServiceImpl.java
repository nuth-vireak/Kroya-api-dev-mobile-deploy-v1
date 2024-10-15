package com.kshrd.kroya_api.service.Address;

import com.kshrd.kroya_api.entity.AddressEntity;
import com.kshrd.kroya_api.entity.UserEntity;
import com.kshrd.kroya_api.payload.Address.AddressRequest;
import com.kshrd.kroya_api.payload.Address.AddressResponse;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.repository.Address.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;

    @Override
    public BaseResponse<?> createAddress(AddressRequest addressRequest) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        // Map the request to AddressEntity
        AddressEntity addressEntity = modelMapper.map(addressRequest, AddressEntity.class);
        addressEntity.setUser(currentUser);  // Set the authenticated user

        // Save the address to the database
        AddressEntity savedAddress = addressRepository.save(addressEntity);
        AddressResponse addressResponse = modelMapper.map(savedAddress, AddressResponse.class);

        // Return success response
        return BaseResponse.builder()
                .payload(addressResponse)
                .message("Address created successfully")
                .statusCode("201")
                .build();
    }

    @Override
    public BaseResponse<?> getAllAddresses() {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching addresses for user: {}", currentUser.getEmail());

//        // Find addresses by the current user's ID
        List<AddressEntity> addresses = addressRepository.findAllByUserId(currentUser.getId());

        // Map to response format
        List<AddressResponse> addressResponses = addresses.stream()
                .map(address -> modelMapper.map(address, AddressResponse.class))
                .toList();

        return BaseResponse.builder()
                .payload(addressResponses)
                .message("Addresses fetched successfully")
                .statusCode("200")
                .build();
    }

    @Override
    public BaseResponse<?> getAddressById(Long id) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching address with ID: {} for user: {}", id, currentUser.getEmail());

        // Find the address by ID
        Optional<AddressEntity> addressEntityOptional = addressRepository.findById(id);
        if (addressEntityOptional.isPresent()) {
            AddressEntity addressEntity = addressEntityOptional.get();

            // Check if the address belongs to the current user
            if (!addressEntity.getUser().getId().equals(currentUser.getId())) {
                return BaseResponse.builder()
                        .message("Address not found or not owned by current user")
                        .statusCode("403")  // Forbidden
                        .build();
            }

            // Map to response format
            AddressResponse addressResponse = modelMapper.map(addressEntity, AddressResponse.class);

            return BaseResponse.builder()
                    .payload(addressResponse)
                    .message("Address fetched successfully")
                    .statusCode("200")
                    .build();
        } else {
            return BaseResponse.builder()
                    .message("Address not found")
                    .statusCode("404")
                    .build();
        }
    }

    @Override
    public BaseResponse<?> updateAddress(Long id, AddressRequest addressRequest) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Updating address with ID: {} for user: {}", id, currentUser.getEmail());

        // Find the address by ID
        Optional<AddressEntity> addressEntityOptional = addressRepository.findById(id);
        if (addressEntityOptional.isPresent()) {
            AddressEntity addressEntity = addressEntityOptional.get();

            // Check if the address belongs to the current user
            if (!addressEntity.getUser().getId().equals(currentUser.getId())) {
                return BaseResponse.builder()
                        .message("Address not found or not owned by current user")
                        .statusCode("403")  // Forbidden
                        .build();
            }

            // Update the address details
            modelMapper.map(addressRequest, addressEntity);

            // Save the updated address
            AddressEntity updatedAddress = addressRepository.save(addressEntity);
            AddressResponse addressResponse = modelMapper.map(updatedAddress, AddressResponse.class);

            return BaseResponse.builder()
                    .payload(addressResponse)
                    .message("Address updated successfully")
                    .statusCode("200")
                    .build();
        } else {
            return BaseResponse.builder()
                    .message("Address not found")
                    .statusCode("404")
                    .build();
        }

    }

    @Override
    public BaseResponse<?> deleteAddress(Long id) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Deleting address with ID: {} for user: {}", id, currentUser.getEmail());

        // Find the address by ID
        Optional<AddressEntity> addressEntityOptional = addressRepository.findById(id);
        if (addressEntityOptional.isPresent()) {
            AddressEntity addressEntity = addressEntityOptional.get();

            // Check if the address belongs to the current user
            if (!addressEntity.getUser().getId().equals(currentUser.getId())) {
                return BaseResponse.builder()
                        .message("Address not found or not owned by current user")
                        .statusCode("403")  // Forbidden
                        .build();
            }

            // Delete the address
            addressRepository.deleteById(id);
            return BaseResponse.builder()
                    .message("Address deleted successfully")
                    .statusCode("200")
                    .build();
        } else {
            return BaseResponse.builder()
                    .message("Address not found")
                    .statusCode("404")
                    .build();
        }
    }
}
