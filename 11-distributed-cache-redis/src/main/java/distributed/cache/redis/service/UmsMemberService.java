package distributed.cache.redis.service;

import distributed.cache.redis.entity.UmsMember;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 会员表 服务类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface UmsMemberService extends IService<UmsMember> {

    Object queryUserByUsername(String username);
}
