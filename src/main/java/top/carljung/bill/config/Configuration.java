package top.carljung.bill.config;

import com.googlecode.protobuf.format.JsonFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wangchao
 */
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    public static final Configuration instance = new Configuration();
    private BillProto.Config config;
    
    private Configuration(){
    }
    
    public BillProto.Config getConfig(){
        return config;
    }
    
    public BillProto.DB getDBConfig(){
        return getConfig().getDb();
    }
    public BillProto.WX getWXConfig(){
        return getConfig().getWx();
    }
    
    public BillProto.Server getServerConfig(){
        return getConfig().getServer();
    }
    
    public void readConfig() throws FileNotFoundException, IOException{
        JsonFormat jsonFormat = new JsonFormat();
        File configJson = new File("config/config.json");
        BillProto.Config.Builder configBuilder = BillProto.Config.newBuilder();
        jsonFormat.merge(new FileInputStream(configJson), configBuilder);
        config = configBuilder.build();
    }
}
