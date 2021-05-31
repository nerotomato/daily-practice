package com.nerotomato.xa.service.impl;

import com.nerotomato.xa.entity.Account;
import com.nerotomato.xa.mapper.AccountMapper;
import com.nerotomato.xa.service.AccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户金额表 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-05-30
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

}
