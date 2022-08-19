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
import com.udoolleh.backend.web.dto.RequestReview;
import com.udoolleh.backend.web.dto.ResponseReview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService implements ReviewServiceInterface {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public void registerReview(MultipartFile file,  String email, RequestReview.registerDto requestDto){
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
                .context(requestDto.getContext())
                .grade(requestDto.getGrade())
                .restaurant(restaurant)
                .build();
        review = reviewRepository.save(review);
        restaurant.addReview(review);
        user.addReview(review);

        //사진이 있으면 등록
        if(Optional.ofNullable(file).isPresent()){
            String url= "";
            try{
                url = s3Service.upload(file, "review");
                review.updatePhoto(url);
            }catch (IOException e){
                System.out.println("s3 등록 실패");
            }
        }
    }

    @Override
    @Transactional
    public void modifyReview(MultipartFile file, String email, String reviewId, RequestReview.modifyDto requestDto){
        User user = userRepository.findByEmail(email);
        if(user == null){ //해당 유저가 없으면
            throw new NotFoundUserException();
        }
        Review review = reviewRepository.findByUserAndReviewId(user, reviewId);
        if(review == null){
            throw new NotFoundReviewException();
        }
        if(Optional.ofNullable(review.getPhoto()).isPresent()){
            s3Service.deleteFile(review.getPhoto());

        }
        //사진 수정
        review.modifyReview(requestDto.getContext(), requestDto.getGrade());


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

    @Override
    @Transactional
    public List<ResponseReview.getReviewDto> getReview(String restaurantId){
        List<ResponseReview.getReviewDto> list = new ArrayList<>();

        return list;
    }
}
