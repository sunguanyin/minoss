package io.javac.minoss.minoss.minossappservice.impl;

import io.javac.minoss.minoss.minossappservice.VertxUserService;
import io.javac.minoss.minosscommon.bcrypt.PasswordEncoder;
import io.javac.minoss.minosscommon.cache.StringCacheStore;
import io.javac.minoss.minosscommon.constant.CacheConst;
import io.javac.minoss.minosscommon.exception.MinOssMessageException;
import io.javac.minoss.minosscommon.plugin.JwtPlugin;
import io.javac.minoss.minosscommon.toolkit.Kv;
import io.javac.minoss.minosscommon.toolkit.id.IdGeneratorCore;
import io.javac.minoss.minossservice.vertx.VertxRequest;
import io.javac.minoss.minossdao.model.UserModel;
import io.javac.minoss.minossdao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * @author pencilso
 * @date 2020/1/24 3:21 下午
 */
@Slf4j
@Validated
@Service
public class VertxUserServiceImpl implements VertxUserService {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtPlugin jwtPlugin;
    @Autowired
    private StringCacheStore stringCacheStore;

    @Override
    public void userLogin(@NotNull VertxRequest vertxRequest, @NotEmpty String loginName, @NotEmpty String loginPassword) {
        UserModel userModel = userService.getByLoginName(loginName).orElseThrow(() -> new MinOssMessageException("该用户不存在，请与管理换联系！"));
        //check login password
        if (!passwordEncoder.matches(loginPassword, userModel.getLoginPassword())) {
            throw new MinOssMessageException("账户或者密码错误，请尝试稍后重试！");
        }
        //generator new salt
        String jwtSalt = IdGeneratorCore.generatorUUID();
        //generator new accesstoken
        String accesstoken = jwtPlugin.generateToken(userModel.getMid(), jwtSalt).orElseThrow(() -> new MinOssMessageException("生成token令牌出错，请尝试稍后重试！"));
        //update user accesstoken and  salt
        userService.updateModelByMid(
                new UserModel().setJwtSalt(jwtSalt).setJwtToken(accesstoken).setVersion(userModel.getVersion()),
                userModel.getMid());
        vertxRequest.buildVertxRespone().responeSuccess(Kv.stringStringKv().set("accesstoken", accesstoken));
        //put new jwt salt
        stringCacheStore.put(CacheConst.CACHE_OBJECT_JWT_SALT + userModel.getMid(), jwtSalt);
        log.debug("user login success by  [{}]  new accesstoken [{}]  jwtSalt [{}]", loginName, accesstoken, jwtSalt);
    }

    @Override
    public Optional<UserModel> getByuMid(@NotNull Long uMid) {
        return Optional.ofNullable(userService.getByMid(uMid));
    }
}
