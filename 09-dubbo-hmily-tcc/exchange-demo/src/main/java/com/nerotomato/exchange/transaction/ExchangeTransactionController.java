package com.nerotomato.exchange.transaction;

import forex.account.api.entity.ForexAccount;
import forex.account.api.service.ForexAccountService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by nero on 2021/6/9.
 */
@Slf4j
@Api(tags = "ExchangeTransactionController", value = "外币兑换接口")
@RestController
@RequestMapping("/exchange-transaction")
public class ExchangeTransactionController {
    @Autowired
    ForexAccountService forexAccountService;


    @HmilyTCC(confirmMethod = "confirmExchangeOperation", cancelMethod = "cancelExchangeOperation")
    @RequestMapping(value = "/transaction", method = RequestMethod.POST)
    public Object testTransaction(ForexAccount forexAccount1, ForexAccount forexAccount2) {
        Object exchange1 = exchange1(forexAccount1);
        Object exchange2 = exchange2(forexAccount2);
        return exchange1.toString() + "," + exchange2.toString();
    }

    //dubbo调用远程服务
    public Object exchange1(ForexAccount forexAccount) {
        return forexAccountService.exchangeMoney(forexAccount);
    }

    //dubbo调用远程服务
    public Object exchange2(ForexAccount forexAccount) {
        return forexAccountService.exchangeMoney(forexAccount);
    }

    public void confirmExchangeOperation() {
        log.info("confirm the exchange operation.");
    }

    public void cancelExchangeOperation() {
        log.info("cancel the exchange operation!!!");
    }

}
