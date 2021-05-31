package com.nerotomato.xa.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.nerotomato.xa.entity.Account;
import com.nerotomato.xa.service.AccountService;
import io.swagger.annotations.Api;
import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * <p>
 * 用户金额操作api
 * </p>
 *
 * @author nero
 * @since 2021-05-30
 */
@Api(tags = "AccountController", value = "用户金额操作api")
@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    AccountService accountService;

    @ShardingTransactionType(value = TransactionType.LOCAL)
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Object save(@RequestBody Account account) {
        return accountService.save(account);
    }

    @ShardingTransactionType(value = TransactionType.LOCAL)
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Object delete(@RequestBody Account account) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", account.getUsername());
        return accountService.remove(queryWrapper);
    }

    @ShardingTransactionType(value = TransactionType.LOCAL)
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Object update(@RequestBody Account account) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", account.getUsername());
        return accountService.update(account, updateWrapper);
    }

    @ShardingTransactionType(value = TransactionType.LOCAL)
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/find", method = RequestMethod.POST)
    public Object find(@RequestBody Account account) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", account.getUsername());
        return accountService.getOne(queryWrapper);
    }

    /**
     * @param money    汇款金额
     * @param remitter 汇款人
     * @param payee    收款人
     */
    @ShardingTransactionType(value = TransactionType.XA)
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public Object transferAccount(@RequestParam(name = "money") BigDecimal money,
                                  @RequestParam(value = "remitter") String remitter,
                                  @RequestParam(value = "payee") String payee) throws Exception {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", remitter);
        //汇款人账号
        Account remitterAccount = accountService.getOne(queryWrapper);
        if (null == remitterAccount) {
            throw new Exception("remitter: " + remitter + " is not exist!");
        }
        queryWrapper.clear();
        queryWrapper.eq("username", payee);
        //收款人账号
        Account payeeAccount = accountService.getOne(queryWrapper);
        if (null == payeeAccount) {
            throw new Exception("payee: " + payee + " is not exist!");
        }
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", remitter);
        //汇款人账号扣除金额-money
        if (remitterAccount.getMoney().compareTo(money) == -1) {
            throw new Exception(remitter + " doesn't have enough money!");
        }
        remitterAccount.setMoney(remitterAccount.getMoney().subtract(new BigDecimal(String.valueOf(money))));
        boolean remitterResult = accountService.update(remitterAccount, updateWrapper);

        boolean payeeResult = transferMoney(payeeAccount, money);

        if (remitterResult && payeeResult) {
            return "Transfer money successfully!";
        } else {
            throw new Exception("Failed transfering the money!");
        }
    }

    @ShardingTransactionType(value = TransactionType.XA)
    @Transactional(rollbackFor = Exception.class)
    private boolean transferMoney(Account payeeAccount, BigDecimal money) throws Exception {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", payeeAccount.getUsername());
        //收款人账号添加金额-money
        payeeAccount.setMoney(payeeAccount.getMoney().add(new BigDecimal(String.valueOf(money))));
        boolean payeeResult = accountService.update(payeeAccount, updateWrapper);
        //test exception
        //payeeResult = false;

        if (payeeResult) {
            return payeeResult;
        } else {
            throw new Exception("Payee failed receiving the money!");
        }
    }

}



