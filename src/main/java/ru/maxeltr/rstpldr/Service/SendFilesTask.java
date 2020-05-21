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
package ru.maxeltr.rstpldr.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.maxeltr.rstpldr.Config.Config;
import ru.maxeltr.rstpldr.Main;

/**
 *
 * @author Maxim Eltratov <Maxim.Eltratov@yandex.ru>
 */
public class SendFilesTask extends TimerTask {

    private final RestUploadService uploadService;

    private final CryptService cryptService;

    private final Config config;

    private static final Logger logger = Logger.getLogger(SendFilesTask.class.getName());

    public SendFilesTask(Config config, CryptService cryptService, RestUploadService uploadService) {
        this.uploadService = uploadService;
        this.cryptService = cryptService;
        this.config = config;
    }

    @Override
    public void run() {
        String logDir = new String(this.cryptService.decrypt(this.config.getProperty("LogDir", "")));
        ArrayList<File> files = this.listFiles(new File(logDir));

        String[] subDirs = new String(cryptService.decrypt(config.getProperty("SubDirs", ""))).split(" ");
        for (String subDir : subDirs) {
            files.addAll(this.listFiles(new File(logDir + File.separator + subDir)));
        }

        if (files.isEmpty()) {
            logger.log(Level.INFO, String.format("There is no files to sent.%n"));

            return;
        }

        String token = this.uploadService.authenticate();
        if (token.isEmpty()) {
            logger.log(Level.INFO, String.format("Cannot authenticate.%n"));

            return;
        }

        ArrayList<File> postedFiles = this.sendFiles(files);
        this.deleteFiles(postedFiles);
    }

    private void deleteFiles(ArrayList<File> files) {
        files.forEach((file) -> {
            boolean isDel = file.delete();
            if (isDel == true) {
                logger.log(Level.INFO, String.format("%s was deleted.%n", file.getName()));
            } else {
                logger.log(Level.SEVERE, String.format("%s was not deleted.%n", file.getName()));
            }
        });

    }

    private ArrayList<File> sendFiles(ArrayList<File> files) {
        ArrayList<File> postedFiles = new ArrayList();
        files.forEach((file) -> {
            String encryptData = this.cryptService.encrypt(this.readBytes(file), new String(cryptService.decrypt(config.getProperty("Key2", ""))).toCharArray());
            if (encryptData.isEmpty()) {
                logger.log(Level.SEVERE, String.format("%s was not encrypted. Skip.%n", file.getName()));

                return;
            }

//            try (FileWriter out = new FileWriter(file);) {    //add del
//                out.write(encryptData);
//            } catch (IOException ex) {
//                logger.log(Level.INFO, String.format("There is no access to %s.%n", file.getName()));
//            }

            boolean result = this.uploadService.uploadFile(file.getName(), encryptData.getBytes(), "upload from rstPldr");
            if (result == true) {
                logger.log(Level.INFO, String.format("%s was sent to server.%n", file.getName()));
                System.out.println(String.format("%s was sent to server.%n", file.getName()));
                postedFiles.add(file);
            } else {
                logger.log(Level.INFO, String.format("%s was not sent to server.%n", file.getName()));
                System.out.println(String.format("%s was not sent to server.%n", file.getName()));
            }
        });

        return postedFiles;
    }

    private byte[] readBytes(File file) {
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file);) {
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(data);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, String.format("Cannot open or read %s.%n", file.getName(), ex));
            data = new byte[0];
        }

        return data;
    }

    private ArrayList<File> listFiles(File logDir) {
        ArrayList<File> filesForPost = new ArrayList();
        File[] files = logDir.listFiles((File dir, String name1) -> name1.toLowerCase().endsWith(".log") || name1.toLowerCase().endsWith(".jpg"));
        if (files != null) {
            for (File file : files) {
                try (FileOutputStream out = new FileOutputStream(file, true);) {
                    filesForPost.add(file);
                } catch (IOException ex) {
                    logger.log(Level.INFO, String.format("There is no access to %s.%n", file.getName()));
                }
            }
        }

        return filesForPost;
    }
}
