package ru.promelectronika.chargeController;

public class ControllerParams {
    //rpcPing
    private int selfConnectionInputState;
    private int selfConnectionOutputState;
    //SET_FW_VERSION
    private String version;
    //SET_PROTOCOL_VERSION
    private String protocolVersion;
    //SET_SECC_CURRENT_STATE
    private int chargeState = 0;
    private String chargeStateProtocolSpecific = "";
    //SET_EV_LIMITS
    private float maximumPowerLimitW;
    private float maximumVoltageLimitV;
    private float maximumCurrentLimitA;
    //SET_EV_TARGET_PARAMS
    private int invertorState = 4;
    private boolean outputContactorOn;
    private boolean insulationControlOn;
    private float targetVoltageV;
    private float targetCurrentA;
    //SET_EV_PARAMS
    private String evId;
    private float energyCapacity;
    private float energyRequest;
    //SET_EV_STATE
    private boolean evReady;
    private String evErrorCode;
    //SET_EV_SOC
    private float evSoc;
    private boolean bulkChargingComplete;
    private boolean chargingComplete;
    private float bulkSoc;
    private float fullSoc;
    private float remainingTimeToBulkSocSec;
    private float remainingTimeToFullSocSec;
    //SET_ERROR_CODE
    private String errorCode;

