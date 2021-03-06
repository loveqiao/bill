package top.carljung.bill.server;

import top.carljung.bill.server.probe.AccessLoggerProbe;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import liquibase.exception.LiquibaseException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.HttpServerProbe;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.monitoring.MonitoringConfig;
import org.slf4j.LoggerFactory;
import top.carljung.bill.config.Configuration;
import top.carljung.bill.db.DBFactory;
import top.carljung.bill.db.LiquibaseInit;
import top.carljung.bill.proto.ConfigStore;
import top.carljung.bill.server.handler.UploadHandler;

/**
 *
 * @author wangchao
 */
public class ServerStarter {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ServerStarter.class);;
    private ServerStarter(){
    }
    
    public static void main(String[] args){
        ServerStarter starter = new ServerStarter();
        starter.init();
        
        ConfigStore.Server serverConfig = Configuration.instance.getServerConfig();
       
        int port = serverConfig.getPort();
        final URI BASE_URI = URI.create("http://0.0.0.0:" + port + "/");
        final HttpServer server = GrizzlyHttpServerFactory2.createHttpServer(BASE_URI,
                    new GrizzlyHttpContainer2(Application.getInstance()), false, null, false);
        final ServerConfiguration config = server.getServerConfiguration();
        config.addHttpHandler(new UploadHandler(), "/upload");
        MonitoringConfig<HttpServerProbe> webServerConfig = config.getMonitoringConfig().getWebServerConfig();
        webServerConfig.addProbes(new AccessLoggerProbe());
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        try {
            server.start();
        } catch (IOException ex) {
            logger.warn("server start fail", ex);
            System.exit(1);
        }
    }
    
    public void init(){
        try {
            Configuration.instance.loadConfig();
            new LiquibaseInit().init();
            DBFactory.init();
            reConfigLogback();
        } catch (IOException | LiquibaseException | SQLException ex) {
            logger.warn("server start fail", ex);
            System.exit(1);
        } catch (JoranException ex) {
            logger.warn("reset logback configuartion file fail", ex);
            System.exit(1);
        }
    }
    
    //对此方法的调用应该放在后面，有些框架（比如liquibase）会重写logbcak配置
    public void reConfigLogback() throws JoranException{
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        ConfigStore.Server serverConfig = Configuration.instance.getServerConfig();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        configurator.doConfigure(new File(serverConfig.getLogback())); // loads logback file
    }    
}
