package distributed.cache.redis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import distributed.cache.redis.entity.UmsMember;
import distributed.cache.redis.mapper.UmsMemberMapper;
import distributed.cache.redis.service.UmsMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@Service
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberMapper, UmsMember> implements UmsMemberService {

    @Autowired
    UmsMemberMapper umsMemberMapper;

    @Override
    public Object queryUserByUsername(String username) {
        QueryWrapper<UmsMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return umsMemberMapper.selectOne(queryWrapper);
    }
}
