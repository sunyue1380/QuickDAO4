package cn.schoolwow.quickdao.util;

import org.aeonbits.owner.Config;

import static org.aeonbits.owner.Config.DisableableFeature.PARAMETER_FORMATTING;

@Config.DisableFeature(PARAMETER_FORMATTING)
@Config.Sources({"file:${user.dir}/daoutil.properties"})
public interface DAOUtilProperties extends Config {
    @Key("source.postgre.jdbc")
    String sourcePostgreJdbc();

    @Key("source.postgre.username")
    String sourcePostgreUsername();

    @Key("source.postgre.password")
    String sourcePostgrePassword();

    @Key("target.postgre.jdbc")
    String targetPostgreJdbc();

    @Key("target.postgre.username")
    String targetPostgreUsername();

    @Key("target.postgre.password")
    String targetPostgrePassword();

    @Key("source.oracle.jdbc")
    String sourceOracleJdbc();

    @Key("source.oracle.username")
    String sourceOracleUsername();

    @Key("source.oracle.password")
    String sourceOraclePassword();

    @Key("target.oracle.jdbc")
    String targetOracleJdbc();

    @Key("target.oracle.username")
    String targetOracleUsername();

    @Key("target.oracle.password")
    String targetOraclePassword();

}
