package com.nerotomato.exchange;

import com.nerotomato.exchange.service.ExchangeService;
import forex.account.api.entity.ForexAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.math.BigDecimal;

/**
 * Created by nero on 2021/6/9.
 */
@SpringBootApplication
@ImportResource({"classpath:spring-dubbo-consumer.xml"})
public class ExchangeDemoApplication implements ApplicationRunner {
    @Autowired
    private ExchangeService exchangeService;

    public static void main(String[] args) {
        SpringApplication.run(ExchangeDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        ForexAccount forexAccount = new ForexAccount();
        forexAccount.setUsername("nero");
        forexAccount.setCnyWallet(new BigDecimal("7"));
        forexAccount.setUsWallet(new BigDecimal("-1"));

        ForexAccount forexAccount1 = new ForexAccount();
        forexAccount1.setUsername("dante");
        forexAccount1.setCnyWallet(new BigDecimal("-7"));
        forexAccount1.setUsWallet(new BigDecimal(1));
        exchangeService.exchange(forexAccount, forexAccount1);
    }
}
