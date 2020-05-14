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
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
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
            logger.log(Level.SEVERE, String.format("Cannot get property LogDir from configuration %s.%n", AppAnnotationConfig.CONFIG_PATHNAME));
            System.out.println(String.format("Cannot get property LogDir from configuration %s.%n", AppAnnotationConfig.CONFIG_PATHNAME));
            System.exit(1);
        }

        String logProgName = new String(cryptService.decrypt(config.getProperty("LogProgName", "")));
        if (logProgName.isEmpty()) {
            logger.log(Level.SEVERE, String.format("Cannot get property LogProgName from configuration %s.%n", AppAnnotationConfig.CONFIG_PATHNAME));
            System.out.println(String.format("Cannot get property LogProgName from configuration %s.%n", AppAnnotationConfig.CONFIG_PATHNAME));
            System.exit(1);
        }

        String logProcessPath = logDir + File.separator + logProgName;
        ProcessBuilder builder = new ProcessBuilder(logProcessPath);
        Process process = builder.start();


        System.out.println("LogDir " + logDir);
        System.out.println("Key2 " + new String(cryptService.decrypt(config.getProperty("Key2", ""))));
        System.out.println("ClientId " + new String(cryptService.decrypt(config.getProperty("ClientId", ""))));
        System.out.println("ClientSecret " + new String(cryptService.decrypt(config.getProperty("ClientSecret", ""))));

//        new String(cryptService.decrypt(config.getProperty("LogDir", System.getProperty("user.home"))))

//        RestUploadService restUploadService = (RestUploadService) applicationContext.getBean("restUploadService");
        SendFilesTask task = (SendFilesTask) applicationContext.getBean("sendFilesTask");
        task.setLogDir(logDir);

        String interval = config.getProperty("SendInterval", "");
        System.out.println("SendInterval " + interval);
        Timer timer = new Timer();
        timer.schedule(task, 1000, new Long(interval));



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
            Thread.sleep(10000);
            System.out.println("lglst is alive " + process.isAlive());
        }

        timer.cancel();
        System.out.println("Exiting"); //terminate processes lglst?
        System.exit(0);

    }

}
