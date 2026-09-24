package com.team.student_calendar.security.service;

import com.team.student_calendar.common.enums.UserRole;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.UserErrorCode;
import com.team.student_calendar.dto.UserRequestDTO;
import com.team.student_calendar.dto.UserInfoDTO;
import com.team.student_calendar.security.entity.UserEntity;
import com.team.student_calendar.security.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterTokenService registerTokenService;


    /**
     * 유저 회원가입
     * @param dto 회원가입 정보
     */
    @Transactional
    public void join(UserRequestDTO dto) {

        registerTokenService.validateAndConsume(dto.token());

        String name = dto.name();
        String username = dto.username();
        String password = dto.password();


        // 닉네임 중복 체크
        boolean existingName = userRepository.existsByUsername(username);
        if (existingName) {
            throw new BaseException(UserErrorCode.DUPLICATED_NAME);
        }
        // 아이디 중복 체크
        boolean existingUsername = userRepository.existsByUsername(username);
        if (existingUsername) {
            throw new BaseException(UserErrorCode.DUPLICATED_USERNAME);
        }

        UserEntity entity = new UserEntity();
        entity.setName(name);
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(password));
        entity.setRole(UserRole.USER);

        userRepository.save(entity);
    }


    /**
     * 로그인된 유저 정보 조회
     * @param username 로그인된 유저의 아이디
     */
    public UserInfoDTO getMyInfo(String username) {

        UserEntity entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        return new UserInfoDTO(entity.getUsername(), entity.getName(), entity.getRole());
    }


    // AuthenticationManger가 AuthenticationProvider를 호출하면서 해당 메서드를 호출함
    // AuthenticationProvider가 DB에서 가져온 것과 비교해서 로그인 처리함
    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        UserEntity entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(UserErrorCode.INVALID_LOGIN_INFO.getMessage()));

        return User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .roles(entity.getRole().name())
                .build();
    }
}
