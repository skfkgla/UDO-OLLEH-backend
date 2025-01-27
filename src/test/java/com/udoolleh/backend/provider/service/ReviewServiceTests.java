package com.udoolleh.backend.provider.service;

import com.udoolleh.backend.entity.Restaurant;
import com.udoolleh.backend.entity.Review;
import com.udoolleh.backend.entity.User;
import com.udoolleh.backend.exception.errors.NotFoundReviewException;
import com.udoolleh.backend.exception.errors.ReviewDuplicatedException;
import com.udoolleh.backend.repository.RestaurantRepository;
import com.udoolleh.backend.repository.ReviewRepository;
import com.udoolleh.backend.repository.UserRepository;
import com.udoolleh.backend.web.dto.RequestReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewServiceTests {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewService reviewService;

    @Test
    @Transactional
    @DisplayName("리뷰 등록 테스트(성공 - 사진이 있을 경우)")
    void registerReviewTestWhenExistPhoto(){
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .totalGrade(0.0)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        //리뷰 등록
        RequestReview.RegisterReviewDto requestDto = RequestReview.RegisterReviewDto.builder()
                .restaurantName(restaurant.getName())
                .context("리뷰 내용")
                .grade(3.5)
                .build();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.png",
                "image/png", "test data".getBytes());

        reviewService.registerReview(mockMultipartFile, "test", requestDto);

        assertNotNull(reviewRepository.findByUserAndRestaurant(user, restaurant));
        assertNotNull(reviewRepository.findByUserAndRestaurant(user, restaurant).getPhoto());
    }

    @Test
    @Transactional
    @DisplayName("리뷰 등록 테스트(성공 - 사진이 없을 경우)")
    void registerReviewTestWhenNotExistPhoto(){
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        //리뷰 등록
        RequestReview.RegisterReviewDto requestDto = RequestReview.RegisterReviewDto.builder()
                .restaurantName(restaurant.getName())
                .context("리뷰 내용")
                .grade(3.5)
                .build();

        reviewService.registerReview(null, "test", requestDto);

        assertNotNull(reviewRepository.findByUserAndRestaurant(user, restaurant));
        assertNull(reviewRepository.findByUserAndRestaurant(user, restaurant).getPhoto());
    }

    @Test
    @Transactional
    @DisplayName("리뷰 등록 테스트(성공 - 별점 확인)")
    void registerReviewTest(){
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        User user1 = User.builder()
                .email("test1")
                .password("1234")
                .build();
        user1 = userRepository.save(user1);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .totalGrade(0.0)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        //리뷰 등록
        RequestReview.RegisterReviewDto requestDto = RequestReview.RegisterReviewDto.builder()
                .restaurantName(restaurant.getName())
                .context("리뷰 내용")
                .grade(3.5)
                .build();

        reviewService.registerReview(null, "test", requestDto);

        RequestReview.RegisterReviewDto requestDto1 = RequestReview.RegisterReviewDto.builder()
                .restaurantName(restaurant.getName())
                .context("리뷰 내용")
                .grade(4.5)
                .build();
        reviewService.registerReview(null, "test1", requestDto1);

        assertEquals(4.0, restaurantRepository.findByName("음식점").getTotalGrade());
    }

    @Test
    @Transactional
    @DisplayName("리뷰 등록 테스트(실패 - 이미 리뷰가 있는 경우)")
    void registerReviewTestWhenExistReview(){
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        //리뷰 등록
        RequestReview.RegisterReviewDto requestDto = RequestReview.RegisterReviewDto.builder()
                .restaurantName(restaurant.getName())
                .context("리뷰 내용")
                .grade(3.5)
                .build();
        reviewService.registerReview(null, "test", requestDto);

        //리뷰 중복
        assertThrows(ReviewDuplicatedException.class, ()-> reviewService.registerReview(null, "test", requestDto));
    }
    @Test
    @Transactional
    @DisplayName("리뷰 수정 테스트(성공)")
    void modifyReviewTest(){
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .totalGrade(0.0)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        //리뷰 등록
        RequestReview.RegisterReviewDto requestDto = RequestReview.RegisterReviewDto.builder()
                .restaurantName(restaurant.getName())
                .context("리뷰 내용")
                .grade(3.5)
                .build();
        reviewService.registerReview(null, "test", requestDto);

        Review review = reviewRepository.findByUserAndRestaurant(user, restaurant);
        //리뷰 수정
        RequestReview.UpdateReviewDto request = RequestReview.UpdateReviewDto.builder()
                .context("리뷰 수정 내용")
                .grade(5.0)
                .build();
        reviewService.updateReview(null, "test", review.getId(), request);

        Review result = reviewRepository.findByUserAndRestaurant(user, restaurant);
        assertTrue(result.getContext().equals("리뷰 수정 내용"));
        assertEquals(5.0, restaurant.getTotalGrade());
    }

    @Test
    @Transactional
    @DisplayName("리뷰 수정 테스트(성공 - 사진 추가)")
    void modifyReviewTestWhenAddPhoto(){
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        //리뷰 등록
        RequestReview.RegisterReviewDto requestDto = RequestReview.RegisterReviewDto.builder()
                .restaurantName(restaurant.getName())
                .context("리뷰 내용")
                .grade(3.5)
                .build();
        reviewService.registerReview(null, "test", requestDto);

        Review review = reviewRepository.findByUserAndRestaurant(user, restaurant);
        //리뷰 수정
        RequestReview.UpdateReviewDto request = RequestReview.UpdateReviewDto.builder()
                .context("리뷰 수정 내용")
                .grade(5.0)
                .build();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.png",
                "image/png", "test data".getBytes());

        reviewService.updateReview(mockMultipartFile, "test", review.getId(), request);

        Review result = reviewRepository.findByUserAndRestaurant(user, restaurant);
        assertTrue(result.getContext().equals("리뷰 수정 내용"));
        assertNotNull(reviewRepository.findByUserAndRestaurant(user, restaurant).getPhoto());
    }

    @Test
    @Transactional
    @DisplayName("리뷰 수정 테스트(실패 - 리뷰가 없을 경우)")
    void modifyReviewTestWhenNotExistReview(){
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        //리뷰 수정
        RequestReview.UpdateReviewDto request = RequestReview.UpdateReviewDto.builder()
                .context("리뷰 수정 내용")
                .grade(5.0)
                .build();
        assertThrows(NotFoundReviewException.class, ()-> reviewService.updateReview(null, "test", "옳지 않은 리뷰 아이디", request));
    }

    @Test
    @Transactional
    @DisplayName("리뷰 삭제 테스트(성공)")
    void deleteReviewTest(){
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .totalGrade(0.0)
                .build();
        restaurant = restaurantRepository.save(restaurant);
        //리뷰 등록
        RequestReview.RegisterReviewDto requestDto = RequestReview.RegisterReviewDto.builder()
                .restaurantName(restaurant.getName())
                .context("리뷰 내용")
                .grade(3.5)
                .build();
        reviewService.registerReview(null, "test", requestDto);
        Review review = reviewRepository.findByUserAndRestaurant(user, restaurant);

        //리뷰 삭제
        reviewService.deleteReview("test", review.getId());

        assertNull(reviewRepository.findByUserAndRestaurant(user, restaurant));
        assertFalse(user.getReviewList().contains(review));
        assertFalse(restaurant.getReviewList().contains(review));
        assertEquals(0.0, restaurant.getTotalGrade());
    }
    @Test
    @Transactional
    @DisplayName("리뷰 삭제 테스트(실패 - 리뷰가 없을 경우)")
    void deleteReviewTestWhenNotExistReview() {
        User user = User.builder()
                .email("test")
                .password("1234")
                .build();
        user = userRepository.save(user);

        Restaurant restaurant = Restaurant.builder()
                .name("음식점")
                .build();
        restaurant = restaurantRepository.save(restaurant);
        //리뷰 삭제
        assertThrows(NotFoundReviewException.class, ()-> reviewService.deleteReview("test", "존재하지 않은 리뷰 아이디"));
    }
    }
