package com.nerotomto.forex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import forex.account.api.entity.ForexAccount;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 用户外汇账号金额表 Mapper 接口
 * </p>
 *
 * @author nero
 * @since 2021-06-08
 */
public interface ForexAccountMapper extends BaseMapper<ForexAccount> {
    /**
     * 自定义sql
     */
    @Update("update forex_account set us_wallet = us_wallet + #{usWallet}, cny_wallet = cny_wallet +" +
            "#{cnyWallet} where us_wallet >= #{usWallet} and cny_wallet >= #{cnyWallet} and username = #{username}")
    int exchangeMoney(ForexAccount forexAccount);

}
