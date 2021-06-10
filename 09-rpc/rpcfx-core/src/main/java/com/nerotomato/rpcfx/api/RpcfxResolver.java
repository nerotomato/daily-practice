package com.nerotomato.rpcfx.api;

public interface RpcfxResolver {

    //Object resolve(String serviceClass);

    <T> T resolve(String serviceClass);

}
