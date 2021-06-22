package cn.schoolwow.quickdao;

import org.aeonbits.owner.Config;

import static org.aeonbits.owner.Config.DisableableFeature.PARAMETER_FORMATTING;

@Config.DisableFeature(PARAMETER_FORMATTING)
@Config.Sources({"file:${user.dir}/account.properties"})
public interface Account extends Config {
    @Key("mysql.jdbc")
    String mysqlJdbc();

    @Key("mysql.username")
    String mysqlUsername();

    @Key("mysql.password")
    String mysqlPassword();

    @Key("postgre.jdbc")
    String postgreJdbc();

    @Key("postgre.username")
    String postgreUsername();

    @Key("postgre.password")
    String postgrePassword();

    @Key("sqlserver.jdbc")
    String sqlserverJdbc();

    @Key("sqlserver.username")
    String sqlserverUsername();

    @Key("sqlserver.password")
    String sqlserverPassword();

    @Key("oracle.jdbc")
    String oracleJdbc();

    @Key("oracle.username")
    String oracleUsername();

    @Key("oracle.password")
    String oraclePassword();
}
