/*
 * The MIT License
 *
 * Copyright 2020 Maxim Eltratov <Maxim.Eltratov@yandex.ru>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ru.maxeltr.rstpldr.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Maxim Eltratov <Maxim.Eltratov@yandex.ru>
 */
public class Config {

    private static final Logger logger = LogManager.getLogger(Config.class);
    private final Properties properties = new Properties();
    private final Path path;

    Config(String path) {
        this.path = Paths.get(path);
        this.readConfigFromFile();
    }

    public String getProperty(String property, String defaultValue) {
        return this.properties.getProperty(property, defaultValue);
    }

    public void setProperty(String property, String value) {
        this.properties.setProperty(property, value);
        this.saveConfigToFile();
    }

//    public Properties setProperties(Properties properties) {
//        //TODO
//    }
    private void readConfigFromFile() {
        File configFile = new File(this.path.toString());
        try (FileInputStream in = new FileInputStream(configFile);) {
            this.properties.loadFromXML(in);
        } catch (IOException ex) {
            logger.error(String.format("Cannot read configuration from file: %s.%n", this.path), ex);
        }
    }

    private void saveConfigToFile() {
        File configFile = new File(this.path.toString());
        try (FileOutputStream out = new FileOutputStream(configFile);) {
            this.properties.storeToXML(out, "Configuration");
        } catch (IOException ex) {
            logger.error(String.format("Cannot save configuration to file: %s.%n", this.path), ex);
        }
    }
}
