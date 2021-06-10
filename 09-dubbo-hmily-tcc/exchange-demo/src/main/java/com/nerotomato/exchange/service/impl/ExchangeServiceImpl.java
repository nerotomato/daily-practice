package com.nerotomato.exchange.service.impl;

import com.nerotomato.exchange.service.ExchangeService;
import forex.account.api.entity.ForexAccount;
import forex.account.api.service.ForexAccountService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by nero on 2021/6/9.
 */
@Slf4j
@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    ForexAccountService forexAccountService;

    @Override
    @HmilyTCC(confirmMethod = "confirmExchangeOperation", cancelMethod = "cancelExchangeOperation")
    public Object exchange(ForexAccount forexAccount1, ForexAccount forexAccount2) {
        Object result1 = exchangeRemote(forexAccount1);
        Object result2 = exchangeRemote(forexAccount2);
        return result1.toString() + "," + result2.toString();
    }

    /**
     * dubbo调用远程服务
     */
    private Object exchangeRemote(ForexAccount forexAccount) {
        return forexAccountService.exchangeMoney(forexAccount);
    }

    public boolean confirmExchangeOperation(ForexAccount forexAccount1, ForexAccount forexAccount2) {
        log.info("confirm the exchange operation.");
        return true;
    }

    public boolean cancelExchangeOperation(ForexAccount forexAccount1, ForexAccount forexAccount2) {
        log.info("cancel the exchange operation!!!");
        return true;
    }
}
