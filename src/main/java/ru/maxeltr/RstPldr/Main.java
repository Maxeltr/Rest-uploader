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

        CryptService cryptService = (CryptService) applicationContext.getBean("cryptService");

        Config config = (Config) applicationContext.getBean("config");
        System.out.println(new String(cryptService.decrypt(config.getProperty("LogDir", ""))));

//        new String(cryptService.decrypt(config.getProperty("LogDir", System.getProperty("user.home"))))

//        SendFilesTask task = new SendFilesTask();
//        Timer timer = new Timer();
//        timer.schedule(task, 1000, 2000);



        // Setup dirs in the home folder
//        final Path directory = Files.createDirectories(
//                new File(System.getProperty("user.dir")).toPath());
//
//        // In this case we use an AtomicBoolean to hold the "exit-status"
//        AtomicBoolean shouldExit = new AtomicBoolean(false);
//
//        // Start the exit checker, provide a Runnable that will be executed
//        // when it is time to exit the program
//        ExitChecker exitChecker = (ExitChecker) applicationContext.getBean("exitChecker");
//        exitChecker.runWhenItIsTimeToExit(() -> {
//            // This is where your exit code will end up. In this case we
//            // simply change the value of the AtomicBoolean
//            shouldExit.set(true);
//        });
//
//        // Start processing
//        while (!shouldExit.get()) {
//            System.out.println("Do something in loop");
//            Thread.sleep(1000);
//        }

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
