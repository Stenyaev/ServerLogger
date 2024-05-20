package ru.promelectronika;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;
import ru.promelectronika.chargeController.GbtClient;
import ru.promelectronika.chargeController.RpcServer;
import ru.promelectronika.chargeController.configs.Configs;
import ru.promelectronika.chargeController.constants.IpAddress;
import ru.promelectronika.errorHandler.ErrorHandler;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        try {
            Configs configs = new Configs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JoranConfigurator configurator = new JoranConfigurator();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        try {
            configurator.doConfigure(new File(Configs.getServerLogConfigPath()));
        } catch (JoranException e) {
            ErrorHandler.logMain(e);
        }

        RpcServer rpcServer = null;
        try {
            rpcServer = new RpcServer(IpAddress.SERVER_ADDRESS, 15000, 8071, 8091);
        } catch (IOException e) {
            ErrorHandler.logMain(e);
        }

        GbtClient gbtClient = null;
        try {
            gbtClient = new GbtClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread.sleep(250);
        gbtClient.rpcLog(true, IpAddress.SERVER_ADDRESS, 15000);
        Thread.sleep(250);

        while (true) {
            try {
                Objects.requireNonNull(rpcServer).start();
            } catch (IOException | ClassCastException e) {
                ErrorHandler.logMain(e);
            }

        }
    }
}

