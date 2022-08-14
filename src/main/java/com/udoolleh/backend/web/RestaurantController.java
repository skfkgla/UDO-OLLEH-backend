package com.udoolleh.backend.web;

import com.udoolleh.backend.core.type.PlaceType;
import com.udoolleh.backend.core.type.ShipCourseType;
import com.udoolleh.backend.core.type.ShipTimetableType;
import com.udoolleh.backend.core.type.UdoCoordinateType;
import com.udoolleh.backend.provider.service.KakaoApiService;
import com.udoolleh.backend.provider.service.RestaurantService;
import com.udoolleh.backend.provider.service.S3Service;
import com.udoolleh.backend.provider.service.ShipService;
import com.udoolleh.backend.web.dto.CommonResponse;
import com.udoolleh.backend.web.dto.ResponseWharfTimetable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestaurantController {
    private final KakaoApiService kakaoApiService;
    private final RestaurantService restaurantService;
    private final S3Service s3Service;

    @PostMapping("/admin/restaurant/place")
    public ResponseEntity<CommonResponse> registerRestaurantInfo(@RequestBody PlaceType place){
        kakaoApiService.callKakaoApi("우도",1,place,UdoCoordinateType.ONE_QUADRANT); //1사분면 저장
        return new ResponseEntity<>(CommonResponse.builder()
                .status(HttpStatus.OK.value())
                .message("카카오 맛집 등록 성공")
                .build(), HttpStatus.OK);
    }
    @PostMapping("/admin/restaurant/images")
    public ResponseEntity<CommonResponse> registerRestaurantImage(@RequestPart(value="images") List<MultipartFile> images,@RequestPart(value="restaurantName") String restaurantName){
        restaurantService.registerRestaurantImage(images, restaurantName);
        return new ResponseEntity<>(CommonResponse.builder()
                .status(HttpStatus.OK.value())
                .message("S3사진 등록 성공")
                .build(), HttpStatus.OK);
    }



}
