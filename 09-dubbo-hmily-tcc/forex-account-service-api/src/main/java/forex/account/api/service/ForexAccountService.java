package forex.account.api.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import forex.account.api.entity.ForexAccount;

/**
 * <p>
 * 用户外汇账号金额表 服务类
 * </p>
 *
 * @author nero
 * @since 2021-06-08
 */
public interface ForexAccountService {

    Object exchangeMoney(ForexAccount forexAccount);

    Object save(ForexAccount forexAccount);

    Object remove(QueryWrapper<ForexAccount> queryWrapper);
}
