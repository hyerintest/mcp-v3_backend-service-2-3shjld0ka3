package com.tlc.controller;

import com.tlc.annotation.AnonymousCallable;
import com.tlc.model.email.EmailAuth;
import com.tlc.response.ResponseObject;
import com.tlc.service.LoginService;
import com.tlc.exception.InvalidParameterException;
import com.tlc.exception.AuthException;
import com.tlc.exception.NotAllowedMethodException;
import com.tlc.exception.ServiceUnavailableException;
import com.tlc.exception.PermissionDeniedException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.concurrent.Callable;

@Tag(name = "Login", description = "로그인 기능을 수행합니다.")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "정상 응답", content = @Content(schema = @Schema(implementation = ResponseObject.class))),
        @ApiResponse(responseCode = "400", description = "권한 없음", content = @Content(schema = @Schema(implementation = InvalidParameterException.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = AuthException.class))),
        @ApiResponse(responseCode = "405", description = "허용되지 않은 메서드", content = @Content(schema = @Schema(implementation = NotAllowedMethodException.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ServiceUnavailableException.class))),
        @ApiResponse(responseCode = "401", description = "권한 없음", content = @Content(schema = @Schema(implementation = PermissionDeniedException.class)))
    })
@RequiredArgsConstructor
@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
public class LoginController {
	@Autowired LoginService service;

    @AnonymousCallable
    @RequestMapping(value = "/turaco/login", method = RequestMethod.POST)
    public Callable<ResponseObject> login(@RequestBody EmailAuth auth, HttpSession session) {
        auth.setSessionId(session.getId());
        return () -> service.doLogin(auth);
    }
}
