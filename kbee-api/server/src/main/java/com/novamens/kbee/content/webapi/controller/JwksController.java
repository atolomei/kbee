package com.novamens.kbee.content.webapi.controller;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.kbee.content.webapi.type.IUserAdapter;
import com.novamens.kbee.security.jwt.InternalPublicKeyFactory;
import com.novamens.security.AuthToken;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import kbee.api.model.ApiRefreshResponse;
import kbee.api.model.ApiUser;
import kbee.api.model.ApiLoginResponse;
import kbee.api.model.ApiToken;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

@RestController
public class JwksController {

    private final JWKSet jwkSet;

    public JwksController() throws Exception {

        RSAPublicKey publicKey =
            new InternalPublicKeyFactory().createPublicKey();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .keyID("kbee-internal-key-1")
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .build();

        this.jwkSet = new JWKSet(rsaKey);
    }

    @GetMapping(
        value = "/.well-known/jwks.json",
        produces = "application/json"
    )
    public Map<String, Object> getJwks() {
        return jwkSet.toJSONObject();
    }
    

    @ApiOperation(value = "Login")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 401, message = "Invalid credentials")
    })
    @RequestMapping(
        value = "/auth/login",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiLoginResponse> login(
            @RequestBody LoginRequest request) {

        AuthToken token = ServiceLocator
            .getService(SecurityService.class)
            .createToken(
                request.getUsername(),
                request.getPassword()
            );

        ApiToken itoken = new ApiToken();
        itoken.setValue(token.getTokenValue());
        itoken.setRefreshToken(token.getRefreshToken());

        UserProfile userProfile = ServiceLocator
            .getService(UserService.class)
            .getSessionUserProfile();

        Person person = userProfile.getPerson();

        ApiUser iuser = new IUserAdapter().adapt(person);

        ApiLoginResponse response = new ApiLoginResponse();
        response.setUser(iuser);
        response.setToken(itoken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    

    @ApiOperation(value = "Refresh access token")
    @ApiResponses(value = {
    	    @ApiResponse(code = 200, message = "Ok"),
    	    @ApiResponse(code = 401, message = "Invalid refresh token")
    	})
    @RequestMapping(
        value = "/auth/refresh",
        method = RequestMethod.POST
    )
    public ResponseEntity<ApiRefreshResponse> refresh(
            @RequestParam("refresh_token") String refreshToken) {

        ApiRefreshResponse response = null;
//            ServiceLocator.getService(SecurityService.class)
//                .refreshToken(refreshToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}