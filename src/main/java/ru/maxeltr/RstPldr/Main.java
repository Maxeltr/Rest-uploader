/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.maxeltr.rstpldr;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.maxeltr.rstpldr.Config.AppAnnotationConfig;
import ru.maxeltr.rstpldr.Config.CmdLnParser;
import java.util.logging.Logger;
import ru.maxeltr.rstpldr.Config.Config;
import ru.maxeltr.rstpldr.Service.CryptService;
import ru.maxeltr.rstpldr.Service.ExitChecker;
import ru.maxeltr.rstpldr.Service.SendFilesTask;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppAnnotationConfig.class);
        CmdLnParser parser = (CmdLnParser) applicationContext.getBean("cmdLnParser");
        parser.parse(args);
        parser = null;

        CryptService cryptService = (CryptService) applicationContext.getBean("cryptService");
        Config config = (Config) applicationContext.getBean("config");
        String logDir = new String(cryptService.decrypt(config.getProperty("LogDir", "")));
        if (logDir.isEmpty()) {
            logger.log(Level.SEVERE, String.format("Cannot get property LogDir from configuration %s. Exit.%n", AppAnnotationConfig.CONFIG_PATHNAME));
            System.out.println(String.format("Cannot get property LogDir from configuration %s. Exit.%n", AppAnnotationConfig.CONFIG_PATHNAME));
            System.exit(1);
        }

        String logProgName = new String(cryptService.decrypt(config.getProperty("LogProgName", "")));
        if (logProgName.isEmpty()) {
            logger.log(Level.SEVERE, String.format("Cannot get property LogProgName from configuration %s. Exit.%n", AppAnnotationConfig.CONFIG_PATHNAME));
            System.out.println(String.format("Cannot get property LogProgName from configuration %s. Exit.%n", AppAnnotationConfig.CONFIG_PATHNAME));
            System.exit(2);
        }

        if (Config.SHOW_OPTIONS == true) {
            System.out.println("LogDir " + logDir);
            System.out.println("logProgName " + logProgName);
            System.out.println("Key2 " + new String(cryptService.decrypt(config.getProperty("Key2", ""))));
            System.out.println("ClientId " + new String(cryptService.decrypt(config.getProperty("ClientId", ""))));
            System.out.println("ClientSecret " + new String(cryptService.decrypt(config.getProperty("ClientSecret", ""))));
            System.out.println("SubDirs " + new String(cryptService.decrypt(config.getProperty("SubDirs", ""))));
            System.out.println("UrlGetToken " + new String(cryptService.decrypt(config.getProperty("UrlGetToken", ""))));
            System.out.println("UrlUploadFile " + new String(cryptService.decrypt(config.getProperty("UrlUploadFile", ""))));
            System.out.println("SendInterval " + config.getProperty("SendInterval", ""));
        }

        // In this case we use an AtomicBoolean to hold the "exit-status"
        AtomicBoolean shouldExit = new AtomicBoolean(false);

        // Start the exit checker, provide a Runnable that will be executed
        // when it is time to exit the program
        ExitChecker exitChecker = (ExitChecker) applicationContext.getBean("exitChecker");
        exitChecker.setDir(logDir);
        try {
            exitChecker.runWhenItIsTimeToExit(() -> {
                shouldExit.set(true);
            });
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Cannot run ExitChecker. Exit.%n", ex);
            System.out.println("Cannot run ExitChecker. Exit.%n");
            System.exit(3);
        }

        Timer timer = new Timer();
        SendFilesTask task = (SendFilesTask) applicationContext.getBean("sendFilesTask");
        String interval = config.getProperty("SendInterval", "1800000");
        timer.schedule(task, 1000, new Long(interval));

        String logProcessPath = logDir + File.separator + logProgName;
        ProcessBuilder builder = new ProcessBuilder(logProcessPath);
        builder.directory(new File(logDir));
        Process process = null;
        try {
            process = builder.start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, String.format("Cannot start %s. Exit.%n", logProgName), ex);
            System.out.println(String.format("Cannot start %s. Exit.%n", logProgName));
            System.exit(4);
        }

        // Start processing
        while (!shouldExit.get()) {
            Thread.sleep(10000);
            if (!process.isAlive()) {
                logger.log(Level.SEVERE, String.format("Process %s was terminated by unknown. Exit value %s.%n", logProcessPath, process.exitValue()));
                try {
                    process = builder.start();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, String.format("Cannot restart %s.%n", logProgName), ex);
                    System.out.println(String.format("Cannot restart %s.%n", logProgName));
                }
            }
        }

        process.destroy();
        timer.cancel();
        System.out.println("Exiting");
        System.exit(0);

    }

}
