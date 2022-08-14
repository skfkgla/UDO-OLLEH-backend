package com.udoolleh.backend.provider.service;

import com.udoolleh.backend.core.service.ReviewServiceInterface;
import com.udoolleh.backend.entity.Restaurant;
import com.udoolleh.backend.entity.Review;
import com.udoolleh.backend.entity.User;
import com.udoolleh.backend.exception.errors.NotFoundRestaurantException;
import com.udoolleh.backend.exception.errors.NotFoundReviewException;
import com.udoolleh.backend.exception.errors.NotFoundUserException;
import com.udoolleh.backend.exception.errors.ReviewDuplicatedException;
import com.udoolleh.backend.repository.RestaurantRepository;
import com.udoolleh.backend.repository.ReviewRepository;
import com.udoolleh.backend.repository.UserRepository;
import com.udoolleh.backend.web.dto.RequestReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService implements ReviewServiceInterface {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public void registerReview(MultipartFile file,  String email, RequestReviewDto.register requestDto){
        User user = userRepository.findByEmail(email);
        if(user == null){ //해당 유저가 없으면
            throw new NotFoundUserException();
        }
        Restaurant restaurant = restaurantRepository.findById(requestDto.getRestaurantId()).orElseThrow(()-> new NotFoundRestaurantException());

        Review review = reviewRepository.findByUserAndRestaurant(user, restaurant);
        if(review != null){ //이미 리뷰가 있다면
            throw new ReviewDuplicatedException();
        }
        //리뷰 등록
        review = Review.builder()
                .user(user)
                .title(requestDto.getTitle())
                .context(requestDto.getContext())
                .grade(requestDto.getGrade())
                .restaurant(restaurant)
                .build();
        review = reviewRepository.save(review);
        restaurant.addReview(review);
        user.addReview(review);

        //사진 등록
    }

    @Override
    @Transactional
    public void modifyReview(MultipartFile file, String email, String reviewId, RequestReviewDto.modify requestDto){
        User user = userRepository.findByEmail(email);
        if(user == null){ //해당 유저가 없으면
            throw new NotFoundUserException();
        }
        Review review = reviewRepository.findByUserAndReviewId(user, reviewId);
        if(review == null){
            throw new NotFoundReviewException();
        }

        //사진 수정
        String photo = "";

        review.modifyReview(requestDto.getTitle(), requestDto.getContext(), photo, requestDto.getGrade());
    }

    @Override
    @Transactional
    public void deleteReview(String email, String reviewId){
        User user = userRepository.findByEmail(email);
        if(user == null){ //해당 유저가 없으면
            throw new NotFoundUserException();
        }
        Review review = reviewRepository.findByUserAndReviewId(user, reviewId);
        if(review == null){
            throw new NotFoundReviewException();
        }
        //리뷰 삭제
        review.getRestaurant().getReviewList().remove(review);
        user.getReviewList().remove(review);

        reviewRepository.delete(review);
    }

}