package com.udoolleh.backend.provider.service;

import com.udoolleh.backend.entity.User;
import com.udoolleh.backend.exception.errors.UserNicknameDuplicatedException;
import com.udoolleh.backend.repository.UserRepository;
import com.udoolleh.backend.web.dto.RequestUser;
import com.udoolleh.backend.web.dto.ResponseUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTests {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Test
    @DisplayName("회원가입 서비스 테스트")
    @Transactional
    void registerTest() {
        //given
        RequestUser.RegisterUserDto dto = RequestUser.RegisterUserDto.builder()
                .email("hello")
                .password("itsmypassword")
                .build();
        //when
        userService.register(dto);
        //then
        User user = userRepository.findByEmail(dto.getEmail());
        assertEquals(dto.getEmail(), user.getEmail());
        System.out.println(user.getEmail());
        System.out.println(user.getPassword());
    }

    @Test
    @DisplayName("회원가입 테스트(실패 - 닉네임 중복됐을 경우)")
    @Transactional
    void registerTestWhenDuplicatedNickname() {
        //given
        RequestUser.RegisterUserDto dto = RequestUser.RegisterUserDto.builder()
                .email("hello")
                .nickname("nickname")
                .password("itsmypassword")
                .build();
        //when
        userService.register(dto);

        //닉네임 중복
        RequestUser.RegisterUserDto dto1 = RequestUser.RegisterUserDto.builder()
                .email("test")
                .nickname("nickname")
                .password("1234")
                .build();
        //then
        assertThrows(UserNicknameDuplicatedException.class, ()-> userService.register(dto1));
    }

    @Transactional
    @Test
    @DisplayName("로그인 서비스 테스트")
    void loginTest() {
        //given
        RequestUser.RegisterUserDto dto = RequestUser.RegisterUserDto.builder()
                .email("hello")
                .password("itsmypassword")
                .build();
        userService.register(dto);

        RequestUser.LoginDto loginRequest = RequestUser.LoginDto.builder()
                .email("hello")
                .password("itsmypassword")
                .build();

        //when
        ResponseUser.Token loginResponse = userService.login(loginRequest).orElseGet(()->null);
        System.out.println(loginResponse.getAccessToken());
        System.out.println(loginResponse.getRefreshToken());

        //then
        assertNotNull(loginResponse.getAccessToken());
        assertNotNull(loginResponse.getRefreshToken());
    }

    //서버에서는 리프레시 토큰이 제대로 변경 되었는지 확인
    @Test
    @DisplayName("로그아웃 테스트")
    void logoutTest() {
        //given
        User user = User.builder()
                .email("test")
                .password("1234")
                .nickname("him")
                .build();
        user = userRepository.save(user);

        user.changeRefreshToken("refreshToken");
        assertEquals(user.getRefreshToken(), "refreshToken");
        //when
        userService.logout("test"); //객체를 변경시키기 때문에 새로운 참조값으로 변함
        //then
        assertEquals(userRepository.findByEmail("test").getRefreshToken(), "");
    }

    @Transactional
    @Test
    @DisplayName("토큰 갱신 테스트")
    void refreshTokenTest() {
        //given
        RequestUser.RegisterUserDto dto = RequestUser.RegisterUserDto.builder()
                .email("hello")
                .password("itsmypassword")
                .build();
        userService.register(dto);

        RequestUser.LoginDto loginRequest = RequestUser.LoginDto.builder()
                .email("hello")
                .password("itsmypassword")
                .build();

        //when
        ResponseUser.Token loginResponse = userService.login(loginRequest).orElseGet(()->null);
        ResponseUser.Token tokenResponse = userService.refreshToken(loginResponse.getRefreshToken()).orElseGet(()->null);
        //then
        assertNotNull(tokenResponse.getRefreshToken());
        assertNotNull(tokenResponse.getAccessToken());
        System.out.println(tokenResponse.getAccessToken());
        System.out.println(tokenResponse.getRefreshToken());
    }

    @Test
    @DisplayName("회원정보 수정 테스트(성공)")
    void ChangeUserInfoTest(){
        User user = User.builder()
                .email("email")
                .password("password")
                .nickname("nick")
                .build();
        userRepository.save(user);
        //회원정보 변경
        RequestUser.UpdateUserDto updateDto = RequestUser.UpdateUserDto.builder()
                .password("changedpassword")
                .nickname("changednick")
                .build();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test2.png",
                "image/png", "test data".getBytes());
        userService.updateUser("email",mockMultipartFile,updateDto);
        User updateUser = userRepository.findByEmail("email");
        assertEquals(updateDto.getNickname(), updateUser.getNickname());
        assertNotNull(updateUser.getProfile());
        }


}
