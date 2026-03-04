package com.team.student_calendar.service;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.UserErrorCode;
import com.team.student_calendar.entity.UserEntity;
import com.team.student_calendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SelectUserService {

    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public UserEntity findUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    }
}
