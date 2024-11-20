package com.globalnest.be.user.aplication;

import com.globalnest.be.user.domain.Subscribe;
import com.globalnest.be.user.domain.User;
import com.globalnest.be.user.dto.request.FirstLoginRequest;
import com.globalnest.be.user.dto.response.UserRecommendResponse;
import com.globalnest.be.user.dto.response.UserRecommendResponseList;
import com.globalnest.be.user.exception.UserNotFoundException;
import com.globalnest.be.user.exception.errorCode.UserErrorCode;
import com.globalnest.be.user.repository.SubscribeRepository;
import com.globalnest.be.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final SubscribeRepository subscribeRepository;

    /**
     * 첫 로그인 시 정보를 받아오는 메소드
     */
    public void registerUser(FirstLoginRequest request, String file_url, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        user.setName(request.name());
        user.setNickName(request.nickname());
        user.setPart(request.part());
        user.setLanguage(request.language());
        user.setProfileImage(file_url);
        user.setAgeRange(request.ageRange());

        userRepository.save(user);
    }

    @Transactional
    public void subscribeUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        subscribeRepository.save(Subscribe.of(targetUser, user));
    }

    public UserRecommendResponseList findUserRecommendList(
            Long userId,
            int size,
            int page
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        List<UserRecommendResponse> userRecommendList =
                userRepository.findUserRecommendList(userId, user.getPart(), user.getAgeRange(), size, page);

        boolean hasNext = userRecommendList.size() == size + 1;

        if (hasNext) {
            userRecommendList = userRecommendList.subList(0, userRecommendList.size() - 1);
        }

        return UserRecommendResponseList.of(hasNext, size, page, userRecommendList);
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
    }
}