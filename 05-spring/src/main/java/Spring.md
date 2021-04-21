#1.Spring bean 的生命周期
##1.1.创建对象(实例化) createBeanInstance方法
##1.2.属性赋值(设置对象属性) populateBean方法
##1.3.初始化 initializeBean方法
###1.3.1. 检查aware相关接口并设置相关依赖
Aware 接口有： BeanNameAware：注入当前 bean 对应 beanName； BeanClassLoaderAware：注入加载当前 bean 的 ClassLoader； BeanFactoryAware：注入 当前BeanFactory容器 的引用。
###1.3.2. BeanPostProcessor前置处理
BeanPostProcessor 是 Spring 为修改 bean提供的强大扩展点，其可作用于容器中所有 bean。
###1.3.3. 若实现InitializingBean接口，则调用afterPropertiesSet方法。若配置自定义的init-method方法则执行
InitializingBean 和 init-method 是 Spring 为 bean 初始化提供的扩展点。
###1.3.4. BeanPostProcessor后置处理
BeanPostProcessor 是 Spring 为修改 bean提供的强大扩展点，其可作用于容器中所有 bean。
##1.4.注销接口注册 registerDisposableBeanIfNecessary方法