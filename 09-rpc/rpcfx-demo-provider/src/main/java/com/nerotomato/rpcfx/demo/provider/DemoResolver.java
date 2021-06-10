package com.nerotomato.rpcfx.demo.provider;

import com.nerotomato.rpcfx.api.RpcfxResolver;
import com.nerotomato.rpcfx.demo.provider.factory.MyBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DemoResolver<T> implements RpcfxResolver, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public T resolve(String serviceClass) {
        T bean = null;
        try {
            bean = (T) MyBeanFactory.getBean(serviceClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    /*@Override
    public Object resolve(String serviceClass) {
        return this.applicationContext.getBean(serviceClass);
    }*/

}
