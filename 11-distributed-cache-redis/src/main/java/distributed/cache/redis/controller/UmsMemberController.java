package distributed.cache.redis.controller;


import distributed.cache.redis.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 会员表 前端控制器
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@RestController
@RequestMapping("/ums-member")
public class UmsMemberController {
    @Autowired
    UmsMemberService umsMemberService;

    @RequestMapping(value = "/queryUser", method = RequestMethod.GET)
    public Object queryUserByUsername(@RequestParam(value = "username") String username) {
        return umsMemberService.queryUserByUsername(username);
    }
}

