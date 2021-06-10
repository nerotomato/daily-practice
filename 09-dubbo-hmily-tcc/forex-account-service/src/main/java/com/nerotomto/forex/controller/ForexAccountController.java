package com.nerotomto.forex.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import forex.account.api.entity.ForexAccount;
import forex.account.api.service.ForexAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户外汇账号金额表 前端控制器
 * </p>
 *
 * @author nero
 * @since 2021-06-08
 */
@RestController
@RequestMapping("/forex-account")
public class ForexAccountController {

    @Autowired
    ForexAccountService forexAccountService;

    /**
     * 兑换金额
     *
     * @param forexAccount - 账号金额信息
     */
    @RequestMapping(value = "/exchange", method = RequestMethod.POST)
    public Object exchangeMoney(@RequestBody ForexAccount forexAccount) {
        return forexAccountService.exchangeMoney(forexAccount);
    }

    /**
     * 新增用户账户信息
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Object save(@RequestBody ForexAccount forexAccount) {
        return forexAccountService.save(forexAccount);
    }

    /**
     * 删除用户账户信息
     *
     * @param username - 用户名
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Object delete(@RequestParam(value = "username") String username) {
        QueryWrapper<ForexAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return forexAccountService.remove(queryWrapper);
    }
}

