package cn.schoolwow.quickdao.builder.dcl;

import cn.schoolwow.quickdao.builder.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDCLBuilder extends AbstractSQLBuilder implements DCLBuilder{
    protected Logger logger = LoggerFactory.getLogger(AbstractDCLBuilder.class);

    public AbstractDCLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }
}
