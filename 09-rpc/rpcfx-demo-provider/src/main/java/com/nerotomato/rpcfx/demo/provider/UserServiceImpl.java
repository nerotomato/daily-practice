package com.nerotomato.rpcfx.demo.provider;

import com.nerotomato.rpcfx.demo.api.User;
import com.nerotomato.rpcfx.demo.api.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id, "KK" + System.currentTimeMillis());
    }
}
