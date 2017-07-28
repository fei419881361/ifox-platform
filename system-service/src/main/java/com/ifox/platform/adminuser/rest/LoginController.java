package com.ifox.platform.adminuser.rest;

import com.ifox.platform.adminuser.exception.NotFoundAdminUserException;
import com.ifox.platform.adminuser.exception.RepeatedAdminUserException;
import com.ifox.platform.adminuser.request.adminuser.AdminUserLoginRequest;
import com.ifox.platform.adminuser.service.AdminUserService;
import com.ifox.platform.common.rest.BaseController;
import com.ifox.platform.common.rest.response.BaseResponse;
import com.ifox.platform.common.rest.response.TokenResponse;
import com.ifox.platform.utility.common.ExceptionUtil;
import com.ifox.platform.utility.jwt.JWTHeader;
import com.ifox.platform.utility.jwt.JWTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

import static com.ifox.platform.common.constant.RestStatusConstant.*;

@Api(description = "后台用户登陆", basePath = "/")
@Controller
@RequestMapping(value = "/adminUser", headers = {"api-version=1.0"})
public class LoginController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    @Autowired
    private AdminUserService adminUserService;


    @ApiOperation(value = "后台用户登录", notes = "后台用户登录接口")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    TokenResponse login(@ApiParam @RequestBody AdminUserLoginRequest adminUserLoginRequest){
        logger.info("用户登陆:{}", adminUserLoginRequest);
        Boolean validAdminUser = false;
        TokenResponse tokenResponse = new TokenResponse();
        try {
            validAdminUser = adminUserService.validLoginNameAndPassword(adminUserLoginRequest.getLoginName(), adminUserLoginRequest.getPassword());
        } catch (NotFoundAdminUserException | RepeatedAdminUserException e) {
            logger.error(ExceptionUtil.getStackTraceAsString(e));
            tokenResponse.setStatus(NOT_FOUND);
            tokenResponse.setDesc("用户不存在");
            logger.info("登陆异常 loginName:{}", adminUserLoginRequest.getLoginName());
            return tokenResponse;
        }

        if (!validAdminUser) {
            tokenResponse.setStatus(USER_NAME_OR_PASSWORD_ERROR);
            tokenResponse.setDesc("用户名或者密码错误");
            logger.info("用户名或者密码错误 loginName:{}", adminUserLoginRequest.getLoginName());
            return tokenResponse;
        }

        String secret = env.getProperty("jwt.secret");
        try {
            tokenResponse.setStatus(SUCCESS);
            tokenResponse.setDesc("登陆成功");
            String token = JWTUtil.generateJWT(new JWTHeader(), adminUserService.generatePayload(adminUserLoginRequest.getLoginName()), secret);
            tokenResponse.setToken(token);
            logger.info("登陆成功 loginName:{}, token:{}", adminUserLoginRequest.getLoginName(), token);
        } catch (UnsupportedEncodingException e) {
            tokenResponse.setStatus(SERVER_EXCEPTION);
            tokenResponse.setDesc("服务器异常");
            logger.error(ExceptionUtil.getStackTraceAsString(e));
            logger.info("登陆异常 loginName:{}", adminUserLoginRequest.getLoginName());
        }
        return tokenResponse;
    }

    @ApiOperation("校验Token")
    @RequestMapping(value = "/verifyToken", method = RequestMethod.POST)
    @ResponseBody
    BaseResponse verifyToken(@ApiParam String token) {
        logger.info("校验Token : {}", token);
        try {
            JWTUtil.verifyToken(token, env.getProperty("jwt.secret"));
        } catch (Exception e) {
            logger.error(ExceptionUtil.getStackTraceAsString(e));
            logger.info("Token校验失败");
            return new BaseResponse(TOKEN_ERROR, "Token校验失败");
        }
        logger.info("Token校验成功");
        return successBaseResponse("Token校验成功");
    }

}
