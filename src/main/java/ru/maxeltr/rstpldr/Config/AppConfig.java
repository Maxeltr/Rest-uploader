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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.LogManager;
import javax.crypto.NoSuchPaddingException;
import org.kohsuke.args4j.Option;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.maxeltr.rstpldr.Service.CryptService;

/**
 *
 * @author Maxim Eltratov <Maxim.Eltratov@yandex.ru>
 */
@Configuration
public class AppConfig {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String CONFIG_PATHNAME = "Configuration.xml";
    public static final byte[] SALT = {1, 2, 3, 4, 5, 6, 7, 8};
    public static final int ITERATION_COUNT = 4000;
    public static final int KEY_LENGTH = 128;
    public static final String URL = "http://176.113.82.112/v1/api/file";
    public static final String URL_GET_TOKEN = "http://176.113.82.112/oauth";

    @Option(name = "-pin", usage = "enter pin to decrypt options")
    public static String pin = "";

    public AppConfig() {
        try {
            LogManager.getLogManager().readConfiguration(
                    AppConfig.class.getResourceAsStream("/logging.properties")
            );
        } catch (IOException | SecurityException ex) {
            System.err.println("Could not setup logger configuration: " + ex.toString());
        }
    }

    @Bean
    public Config config() {
        return new Config(CONFIG_PATHNAME);
    }

    @Bean
    public CryptService cryptService(char[] pin) throws NoSuchAlgorithmException, NoSuchPaddingException {
        return new CryptService();
    }

    @Bean
    public CmdLnParser cmdLnParser() {
        return new CmdLnParser(this);
    }


}
