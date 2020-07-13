import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigFile
{
    private InputStream inputStream;
    private Properties prop;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public ConfigFile() throws IOException {
        this.getPropValues();
    }

    public Properties getProp() {
        return prop;
    }

    public String getProp(String prop){
        return getProp().getProperty(prop);
    }

    public void printStringProp() {
        getProp().stringPropertyNames().forEach(log::info);
    }

    private void getPropValues() throws IOException {
        try {
            prop = new Properties();
            String propFileName = "config.properties";
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Archivo properties '" + propFileName + "' No se encuentra");
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            assert inputStream != null;
            inputStream.close();
        }
    }

}
