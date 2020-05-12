/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.maxeltr.rstpldr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import ru.maxeltr.rstpldr.Service.RestUploadService;
import ru.maxeltr.rstpldr.Service.SendFilesTask;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppAnnotationConfig.class);
        CmdLnParser parser = (CmdLnParser) applicationContext.getBean("cmdLnParser");
        parser.parse(args);
        parser = null;

        CryptService cryptService = (CryptService) applicationContext.getBean("cryptService");

        Config config = (Config) applicationContext.getBean("config");
        String logDir = new String(cryptService.decrypt(config.getProperty("LogDir", "")));
        if (logDir.isEmpty()) {
            logDir = System.getProperty("user.home");
        }

        System.out.println("LogDir " + logDir);
        System.out.println("Key2 " + new String(cryptService.decrypt(config.getProperty("Key2", ""))));
        System.out.println("ClientId " + new String(cryptService.decrypt(config.getProperty("ClientId", ""))));
        System.out.println("ClientSecret " + new String(cryptService.decrypt(config.getProperty("ClientSecret", ""))));

//        new String(cryptService.decrypt(config.getProperty("LogDir", System.getProperty("user.home"))))

        RestUploadService restUploadService = (RestUploadService) applicationContext.getBean("restUploadService");
        SendFilesTask task = new SendFilesTask(logDir, restUploadService);
        Timer timer = new Timer();
        timer.schedule(task, 1000, 2000);



        // Setup dirs in the home folder
//        final Path directory = Files.createDirectories(
//                new File(System.getProperty("user.dir")).toPath());

        // In this case we use an AtomicBoolean to hold the "exit-status"
        AtomicBoolean shouldExit = new AtomicBoolean(false);

        // Start the exit checker, provide a Runnable that will be executed
        // when it is time to exit the program
        ExitChecker exitChecker = (ExitChecker) applicationContext.getBean("exitChecker");
        exitChecker.setDir(logDir);
        exitChecker.runWhenItIsTimeToExit(() -> {
            // This is where your exit code will end up. In this case we
            // simply change the value of the AtomicBoolean
            shouldExit.set(true);
        });

        // Start processing
        while (!shouldExit.get()) {
            System.out.println("Do something in loop");
            Thread.sleep(10000);
        }

        timer.cancel();
        System.out.println("Exiting"); //terminate processes lglst?
        System.exit(0);
//        try {
//
//            logger.log(Level.SEVERE, String.format("Canijnta.%n"));
//            logger.info(AppConfig.pin);
//
//            Thread.sleep(15000);
//
//            logger.info("stop");
//            timer.cancel();
//        } catch (InterruptedException ex) {
//
//        }

    }

}
