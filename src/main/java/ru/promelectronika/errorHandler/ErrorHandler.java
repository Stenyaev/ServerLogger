package ru.promelectronika.errorHandler;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {
    private static StringWriter sw = new StringWriter();
    private static PrintWriter pw = new PrintWriter(sw);
    public static Logger loggerBlock = (Logger) LoggerFactory.getLogger("POWER_BLOCK");
    public static Logger loggerMain = (Logger) LoggerFactory.getLogger("MAIN");
    public static Logger loggerClientCcs = (Logger) LoggerFactory.getLogger("CCS SEND");
    public static Logger loggerClientChademo = (Logger) LoggerFactory.getLogger("CHADEMO SEND");
    public static Logger loggerClientGbt = (Logger) LoggerFactory.getLogger("GBT SEND");
    public static Logger loggerServer = (Logger) LoggerFactory.getLogger("RPC SERVER");

    public static Logger loggerClient = (Logger) LoggerFactory.getLogger("RPC CLIENT");
    public static Logger loggerCcs = (Logger) LoggerFactory.getLogger("CCS RECV");
    public static Logger loggerChademo = (Logger) LoggerFactory.getLogger("CHADEMO RECV");
    public static Logger loggerGbt = (Logger) LoggerFactory.getLogger("GBT RECV");

    public static void logMain(Throwable e) {
        e.printStackTrace(pw);
        loggerMain.error(sw.toString());
        sw.getBuffer().setLength(0);
        pw.flush();
    }
    public static void logBlock(String message) {
        loggerBlock.info(message);
    }

    public static void logCcs(Throwable e) {
        e.printStackTrace(pw);
        loggerClientCcs.error(sw.toString());
        sw.getBuffer().setLength(0);
        pw.flush();
    }
    public static void logChademo(Throwable e) {
        e.printStackTrace(pw);
        loggerClientChademo.error(sw.toString());
        sw.getBuffer().setLength(0);
        pw.flush();
    }
    public static void logGbt(Throwable e) {
        e.printStackTrace(pw);
        loggerClientGbt.error(sw.toString());
        sw.getBuffer().setLength(0);
        pw.flush();
    }

    public static void logServer(Throwable e) {
        e.printStackTrace(pw);
        loggerServer.error(sw.toString());
        sw.getBuffer().setLength(0);
        pw.flush();
    }
}
