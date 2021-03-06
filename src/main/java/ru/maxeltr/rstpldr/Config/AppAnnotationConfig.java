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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.logging.LogManager;
import javax.crypto.NoSuchPaddingException;
import org.kohsuke.args4j.Option;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.maxeltr.rstpldr.Service.CryptService;
import ru.maxeltr.rstpldr.Service.ExitChecker;
import ru.maxeltr.rstpldr.Service.RestUploadService;
import ru.maxeltr.rstpldr.Service.SendFilesTask;

/**
 *
 * @author Maxim Eltratov <Maxim.Eltratov@yandex.ru>
 */
@Configuration
public class AppAnnotationConfig {

    public static final String CONFIG_PATHNAME = "Configuration.xml";

    public AppAnnotationConfig() throws IOException {
        try {
            LogManager.getLogManager().readConfiguration(AppAnnotationConfig.class.getResourceAsStream("/logging.properties")
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
    public CryptService cryptService() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return new CryptService();
    }

    @Bean
    public CmdLnParser cmdLnParser(Config config, CryptService cryptService) {
        return new CmdLnParser(config, cryptService);
    }

    @Bean
    public ExitChecker exitChecker() throws IOException {
        return new ExitChecker();
    }

    @Bean
    public RestUploadService restUploadService(Config config, CryptService cryptService) {
        return new RestUploadService(config, cryptService);
    }

    @Bean
    public SendFilesTask sendFilesTask(Config config, CryptService cryptService, RestUploadService restUploadService) {
        return new SendFilesTask(config, cryptService, restUploadService);
    }
}
