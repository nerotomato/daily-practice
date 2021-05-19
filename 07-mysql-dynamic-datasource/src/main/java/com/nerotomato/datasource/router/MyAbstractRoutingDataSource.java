package com.nerotomato.datasource.router;

import com.nerotomato.datasource.context.DataSourceContextHolder;
import com.nerotomato.datasource.type.DynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 重写 determineCurrentLookupKey 方法，
 * 返回所使用的数据源的Key(master/slave)给到 resolvedDataSources，
 * 从而通过Key从resolvedDataSources里拿到其对应的DataSource
 * Created by nero on 2021/5/19.
 */
@Slf4j
public class MyAbstractRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        DynamicDataSource dynamicDataSource = DataSourceContextHolder.getDynamicDataSource();
        if (dynamicDataSource == null) {
            log.debug("dataSource not found,use default dataSource: {}" + DynamicDataSource.MASTER);
            dynamicDataSource = DynamicDataSource.MASTER;
        }
        log.trace("use {} as dataSource", dynamicDataSource);
        return dynamicDataSource;
    }
}
