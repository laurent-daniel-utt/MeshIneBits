package meshIneBits.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class CustomLogger {
    private Logger logger;

    public CustomLogger(Class<?> cls) {
        this.logger = LogManager.getLogger(cls);
        //TODO normaly we need to set logger level by system configuration file
        Configurator.setAllLevels(LogManager.getRootLogger().getName(),Level.DEBUG);
    }
    public void logINFOMessage(String mess){
        logger.info(mess);
    }
    public void logDEBUGMessage(String mess){
        logger.debug(mess);
    }
    public void logWARNMessage(String mess){
        logger.warn(mess);
    }
    public void logERRORMessage(String mess){
        logger.error(mess);
    }
    public void logFATALMessage(String mess) {
        logger.fatal(mess);
    }

    public static void main(String[] args) {

    }

}
