package ru.promelectronika.chargeController;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.msgpack.core.*;
import org.msgpack.value.*;
import org.slf4j.LoggerFactory;
import ru.promelectronika.chargeController.configs.Configs;
import ru.promelectronika.chargeController.constants.ControllerType;
import ru.promelectronika.chargeController.constants.IpAddress;
import ru.promelectronika.chargeController.constants.Message;
import ru.promelectronika.errorHandler.ErrorHandler;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class RpcServer {

    private final String CHADEMO_ADDRESS = IpAddress.CHADEMO_ADDRESS;
    private final String CCS_ADDRESS = IpAddress.CCS_ADDRESS;
    private final String GBT_ADDRESS = IpAddress.GBT_ADDRESS;
    private final Selector selectorGbt;
    private final Selector selectorCcs;
    private final Selector selectorChademo;

    private final ServerSocketChannel serverSocketChannelGbt;
    private final ServerSocketChannel serverSocketChannelCcs;
    private final ServerSocketChannel serverSocketChannelChademo;

    private final ControllerParams ccsController;
    private final ControllerParams chademoController;
    private final ControllerParams gbtController;
    public RpcServer(String address, int portGbt, int portCcs, int portChademo) throws IOException {
        serverSocketChannelGbt = ServerSocketChannel.open();
        serverSocketChannelCcs = ServerSocketChannel.open();
        serverSocketChannelChademo = ServerSocketChannel.open();

        selectorGbt = Selector.open();
        selectorCcs = Selector.open();
        selectorChademo = Selector.open();

        serverSocketChannelGbt.configureBlocking(false);
        serverSocketChannelCcs.configureBlocking(false);
        serverSocketChannelChademo.configureBlocking(false);

        serverSocketChannelGbt.bind(new InetSocketAddress(address, portGbt));
        serverSocketChannelCcs.bind(new InetSocketAddress(address, portCcs));
        serverSocketChannelChademo.bind(new InetSocketAddress(address, portChademo));

        int opsGbt = serverSocketChannelGbt.validOps();
        int opsCcs= serverSocketChannelCcs.validOps();
        int opsChademo = serverSocketChannelChademo.validOps();

        serverSocketChannelGbt.register(selectorGbt, opsGbt, null);
        serverSocketChannelCcs.register(selectorCcs, opsCcs, null);
        serverSocketChannelChademo.register(selectorChademo, opsChademo, null);

        ccsController = new ControllerParams();
        chademoController = new ControllerParams();
        gbtController = new ControllerParams();
    }
    public void start() throws IOException {
        serverListen(selectorGbt, ControllerType.GBT);
        serverListen(selectorCcs, ControllerType.CCS);
        serverListen(selectorChademo, ControllerType.CHADEMO);
    }
    public void accept(ControllerType nameController) throws IOException {
        SocketChannel client;
        switch (nameController) {
            case GBT:
                client = serverSocketChannelGbt.accept();
                client.configureBlocking(false);
                client.register(selectorGbt, SelectionKey.OP_READ);
                System.out.println("Client GBT Accepted: " + client.getRemoteAddress() + "\n");
                break;
            case CCS:
                client = serverSocketChannelCcs.accept();
                client.configureBlocking(false);
                client.register(selectorCcs, SelectionKey.OP_READ);
                System.out.println("Client CCS Accepted: " + client.getRemoteAddress() + "\n");
                break;
            case CHADEMO:
                client = serverSocketChannelChademo.accept();
                client.configureBlocking(false);
                client.register(selectorChademo, SelectionKey.OP_READ);
                System.out.println("Client CHADEMO Accepted: " + client.getRemoteAddress() + "\n");
                break;
        }
    }
    private byte[] read(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        int len = client.read(buffer);
        if (len == -1) {
            len = 0;
        }
        return Arrays.copyOf(buffer.array(), len);
    }

    private void write(byte[] writeBuf, SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(writeBuf);
        channel.write(buffer);
    }
    public void sendMessage(int msgId, SocketChannel channel) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packArrayHeader(4)
                .packInt(Message.RESPONSE)
                .packInt(msgId)
                .packNil()
                .packNil();
        packer.flush();
        this.write(packer.toByteArray(), channel);
    }

    public ArrayList<Object> arrayHandler(ArrayValue value) {
        ArrayList<Object> arrayObj = new ArrayList<>();
        for (Value v: value) {
            switch (v.getValueType()) {
                case NIL:
                    arrayObj.add(null);
                    break;
                case BOOLEAN:
                    boolean b = v.asBooleanValue().getBoolean();
                    arrayObj.add(b);
                    break;
                case INTEGER:
                    IntegerValue iv = v.asIntegerValue();
                    if (iv.isInIntRange()) {
                        int i = iv.toInt();
                        arrayObj.add(i);
                    } else if (iv.isInLongRange()) {
                        long l = iv.toLong();
                        arrayObj.add(l);
                    } else {
                        BigInteger i = iv.toBigInteger();
                        arrayObj.add(i);
                    }
                    break;
                case FLOAT:
                    FloatValue fv = v.asFloatValue();
                    if (fv.getValueType().equals(ValueType.FLOAT)) {
                        float f = fv.toFloat();   // use as float
                        arrayObj.add(f);
                    }else {
                        double d = fv.toDouble(); // use as double
                        arrayObj.add(d);
                    }
                    break;
                case STRING:
                    String s = v.asStringValue().asString();
                    arrayObj.add(s);
                    break;

                case ARRAY:
                    ArrayValue a = v.asArrayValue();
                    arrayObj.add(arrayHandler(a));
                    break;
            }
        }
        return arrayObj;
    }

    public void readMessage(SocketChannel channel, ControllerType nameController) {

        try {
            logConfigurator();
        } catch (JoranException e) {
            throw new RuntimeException(e);
        }

        byte[] msg = new byte[0];
        try {
            msg = this.read(channel);
        } catch (IOException e) {
            ErrorHandler.loggerServer.error(e.getMessage());
        }
        if (msg.length > 0) {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(msg);
            while (true) {
                try {
                    if (!unpacker.hasNext()) break;
                } catch (IOException e) {
                    ErrorHandler.loggerServer.error(e.getMessage());
                }
                Value v = null;
                try {
                    v = unpacker.unpackValue();
                } catch (IOException e) {
                    ErrorHandler.loggerServer.error(e.getMessage());
                }
                if (v.getValueType() == ValueType.ARRAY) {
                    ArrayValue vArray = v.asArrayValue();
                    ArrayList<Object> arrayObject;
                    arrayObject = arrayHandler(vArray);
                    int msgType = 0;
                    int msgId = -1;
                    String method = null;
                    ArrayList<Object> arguments = null;
                    if ((int) arrayObject.get(0) == Message.REQUEST) {
                        msgId = (int) arrayObject.get(1);
                        method = (String) arrayObject.get(2);
                        arguments = (ArrayList<Object>) arrayObject.get(3);
                    } else if ((int) arrayObject.get(0) == Message.NOTIFY) {
                        msgType = 2;
                        method = (String) arrayObject.get(1);
                        arguments = (ArrayList<Object>) arrayObject.get(2);
                    }

                    String channelIp = null;
                    try {
                        channelIp = channel.getRemoteAddress()
                                .toString()
                                .split(":")[0]
                                .substring(1);

                    } catch (IOException e) {
                        ErrorHandler.logServer(e);
                    }
                    String addr = "";

                    if (nameController == ControllerType.GBT) {
                        addr = GBT_ADDRESS;
                    } else if (nameController == ControllerType.CCS) {
                        addr = CCS_ADDRESS;
                    } else if (nameController == ControllerType.CHADEMO) {
                        addr = CHADEMO_ADDRESS;
                    }
                    switch (Objects.requireNonNull(method)) {
                        case "rpcPing":
                            rpcPing(arguments, addr);
                            break;
                        case "SET_FW_VERSION":
                            rpcSetFwVersion(arguments, addr);
                            break;
                        case "SET_PROTOCOL_VERSION":
                            rpcSetProtocolVersion(arguments, addr);
                            break;
                        case "SET_SECC_CURRENT_STATE":
                            rpcSetSeccCurrentState(arguments, addr);
                            break;
                        case "SET_EV_LIMITS":
                            rpcSetEvLimits(arguments, addr);
                            break;
                        case "SET_EV_TARGET_PARAMS":
                            rpcSetEvTargetParams(arguments, addr);
                            break;
                        case "SET_EV_PARAMS":
                            rpcSetEvParams(arguments, addr);
                            break;
                        case "SET_EV_STATE":
                            rpcSetEvState(arguments, addr);
                            break;
                        case "SET_EV_SOC":
                            rpcSetEvSoc(arguments, addr);
                            break;
                        case "SET_ERROR_CODE":
                            rpcSetErrorCode(arguments, addr);
                            break;
                        case ("LOGGER"):
                            try {
                                Configs configs = new Configs();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (Configs.getLoggerEnableValue() == true) { // Если логирование включено
                                setLogger(arguments, addr);
                            }
                            break;
                        default:
                            System.out.println("Method not found:" + method);
                    }
                    if (msgType == Message.REQUEST) {
                        try {
                            sendMessage(msgId, channel);
                        } catch (IOException e) {
                            ErrorHandler.loggerServer.error(e.getMessage());
                        }
                    }
                }
            }
        }
    }
    private void serverListen(Selector selector, ControllerType nameController) throws IOException {
        selector.select(1);
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
        while (selectionKeyIterator.hasNext()) {
            SelectionKey aKey = selectionKeyIterator.next();
            if (aKey.isAcceptable()) {
                accept(nameController);
            } else if (aKey.isReadable()) {
                try {
                    readMessage((SocketChannel) aKey.channel(), nameController);
                } catch (MessageInsufficientBufferException e) {
                    ErrorHandler.loggerServer.warn("Message Insufficient Buffer Exception");
                } catch (MessageStringCodingException e) {
                    ErrorHandler.loggerServer.error(e.getMessage());
                }
            }
            selectionKeyIterator.remove();
        }
    }
    public ControllerParams getChademoController() {
        return chademoController;
    }
    public ControllerParams getCcsController() {
        return ccsController;
    }
    public ControllerParams getGbtController() {
        return  gbtController;
    }
    private void rpcPing(ArrayList params, String address) {
        switch (address) {
            case CCS_ADDRESS:
                ccsController.setSelfConnectionInputState((Integer) params.get(0));
                ccsController.setSelfConnectionOutputState((Integer) params.get(1));
                ErrorHandler.loggerCcs.debug("rpcPing: {}", ccsController.getRpcPing());
                break;
            case CHADEMO_ADDRESS:
                chademoController.setSelfConnectionInputState((Integer) params.get(0));
                chademoController.setSelfConnectionOutputState((Integer) params.get(0));
                ErrorHandler.loggerChademo.debug("rpcPing: {}", chademoController.getRpcPing());
                break;
            case GBT_ADDRESS:
                gbtController.setSelfConnectionInputState((Integer) params.get(0));
                gbtController.setSelfConnectionOutputState((Integer) params.get(1));
                ErrorHandler.loggerGbt.debug("rpcPing: {}", gbtController.getRpcPing());
        }
    }
    private void rpcSetFwVersion(ArrayList params, String address) {
        switch (address) {
            case CCS_ADDRESS:
                ccsController.setVersion((String) params.get(0));
                ErrorHandler.loggerCcs.info("rpcSetFwVersion: {}", ccsController.getSetFwVersion());
                break;
            case CHADEMO_ADDRESS:
                chademoController.setVersion((String) params.get(0));
                ErrorHandler.loggerChademo.info("rpcSetFwVersion: {}", chademoController.getSetFwVersion());
                break;
            case GBT_ADDRESS:
                gbtController.setVersion((String) params.get(0));
                ErrorHandler.loggerGbt.info("rpcSetFwVersion: {}", gbtController.getSetFwVersion());
        }
    }
    private void rpcSetProtocolVersion(ArrayList params, String address) {
        switch (address) {
            case CCS_ADDRESS:
                ccsController.setProtocolVersion((String) params.get(0));
                ErrorHandler.loggerCcs.info("rpcSetProtocolVersion: {}", ccsController.getSetProtocolVersion());
                break;
            case CHADEMO_ADDRESS:
                chademoController.setVersion((String) params.get(0));
                ErrorHandler.loggerChademo.info("rpcSetProtocolVersion: {}", chademoController.getSetProtocolVersion());
                break;
            case GBT_ADDRESS:
                gbtController.setVersion((String) params.get(0));
                ErrorHandler.loggerGbt.info("rpcSetProtocolVersion: {}", gbtController.getSetProtocolVersion());
        }
    }
    private void rpcSetSeccCurrentState(ArrayList params, String address) {
        try {
            switch (address) {
                case CCS_ADDRESS:
                    ccsController.setChargeState((int) params.get(0));
                    ccsController.setChargeStateProtocolSpecific((String) params.get(1));
                    ErrorHandler.loggerCcs.info("rpcSetSeccCurrentState: {}", ccsController.getSetSeccCurrentState());
                    break;
                case CHADEMO_ADDRESS:
                    chademoController.setChargeState((int) params.get(0));
                    chademoController.setChargeStateProtocolSpecific((String) params.get(1));
                    ErrorHandler.loggerChademo.info("rpcSetSeccCurrentState: {}", chademoController.getSetSeccCurrentState());
                    break;
                case GBT_ADDRESS:
                    gbtController.setChargeState((int) params.get(0));
                    gbtController.setChargeStateProtocolSpecific((String) params.get(1));
                    ErrorHandler.loggerGbt.info("rpcSetSeccCurrentState: {}", gbtController.getSetSeccCurrentState());
            }
        } catch (ClassCastException e) {
            ErrorHandler.logMain(e);
        }
    }
    private void rpcSetEvLimits(ArrayList params, String address) {
        try {
            switch (address) {
                case CCS_ADDRESS:
                    ccsController.setMaximumPowerLimitW((Float) params.get(0));
                    ccsController.setMaximumVoltageLimitV((Float) params.get(1));
                    ccsController.setMaximumCurrentLimitA((Float) params.get(2));
                    ErrorHandler.loggerCcs.info("rpcSetEvLimits: {}", ccsController.getSetEvLimits());
                    break;
                case CHADEMO_ADDRESS:
                    try {
                        chademoController.setMaximumPowerLimitW((float) params.get(0));
                    } catch (ClassCastException e) {
                        chademoController.setMaximumPowerLimitW((int) params.get(0));
                    }
                    try {
                        chademoController.setMaximumVoltageLimitV((float) params.get(1));
                    } catch (ClassCastException e) {
                        chademoController.setMaximumVoltageLimitV((int) params.get(1));
                    }
                    try {
                        chademoController.setMaximumCurrentLimitA((float) params.get(2));
                    } catch (ClassCastException e) {
                        chademoController.setMaximumCurrentLimitA((int) params.get(2));
                    }
                    ErrorHandler.loggerChademo.info("rpcSetEvLimits: {}", chademoController.getSetEvLimits());
                    break;
                case GBT_ADDRESS:
                    gbtController.setMaximumPowerLimitW((Float) params.get(0));
                    gbtController.setMaximumVoltageLimitV((Float) params.get(1));
                    gbtController.setMaximumCurrentLimitA((Float) params.get(2));
                    ErrorHandler.loggerGbt.info("rpcSetEvLimits: {}", gbtController.getSetEvLimits());
            }
        } catch (ClassCastException e) {
            ErrorHandler.logMain(e);
        }
    }
    private void rpcSetEvTargetParams(ArrayList params, String address) {
        try {
            switch (address) {
                case CCS_ADDRESS:
                    ccsController.setInvertorState((int) params.get(0));
                    ccsController.setOutputContactorOn((Boolean) params.get(1));
                    ccsController.setInsulationControlOn((Boolean) params.get(2));
                    ccsController.setTargetVoltageV((Float) params.get(3));
                    ccsController.setTargetCurrentA((Float) params.get(4));
                    ErrorHandler.loggerCcs.info("rpcSetEvTargetParams: {}", ccsController.getSetEvTargetParams());
                    break;
                case CHADEMO_ADDRESS:
                    chademoController.setInvertorState((int) params.get(0));
                    chademoController.setOutputContactorOn((Boolean) params.get(1));
                    chademoController.setInsulationControlOn((Boolean) params.get(2));
                    chademoController.setTargetVoltageV((Float) params.get(3));
                    chademoController.setTargetCurrentA((Float) params.get(4));
                    ErrorHandler.loggerChademo.info("rpcSetEvTargetParams: {}", chademoController.getSetEvTargetParams());
                    break;
                case GBT_ADDRESS:
                    gbtController.setInvertorState((int) params.get(0));
                    gbtController.setOutputContactorOn((Boolean) params.get(1));
                    gbtController.setInsulationControlOn((Boolean) params.get(2));
                    gbtController.setTargetVoltageV((Float) params.get(3));
                    gbtController.setTargetCurrentA((Float) params.get(4));
                    ErrorHandler.loggerGbt.info("rpcSetEvTargetParams: {}", gbtController.getSetEvTargetParams());
            }
        } catch (ClassCastException e) {
            ErrorHandler.logMain(e);
        }
    }
    private void rpcSetEvParams(ArrayList params, String address) {
        try {
            switch (address) {
                case CCS_ADDRESS:
                    ccsController.setEvId((String) params.get(0));
                    ccsController.setEnergyCapacity((Float) params.get(1));
                    ccsController.setEnergyRequest((Float) params.get(2));
                    ErrorHandler.loggerCcs.info("rpcSetEvParams: {}", ccsController.getSetEvParams());
                    break;
                case CHADEMO_ADDRESS:
                    chademoController.setEvId((String) params.get(0));
                    chademoController.setEnergyCapacity((Float) params.get(1));
                    chademoController.setEnergyRequest((Float) params.get(2));
                    ErrorHandler.loggerChademo.info("rpcSetEvParams: {}", chademoController.getSetEvParams());
                    break;
                case GBT_ADDRESS:
                    gbtController.setEvId((String) params.get(0));
                    gbtController.setEnergyCapacity((Float) params.get(1));
                    gbtController.setEnergyRequest((Float) params.get(2));
                    ErrorHandler.loggerGbt.info("rpcSetEvParams: {}", gbtController.getSetEvParams());
            }
        } catch (ClassCastException e) {
            ErrorHandler.logMain(e);
        }
    }
    private void rpcSetEvState(ArrayList params, String address) {
        try {
            switch (address) {
                case CCS_ADDRESS:
                    ccsController.setEvReady((Boolean) params.get(0));
                    ccsController.setEvErrorCode((String) params.get(1));
                    ErrorHandler.loggerCcs.info("rpcSetEvState: {}", ccsController.getSetEvState());
                    break;
                case CHADEMO_ADDRESS:
                    chademoController.setEvReady((Boolean) params.get(0));
                    chademoController.setEvErrorCode((String) params.get(1));
                    ErrorHandler.loggerChademo.info("rpcSetEvState: {}", chademoController.getSetEvState());
                    break;
                case GBT_ADDRESS:
                    gbtController.setEvReady((Boolean) params.get(0));
                    gbtController.setEvErrorCode((String) params.get(1));
                    ErrorHandler.loggerGbt.info("rpcSetEvState: {}", gbtController.getSetEvState());
            }
        } catch (ClassCastException e) {
            ErrorHandler.logMain(e);
        }
    }
    private void rpcSetEvSoc(ArrayList params, String address) {
        try {
            switch (address) {
                case CCS_ADDRESS:
                    ccsController.setEvSoc((Float) params.get(0));
                    ccsController.setBulkChargingComplete((Boolean) params.get(1));
                    ccsController.setChargingComplete((Boolean) params.get(2));
                    ccsController.setBulkSoc((Float) params.get(3));
                    ccsController.setFullSoc((Float) params.get(4));
                    ccsController.setRemainingTimeToBulkSocSec((Float) params.get(5));
                    ccsController.setRemainingTimeToFullSocSec((Float) params.get(6));
                    ErrorHandler.loggerCcs.info("rpcSetEvSoc: {}", ccsController.getSetEvSoc());
                    break;
                case CHADEMO_ADDRESS:
                    chademoController.setEvSoc((Float) params.get(0));
                    chademoController.setBulkChargingComplete((Boolean) params.get(1));
                    chademoController.setChargingComplete((Boolean) params.get(2));
                    chademoController.setBulkSoc((Float) params.get(3));
                    chademoController.setFullSoc((Float) params.get(4));
                    chademoController.setRemainingTimeToBulkSocSec((Float) params.get(5));
                    chademoController.setRemainingTimeToFullSocSec((Float) params.get(6));
                    ErrorHandler.loggerChademo.info("rpcSetEvSoc: {}", chademoController.getSetEvSoc());
                    break;
                case GBT_ADDRESS:
                    gbtController.setEvSoc((Float) params.get(0));
                    gbtController.setBulkChargingComplete((Boolean) params.get(1));
                    gbtController.setChargingComplete((Boolean) params.get(2));
                    gbtController.setBulkSoc((Float) params.get(3));
                    gbtController.setFullSoc((Float) params.get(4));
                    gbtController.setRemainingTimeToBulkSocSec((Float) params.get(5));
                    gbtController.setRemainingTimeToFullSocSec((Float) params.get(6));
                    ErrorHandler.loggerGbt.info("rpcSetEvSoc: {}", gbtController.getSetEvSoc());
            }
        } catch (ClassCastException e) {
            ErrorHandler.logMain(e);
        }
    }
    private void rpcSetErrorCode(ArrayList params, String address) {
        try {
            switch (address) {
                case CCS_ADDRESS:
                    ccsController.setErrorCode((String) params.get(0));
                    ErrorHandler.loggerCcs.info("rpcSetErrorCode: {}", ccsController.getSetErrorCode());
                    break;
                case CHADEMO_ADDRESS:
                    chademoController.setErrorCode((String) params.get(0));
                    ErrorHandler.loggerChademo.info("rpcSetErrorCode: {}", chademoController.getSetErrorCode());
                    break;
                case GBT_ADDRESS:
                    gbtController.setErrorCode((String) params.get(0));
                    ErrorHandler.loggerGbt.info("rpcSetErrorCode: {}", gbtController.getSetErrorCode());
            }
        } catch (ClassCastException e) {
            ErrorHandler.logMain(e);
        }
    }
    private void clientClose(SocketChannel channel) throws IOException {
        //System.out.println("Client "+channel.getRemoteAddress() + " closed");
        //channel.close();
    }

    private void setLogger(ArrayList params, String channelIp) {

        JoranConfigurator configurator = new JoranConfigurator();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        configurator.setContext(loggerContext);
        loggerContext.reset();

        StringBuilder str = new StringBuilder();
        if (params.contains("{CAN:MSG}") ||
                params.contains("{CAN:SAE_MSG}") ||
                params.contains("{CAN EV->BRM}")) {
            str.append("\t\t\t");
            for (Object mes : params) {
                str.append(mes);
            }
        } else {
            for (Object mes : params) {
                str.append("\t\t\t").append(mes);
            }
        }

        // Изменение конфигурации логгера в зависимости от типа контроллера
        try {
            switch (channelIp) {
                case (IpAddress.CCS_ADDRESS):
                    configurator.doConfigure(new File(Configs.getCcsLogConfigPath()));
                    ErrorHandler.loggerChademo.info("rpcLogChademo: {}", str.toString());
                    break;
                case (IpAddress.CHADEMO_ADDRESS):
                    configurator.doConfigure(new File(Configs.getChademoLogConfigPath()));
                    ErrorHandler.loggerCcs.info("rpcLogCcs: {}", str.toString());
                    break;
                case (IpAddress.GBT_ADDRESS):
                    configurator.doConfigure(new File(Configs.getGbtLogConfigPath()));
                    ErrorHandler.loggerGbt.info("rpcLogGbt: {}", str.toString());
                    break;
                default:
                    configurator.doConfigure(new File(Configs.getServerLogConfigPath()));
                    ErrorHandler.loggerServer.info("Unknown controller: {}", str.toString());
            }
            // Возврат изначально заданной конфигурации
            configurator.doConfigure(new File(Configs.getServerLogConfigPath()));

        } catch (JoranException e) {
            ErrorHandler.logServer(e);
        }
    }

    private void logConfigurator() throws JoranException {
        JoranConfigurator configurator = new JoranConfigurator();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        configurator.doConfigure(new File(Configs.getServerLogConfigPath()));
    }
}
