package com.nerotomto.forex.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.nerotomto.forex.mapper.ForexAccountMapper;
import forex.account.api.entity.ForexAccount;
import forex.account.api.service.ForexAccountService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 用户外汇账号金额表 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-06-08
 */
@Slf4j
@Service(value = "forexAccountService")
public class ForexAccountServiceImpl implements ForexAccountService {

    @Autowired
    ForexAccountMapper forexAccountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HmilyTCC(confirmMethod = "confirmExchange", cancelMethod = "cancelExchange")
    public Object exchangeMoney(ForexAccount forexAccount) {
        UpdateWrapper<ForexAccount> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", forexAccount.getUsername());
        //updateWrapper.setSql("cny_wallet=cny_wallet+#{cnyWallet}");
        //updateWrapper.setSql("us_wallet=us_wallet+#{usWallet}");
        //return forexAccountMapper.update(forexAccount, updateWrapper);
        return forexAccountMapper.exchangeMoney(forexAccount);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmExchange(ForexAccount forexAccount) {
        log.info("======== Hmily TCC confirm the money exchange operation ========");
        log.info("Account info: " + forexAccount.toString());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelExchange(ForexAccount forexAccount) {
        log.info("======== Hmily TCC cancel the money exchange operation ========");
        log.info("Account info: " + forexAccount.toString());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object save(ForexAccount entity) {
        return forexAccountMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object remove(QueryWrapper<ForexAccount> queryWrapper) {
        return forexAccountMapper.delete(queryWrapper);
    }
}

