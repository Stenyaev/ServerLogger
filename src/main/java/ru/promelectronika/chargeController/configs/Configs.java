package ru.promelectronika.chargeController.configs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;

public class Configs {
    private static boolean LOGGER_ENABLE_VALUE;
    private static String SERVER_LOG_CONFIG_PATH;
    private static String CCS_LOG_CONFIG_PATH;
    private static String CHADEMO_LOG_CONFIG_PATH;
    private static String GBT_LOG_CONFIG_PATH;
    private static String CONTROLLER_TYPE;

    public Configs() throws IOException {
        //Текущая директория
        String DIRECTORY_PATH = Arrays.stream((new File(".").getAbsolutePath()).split("\\.")).findFirst().get();
//        System.out.println(DIRECTORY_PATH);

        Properties properties = new Properties();
//        properties.load(new FileInputStream(new File(DIRECTORY_PATH + "Config\\properties.ini")));
        properties.load(Files.newInputStream(new File(DIRECTORY_PATH + "src\\main\\resources\\Config\\properties.ini").toPath()));

        LOGGER_ENABLE_VALUE = Boolean.parseBoolean(properties.getProperty("LOGGER_ENABLE_VALUE"));
        CONTROLLER_TYPE = properties.getProperty("CONTROLLER_TYPE");

//        SERVER_LOG_CONFIG_PATH = DIRECTORY_PATH + "LogConfig\\ServerLogback.xml";
        SERVER_LOG_CONFIG_PATH = DIRECTORY_PATH + "src\\main\\resources\\LogConfig\\ServerLogback.xml";
//        CCS_LOG_CONFIG_PATH = DIRECTORY_PATH + "LogConfig\\CcsLogback.xml";
        CCS_LOG_CONFIG_PATH = DIRECTORY_PATH + "src\\main\\resources\\LogConfig\\CcsLogback.xml";
//        CHADEMO_LOG_CONFIG_PATH = DIRECTORY_PATH + "LogConfig\\ChademoLogback.xml";
        CHADEMO_LOG_CONFIG_PATH = DIRECTORY_PATH + "src\\main\\resources\\LogConfig\\ChademoLogback.xml";
//        GBT_LOG_CONFIG_PATH = DIRECTORY_PATH + "LogConfig\\GbtLogback.xml";
        GBT_LOG_CONFIG_PATH = DIRECTORY_PATH + "src\\main\\resources\\LogConfig\\GbtLogback.xml";
    }

    public static boolean getLoggerEnableValue() {
        return LOGGER_ENABLE_VALUE;
    }

    public static String getServerLogConfigPath() {
        return SERVER_LOG_CONFIG_PATH;
    }

    public static String getCcsLogConfigPath() {
        return CCS_LOG_CONFIG_PATH;
    }

    public static String getChademoLogConfigPath() {
        return CHADEMO_LOG_CONFIG_PATH;
    }

    public static String getGbtLogConfigPath() {
        return GBT_LOG_CONFIG_PATH;
    }

    public static String getControllerType() {
        return CONTROLLER_TYPE;
    }
}
