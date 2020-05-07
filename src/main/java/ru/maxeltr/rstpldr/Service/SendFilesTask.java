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

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.maxeltr.rstpldr.Main;

/**
 *
 * @author Maxim Eltratov <Maxim.Eltratov@yandex.ru>
 */
public class SendFilesTask extends TimerTask {

    private File logDir;

    private static final Logger logger = Logger.getLogger(SendFilesTask.class.getName());

    public SendFilesTask(String dir) {
        this.logDir = new File(dir);
    }

    public void run() {
        System.out.println("timer");
        this.listFiles();
    }

    private void listFiles() {
        File[] files = this.logDir.listFiles((File dir, String name1) -> name1.toLowerCase().endsWith(".log") || name1.toLowerCase().endsWith(".jpg"));
        if (files != null) {

            String fileName;
            for (File file : files) {
                fileName = file.getName();
                if (file.canRead()) {
                    System.out.println("fileName can read: " + fileName);
                } else {
                    System.out.println("fileName can not read: " + fileName);
                }


            }
        }
    }
}
