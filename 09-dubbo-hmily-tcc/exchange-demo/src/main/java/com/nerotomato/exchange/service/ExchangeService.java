package com.nerotomato.exchange.service;

import forex.account.api.entity.ForexAccount;
import org.dromara.hmily.annotation.Hmily;

/**
 * Created by nero on 2021/6/9.
 */
public interface ExchangeService {
    @Hmily
    Object exchange(ForexAccount forexAccount1, ForexAccount forexAccount2);
}
