package com.trinity.ctc.domain.user.service;

import com.trinity.ctc.domain.user.dto.CustomUserDetails;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    /**
     * kakaoId 기반으로 UserDetails 획득
     * @param kakaoId
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String kakaoId) throws UsernameNotFoundException {

        User user = userRepository.findByKakaoId((Long.parseLong(kakaoId))).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        return new CustomUserDetails(user);
    }
}
