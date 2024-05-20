package ru.promelectronika.chargeController;

import ru.promelectronika.chargeController.constants.IpAddress;
import ru.promelectronika.errorHandler.ErrorHandler;

import java.io.IOException;

public class CcsClient extends RpcClient {
    private static final String ADDRESS = IpAddress.CCS_ADDRESS;
    private static final int PORT = IpAddress.CCS_PORT;

    public CcsClient() throws IOException {
        super(ADDRESS, PORT);
    }

    //    Отправка логов на конкретный IP
    public void rpcLog(boolean log, String remoteAddress, int remotePort) {
        Object[] arg = {log, remoteAddress, remotePort};
        try {
            sendMessage("LOG", arg);
        } catch (Throwable e) {
            ErrorHandler.logCcs(e);
        }
        StringBuilder str = new StringBuilder();
        str.append("\n\tLog: ");
        str.append(log);
        str.append("\n\tRemote Address: ");
        str.append(remoteAddress);
        str.append("\n\tRemote Port: ");
        str.append(remotePort);
        ErrorHandler.loggerClientCcs.info("LOG: {}", str.toString());
    }
}
