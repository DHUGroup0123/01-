package com.team.team_work.memberA.user.controller;
import com.team.team_work.common.*;
import com.team.team_work.config.MemberASecurityFilter;
import com.team.team_work.memberA.user.dto.*;
import com.team.team_work.memberA.user.entity.UserAccount;
import com.team.team_work.memberA.user.service.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;

@RestController
public class UserController {
    private final UserService users;
    private final CaptchaService captcha;
    private final LoginThrottle throttle;
    public UserController(UserService users,CaptchaService captcha,LoginThrottle throttle) {this.users=users;this.captcha=captcha;this.throttle=throttle;}
    private String csrf(HttpSession session) {
        synchronized(session) {String value=(String)session.getAttribute(MemberASecurityFilter.CSRF);if(value==null) {value=UUID.randomUUID().toString();session.setAttribute(MemberASecurityFilter.CSRF,value);}return value;}
    }
    @GetMapping("/api/csrf")
    public ApiResponse<Map<String,String>> csrf(HttpSession session,HttpServletResponse response) {response.setHeader("Cache-Control","no-store");return ApiResponse.success(Map.of("token",csrf(session)));}
    @GetMapping(value="/api/captcha",produces=MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> captcha(HttpSession session) {return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(captcha.image(session));}
    @PostMapping("/api/users/register")
    public ApiResponse<UserViews.Profile> register(@Valid @RequestBody UserRequests.Credentials input,HttpServletRequest request) {
        throttle.attempt(request.getRemoteAddr(),input.loginName());captcha.consume(request.getSession(),input.captcha());return ApiResponse.success(users.register(input));
    }
    @PostMapping("/api/users/login")
    public ApiResponse<UserViews.Profile> login(@Valid @RequestBody UserRequests.Credentials input,HttpServletRequest request) {
        throttle.attempt(request.getRemoteAddr(),input.loginName());
        HttpSession session=request.getSession();captcha.consume(session,input.captcha());UserAccount user=users.login(input);
        request.changeSessionId();session.setAttribute(SessionKeys.MALL_USER,new SessionUser(user.getUserId(),user.getLoginName(),user.getNickname()));
        session.setAttribute(MemberASecurityFilter.CSRF,UUID.randomUUID().toString());
        return ApiResponse.success(UserViews.Profile.of(user));
    }
    @PostMapping("/api/users/logout")
    public ApiResponse<Void> logout(HttpSession session) {session.invalidate();return ApiResponse.success(null);}
    private Long id(HttpSession session) {return ((SessionUser)session.getAttribute(SessionKeys.MALL_USER)).userId();}
    @GetMapping("/api/users/me")
    public ApiResponse<UserViews.Profile> me(HttpSession session) {return ApiResponse.success(UserViews.Profile.of(users.active(id(session))));}
    @PutMapping("/api/users/me")
    public ApiResponse<UserViews.Profile> update(@Valid @RequestBody UserRequests.Profile input,HttpSession session) {
        UserViews.Profile result=users.updateProfile(id(session),input);
        session.setAttribute(SessionKeys.MALL_USER,new SessionUser(Long.valueOf(result.userId()),result.loginName(),result.nickname()));return ApiResponse.success(result);
    }
    @GetMapping("/api/addresses")
    public ApiResponse<List<UserViews.Address>> addresses(HttpSession session) {return ApiResponse.success(users.addresses(id(session)));}
    @PostMapping("/api/addresses")
    public ApiResponse<UserViews.Address> create(@Valid @RequestBody UserRequests.Address input,HttpSession session) {return ApiResponse.success(users.saveAddress(id(session),null,input));}
    @PutMapping("/api/addresses/{addressId}")
    public ApiResponse<UserViews.Address> edit(@PathVariable Long addressId,@Valid @RequestBody UserRequests.Address input,HttpSession session) {return ApiResponse.success(users.saveAddress(id(session),addressId,input));}
    @DeleteMapping("/api/addresses/{addressId}")
    public ApiResponse<Void> delete(@PathVariable Long addressId,HttpSession session) {users.deleteAddress(id(session),addressId);return ApiResponse.success(null);}
}
