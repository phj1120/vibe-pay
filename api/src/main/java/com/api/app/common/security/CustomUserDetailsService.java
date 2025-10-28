package com.api.app.common.security;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.entity.MemberBase;
import com.api.app.repository.member.MemberBaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberBaseMapper memberBaseMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);

        if (member == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }

        return new User(member.getEmail(), member.getPassword(), new ArrayList<>());
    }
}
