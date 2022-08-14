package com.udoolleh.backend.provider.service;

import com.udoolleh.backend.core.service.RestaurantServiceInterface;
import com.udoolleh.backend.entity.Photo;
import com.udoolleh.backend.entity.Restaurant;
import com.udoolleh.backend.exception.errors.NotFoundRestaurantException;
import com.udoolleh.backend.repository.PhotoRepository;
import com.udoolleh.backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RestaurantService implements RestaurantServiceInterface {
    private final RestaurantRepository restaurantRepository;
    private final PhotoRepository photoRepository;
    private final S3Service s3Service;
    @Override
    @Transactional
    public void registerRestaurantImage(MultipartFile multipartFile, String restaurantName){
        Restaurant restaurant = restaurantRepository.findByName(restaurantName);
        if(restaurant == null){
            throw new NotFoundRestaurantException();
        }
        String imageUrl="";
        try{
            imageUrl = s3Service.upload(multipartFile,"static");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Photo photo = Photo.builder()
                .url(imageUrl)
                .restaurant(restaurant)
                .build();
        photoRepository.save(photo);

    }
}