    public int getSelfConnectionInputState() {
        return selfConnectionInputState;
    }
    public void setSelfConnectionInputState(int selfConnectionInputState) {
        this.selfConnectionInputState = selfConnectionInputState;
    }
    public int getSelfConnectionOutputState() {
        return selfConnectionOutputState;
    }
    public void setSelfConnectionOutputState(int selfConnectionOutputState) {
        this.selfConnectionOutputState = selfConnectionOutputState;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getProtocolVersion() {
        return protocolVersion;
    }
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    public int getChargeState() {
        return chargeState;
    }
    public void setChargeState(int chargeState) {
        this.chargeState = chargeState;
    }
    public String getChargeStateProtocolSpecific() {
        return chargeStateProtocolSpecific;
    }
    public void setChargeStateProtocolSpecific(String chargeStateProtocolSpecific) {
        this.chargeStateProtocolSpecific = chargeStateProtocolSpecific;
    }
    public float getMaximumPowerLimitW() {
        return maximumPowerLimitW;
    }
    public void setMaximumPowerLimitW(float maximumPowerLimitW) {
        this.maximumPowerLimitW = maximumPowerLimitW;
    }
    public float getMaximumVoltageLimitV() {
        return maximumVoltageLimitV;
    }
    public void setMaximumVoltageLimitV(float maximumVoltageLimitV) {
        this.maximumVoltageLimitV = maximumVoltageLimitV;
    }
    public float getMaximumCurrentLimitA() {
        return maximumCurrentLimitA;
    }
    public void setMaximumCurrentLimitA(float maximumCurrentLimitA) {
        this.maximumCurrentLimitA = maximumCurrentLimitA;
    }
    public int getInvertorState() {
        return invertorState;
    }
    public void setInvertorState(int invertorState) {
        this.invertorState = invertorState;
    }
    public boolean isOutputContactorOn() {
        return outputContactorOn;
    }
    public void setOutputContactorOn(boolean outputContactorOn) {
        this.outputContactorOn = outputContactorOn;
    }
    public boolean isInsulationControlOn() {
        return insulationControlOn;
    }
    public void setInsulationControlOn(boolean insulationControlOn) {
        this.insulationControlOn = insulationControlOn;
    }
    public float getTargetVoltageV() {
        return targetVoltageV;
    }
    public void setTargetVoltageV(float targetVoltageV) {
        this.targetVoltageV = targetVoltageV;
    }
    public float getTargetCurrentA() {
        return targetCurrentA;
    }
    public void setTargetCurrentA(float targetCurrentA) {
        this.targetCurrentA = targetCurrentA;
    }
    public String getEvId() {
        return evId;
    }
    public void setEvId(String evId) {
        this.evId = evId;
    }
    public float getEnergyCapacity() {
        return energyCapacity;
    }
    public void setEnergyCapacity(float energyCapacity) {
        this.energyCapacity = energyCapacity;
    }
    public float getEnergyRequest() {
        return energyRequest;
    }
    public void setEnergyRequest(float energyRequest) {
        this.energyRequest = energyRequest;
    }
    public boolean isEvReady() {
        return evReady;
    }
    public void setEvReady(boolean evReady) {
        this.evReady = evReady;
    }
    public String getEvErrorCode() {
        return evErrorCode;
    }
    public void setEvErrorCode(String evErrorCode) {
        this.evErrorCode = evErrorCode;
    }
    public float getEvSoc() {
        return evSoc;
    }
    public void setEvSoc(float evSoc) {
        this.evSoc = evSoc;
    }
    public boolean isBulkChargingComplete() {
        return bulkChargingComplete;
    }
    public void setBulkChargingComplete(boolean bulkChargingComplete) {
        this.bulkChargingComplete = bulkChargingComplete;
    }
    public boolean isChargingComplete() {
        return chargingComplete;
    }
    public void setChargingComplete(boolean chargingComplete) {
        this.chargingComplete = chargingComplete;
    }
    public float getBulkSoc() {
        return bulkSoc;
    }
    public void setBulkSoc(float bulkSoc) {
        this.bulkSoc = bulkSoc;
    }
    public float getFullSoc() {
        return fullSoc;
    }
    public void setFullSoc(float fullSoc) {
        this.fullSoc = fullSoc;
    }
    public float getRemainingTimeToBulkSocSec() {
        return remainingTimeToBulkSocSec;
    }
    public void setRemainingTimeToBulkSocSec(float remainingTimeToBulkSocSec) {
        this.remainingTimeToBulkSocSec = remainingTimeToBulkSocSec;
    }
    public float getRemainingTimeToFullSocSec() {
        return remainingTimeToFullSocSec;
    }
    public void setRemainingTimeToFullSocSec(float remainingTimeToFullSocSec) {
        this.remainingTimeToFullSocSec = remainingTimeToFullSocSec;
    }
    public String getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getRpcPing() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tConnection Input State: ");
        str.append(selfConnectionInputState);
        str.append("\n\tConnection Output State: ");
        str.append(selfConnectionOutputState);
        return str.toString();
    }
    public String getSetFwVersion() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tVersion: ");
        str.append(version);
        return str.toString();
    }
    public String getSetProtocolVersion() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tVersion: ");
        str.append(protocolVersion);
        return str.toString();
    }
    public String getSetSeccCurrentState() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tCharge state: ");
        str.append(chargeState);
        str.append("\n\tCharge state protocol specific: ");
        str.append(chargeStateProtocolSpecific);
        return str.toString();
    }
    public String getSetEvLimits() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tMaximum power limit: ");
        str.append(maximumPowerLimitW);
        str.append("\n\tMaximum voltage limit: ");
        str.append(maximumVoltageLimitV);
        str.append("\n\tMaximum current limit: ");
        str.append(maximumCurrentLimitA);
        return str.toString();
    }
    public String getSetEvTargetParams() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tInvertor state: ");
        str.append(invertorState);
        str.append("\n\tOutput contactor on: ");
        str.append(outputContactorOn);
        str.append("\n\tInsulation control on: ");
        str.append(insulationControlOn);
        str.append("\n\tTarget voltage: ");
        str.append(targetVoltageV);
        str.append("\n\tTarget current: ");
        str.append(targetCurrentA);
        return str.toString();
    }
    public String getSetEvParams() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tEV id: ");
        str.append(evId);
        str.append("\n\tEnergy capacity: ");
        str.append(energyCapacity);
        str.append("\n\tEnergy request: ");
        str.append(energyRequest);
        return str.toString();
    }
    public String getSetEvState() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tEV Ready: ");
        str.append(evReady);
        str.append("\n\tEV Error Code: ");
        str.append(evErrorCode);
        return str.toString();
    }
    public String getSetEvSoc() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tEV soc: ");
        str.append(evSoc);
        str.append("\n\tBulk charging complete: ");
        str.append(bulkChargingComplete);
        str.append("\n\tCharging complete: ");
        str.append(chargingComplete);
        str.append("\n\tBulk soc: ");
        str.append(bulkSoc);
        str.append("\n\tFull soc: ");
        str.append(fullSoc);
        str.append("\n\tRemaining time to bulk soc sec: ");
        str.append(remainingTimeToBulkSocSec);
        str.append("\n\tRemaining time to full soc sec: ");
        str.append(remainingTimeToFullSocSec);
        return str.toString();
    }
    public String getSetErrorCode() {
        StringBuilder str = new StringBuilder();
        str.append("\n\tError code: ");
        str.append(errorCode);
        return str.toString();
    }
}
