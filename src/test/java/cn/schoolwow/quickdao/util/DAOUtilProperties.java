package cn.schoolwow.quickdao.util;

import org.aeonbits.owner.Config;

import static org.aeonbits.owner.Config.DisableableFeature.PARAMETER_FORMATTING;

@Config.DisableFeature(PARAMETER_FORMATTING)
@Config.Sources({"file:${user.dir}/daoutil.properties"})
public interface DAOUtilProperties extends Config {
    @Key("source.jdbcDriver")
    String sourceJdbcDriver();

    @Key("source.jdbcUrl")
    String sourceJdbcUrl();

    @Key("source.username")
    String sourceUsername();

    @Key("source.password")
    String sourcePassword();

    @Key("target.jdbcDriver")
    String targetJdbcDriver();

    @Key("target.jdbcUrl")
    String targetJdbcUrl();

    @Key("target.username")
    String targetUsername();

    @Key("target.password")
    String targetPassword();
}