package com.api.app.common.security.service;

import com.api.app.entity.memberbase.MemberBaseDto;
import com.api.app.mapper.memberbase.MemberBaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberBaseMapper memberBaseMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberBaseDto member = memberBaseMapper.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return createUserDetails(member);
    }

    private UserDetails createUserDetails(MemberBaseDto member) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(member.getMemberStatusCode());
        return new User(
                member.getEmail(),
                member.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}
