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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 *
 * @author Maxim Eltratov <Maxim.Eltratov@yandex.ru>
 */
public class ExitChecker {

    private Path dir;
    private final Executor executor;
    private final WatchService watcher;

    // Create the checker using the provided path but with some defaults for
    // executor and watch service
    public ExitChecker() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.executor = Executors.newFixedThreadPool(1);
    }

    // Create the checker using the provided path, watcher and executor
    public ExitChecker(final Path dir, final WatchService watcher, final Executor executor) {
        this.dir = dir;
        this.watcher = watcher;
        this.executor = executor;
    }

    // Wait for the folder to be modified, then invoke the provided runnable
    public void runWhenItIsTimeToExit(final Runnable action) throws IOException {
        // Listen on events in the provided folder
        dir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        // Run it async, otherwise the caller thread will be blocked
        CompletableFuture.runAsync(() -> {
            try {
                WatchKey key = watcher.take();
                while (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
//                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            String filename = event.context().toString();
                            if (filename.startsWith("exit")) {
                                return;
                            }
//                        }
                    }
                    key.reset();
                    Thread.sleep(100);
                }

            } catch (InterruptedException e) {
                // Ok, we got interrupted
            }
        }, executor).thenRunAsync(action);
    }

    public void setDir(String dir) {
        this.dir = new File(dir).toPath();

    }
}
