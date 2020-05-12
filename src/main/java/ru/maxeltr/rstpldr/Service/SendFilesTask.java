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
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.maxeltr.rstpldr.Main;

/**
 *
 * @author Maxim Eltratov <Maxim.Eltratov@yandex.ru>
 */
public class SendFilesTask extends TimerTask {

    private final File logDir;

    private final RestUploadService uploadService;

    private static final Logger logger = Logger.getLogger(SendFilesTask.class.getName());

    public SendFilesTask(String dir, RestUploadService uploadService) {
        this.logDir = new File(dir);
        this.uploadService = uploadService;
    }

    public void run() {
        System.out.println("timer");
        ArrayList<File> files = this.listFiles();
        if (files.isEmpty()) {
            return;
        }

        String token = this.uploadService.authenticate();
        if (token.isEmpty()) {
            return;
        }

        files.forEach((file) -> {
            boolean result = this.uploadService.uploadFile(file.getName(), this.readBytes(file), "upload from rstPldr");
            if (result == true) {
                logger.log(Level.INFO, String.format("%s was sent to server.%n", file.getName()));
                System.out.println(String.format("%s was sent to server.%n", file.getName()));
                boolean isDel = file.delete();
                if (isDel == true) {
                    logger.log(Level.INFO, String.format("%s was deleted.%n", file.getName()));
                } else {
                    logger.log(Level.SEVERE, String.format("%s was not deleted.%n", file.getName()));
                }
            }
        });
    }

    public byte[] readBytes(File file) {
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file);) {
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(data);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, String.format("Cannot open or read %s.%n", file.toString(), ex));
            data = new byte[0];
        }

        return data;
    }

    private ArrayList<File> listFiles() {
        ArrayList<File> filesForPost = new ArrayList();
        File[] files = this.logDir.listFiles((File dir, String name1) -> name1.toLowerCase().endsWith(".log") || name1.toLowerCase().endsWith(".jpg"));
        if (files != null) {
            String fileName;
            for (File file : files) {
                fileName = file.getName();
                try (FileOutputStream out = new FileOutputStream(file, true);) {
                    filesForPost.add(file);
                } catch (IOException ex) {
//                    System.out.println("fileName can not Write: " + fileName);
                }
            }
        }

        return filesForPost;
    }
}
