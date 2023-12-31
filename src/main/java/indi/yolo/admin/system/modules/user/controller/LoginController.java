package indi.yolo.admin.system.modules.user.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.math.Calculator;
import indi.yolo.admin.system.commons.config.UserContextManager;
import indi.yolo.admin.system.commons.entity.rest.RestResult;
import indi.yolo.admin.system.commons.config.MapCache;
import indi.yolo.admin.system.commons.utils.HttpUtils;
import indi.yolo.admin.system.modules.log.annotation.Log;
import indi.yolo.admin.system.modules.user.entity.LoginDto;
import indi.yolo.admin.system.modules.user.service.ILoginService;
import indi.yolo.admin.system.modules.user.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yoloz
 */
@RestController
public class LoginController {

    @Autowired
    private MapCache mapCache;

    @Resource
    private UserContextManager userContextManager;

    @Resource
    private IUserService userService;

    @Resource
    private ILoginService loginService;

    @Log("登陆")
    @PostMapping("/login")
    public RestResult<Object> userLogin(HttpServletRequest request, @RequestBody LoginDto loginDto) {
        Optional<Object> codeResult = mapCache.get(loginDto.getUuid());
        if (codeResult.isEmpty() || !String.valueOf(codeResult.get()).equals(loginDto.getCode())) {
            mapCache.remove(loginDto.getUuid());
            return RestResult.error("验证码不正确!");
        }
        mapCache.remove(loginDto.getUuid());
        Integer userId = loginService.login(loginDto.getUsername(), loginDto.getPassword());
        if (userId == null) {
            return RestResult.error("账号或密码错误!");
        }
        String token = userContextManager.createToken(userId, loginDto.getUsername());
        String loginIp = HttpUtils.getReqIP(request);
        loginService.updateLoginInfo(loginIp, userId);
        Map<String, Object> map = new HashMap<>(2);
        map.put("token", token);
        map.put("userId", userId);
        return RestResult.success(map);
    }

    @Log("退出")
    @PostMapping("/logout")
    public RestResult<Object> logout() {
        userContextManager.removeSession();
        return RestResult.success("true");
    }

    @GetMapping("/captchaImage")
    public RestResult<Object> captcha() {
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(130, 48, 4, 4);
        //验证码内容为四则运算方式
        captcha.setGenerator(new MathGenerator(1));
        String key = UUID.randomUUID().toString();
        int calculateResult = (int) Calculator.conversion(captcha.getCode());
        mapCache.put(key, calculateResult, 1, TimeUnit.MINUTES);
        Map<String, String> map = new HashMap<>(2);
        map.put("uuid", key);
        map.put("image", "data:image/png;base64," + captcha.getImageBase64());
        return RestResult.success(map);
    }
}
