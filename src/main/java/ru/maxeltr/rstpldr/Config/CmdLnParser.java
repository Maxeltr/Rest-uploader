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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import ru.maxeltr.rstpldr.Service.CryptService;

/**
 *
 * @author Maxim Eltratov <Maxim.Eltratov@yandex.ru>
 */
public class CmdLnParser {

    private static final Logger logger = Logger.getLogger(CmdLineParser.class.getName());

    @Option(name = "-pin", usage = "enter pin to decrypt options")
    private String pin = "";

    @Option(name = "-save", usage = "write options to configuration file")
    private boolean shouldSave = false;

    @Option(name = "-id", usage = "enter client ID")
    private void setClientId(String value) {
        this.config.setProperty("ClientId", this.cryptService.encrypt(value.getBytes(), this.pin.toCharArray()));
    }

    @Option(name = "-secret", usage = "enter client secret")
    private void setClientSecret(String value) {
        this.config.setProperty("ClientSecret", this.cryptService.encrypt(value.getBytes(), this.pin.toCharArray()));
    }

    @Option(name = "-dir", usage = "enter log directory")
    private void setLogDir(String value) {
        this.config.setProperty("LogDir", this.cryptService.encrypt(value.getBytes(), this.pin.toCharArray()));
    }

    @Option(name = "-logprog", usage = "enter log programm name")
    private void setLogProgName(String value) {
        this.config.setProperty("LogProgName", this.cryptService.encrypt(value.getBytes(), this.pin.toCharArray()));
    }


    @Option(name = "-key2", usage = "enter key to encrypt files (this is key2)")
    private void setKey2(String value) {
        this.config.setProperty("Key2", this.cryptService.encrypt(value.getBytes(), this.pin.toCharArray()));
    }

    @Option(name = "-fileUploadUrl", usage = "enter url to upload files")
    private void setFileUploadUrl(String value) {
        this.config.setProperty("UrlUploadFile", this.cryptService.encrypt(value.getBytes(), this.pin.toCharArray()));
    }

    @Option(name = "-authUrl", usage = "enter url to authenticate")
    private void setAuthUrl(String value) {
        this.config.setProperty("UrlGetToken", this.cryptService.encrypt(value.getBytes(), this.pin.toCharArray()));
    }

    @Option(name = "-subdir", handler = StringArrayOptionHandler.class, usage = "enter log subdirectories in the log directory")
    private List<String> listSubDirs = new ArrayList<String>();

    private void setSubDir(List<String> value) {
        String subDirs = "";
        for (int i = 0; i < value.size(); i++) {
            subDirs = subDirs.concat(value.get(i) + " ");
        }
        this.config.setProperty("SubDirs", this.cryptService.encrypt(subDirs.trim().getBytes(), this.pin.toCharArray()));
    }

    CmdLineParser parser;

    Config config;

    CryptService cryptService;

    CmdLnParser(Config config, CryptService cryptService) {
        this.parser = new CmdLineParser(this);
        this.config = config;
        this.cryptService = cryptService;
    }

    public void parse(String[] args) {
        try {
            this.parser.parseArgument(args);
        } catch (CmdLineException ex) {
            logger.log(Level.SEVERE, "Cannot parse command line arguments", ex);
        }

        this.cryptService.setPin(this.pin.toCharArray());
        this.setSubDir(this.listSubDirs);

        if (this.shouldSave == true) {
            this.config.saveConfigToFile();
        }
    }

}
