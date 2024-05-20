package ru.promelectronika.chargeController;

import ru.promelectronika.chargeController.constants.IpAddress;
import ru.promelectronika.errorHandler.ErrorHandler;

import java.io.IOException;

public class ChademoClient extends RpcClient {
    private static final String ADDRESS = IpAddress.CHADEMO_ADDRESS;
    private static final int PORT = IpAddress.CHADEMO_PORT;

    public ChademoClient() throws IOException {
        super(ADDRESS, PORT);
    }

    //    Отправка логов на конкретный IP
    public void rpcLog(boolean log, String remoteAddress, int remotePort) {
        Object[] arg = {log, remoteAddress, remotePort};
        try {
            sendMessage("LOG", arg);
        } catch (Throwable e) {
            ErrorHandler.logChademo(e);
        }
        StringBuilder str = new StringBuilder();
        str.append("\n\tLog: ");
        str.append(log);
        str.append("\n\tRemote Address: ");
        str.append(remoteAddress);
        str.append("\n\tRemote Port: ");
        str.append(remotePort);
        ErrorHandler.loggerClientChademo.info("LOG: {}", str.toString());
    }
}
