/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.maxeltr.RstPldr;

//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Общий
 */
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            logger.info("start");

            Thread.sleep(500);

            logger.info("stop");

        } catch (InterruptedException ex) {

        }

    }

}
