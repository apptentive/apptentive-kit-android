/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import androidx.annotation.VisibleForTesting;

import com.apptentive.android.sdk.util.StringUtils;

import java.io.Serializable;

public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uuid;
    private String osName;
    private String osVersion;
    private String osBuild;
    private int osApiLevel;
    private String manufacturer;
    private String model;
    private String board;
    private String product;
    private String brand;
    private String cpu;
    private String device;
    private String carrier;
    private String currentCarrier;
    private String networkType;
    private String buildType;
    private String buildId;
    private String bootloaderVersion;
    private String radioVersion;
    private CustomData customData;
    private String localeCountryCode;
    private String localeLanguageCode;
    private String localeRaw;
    private String utcOffset;
    private String advertiserId;
    private IntegrationConfig integrationConfig;


    public Device() {
        customData = new CustomData();
        integrationConfig = new IntegrationConfig();
    }

    @VisibleForTesting
    public Device(
            String uuid,
            String osName,
            String osVersion,
            String osBuild,
            int osApiLevel,
            String manufacturer,
            String model,
            String board,
            String product,
            String brand,
            String cpu,
            String device,
            String carrier,
            String currentCarrier,
            String networkType,
            String buildType,
            String buildId,
            String bootloaderVersion,
            String radioVersion,
            CustomData customData,
            String localeCountryCode,
            String localeLanguageCode,
            String localeRaw,
            String utcOffset,
            String advertiserId,
            IntegrationConfig integrationConfig
    ) {
        this.uuid = uuid;
        this.osName = osName;
        this.osVersion = osVersion;
        this.osBuild = osBuild;
        this.osApiLevel = osApiLevel;
        this.manufacturer = manufacturer;
        this.model = model;
        this.board = board;
        this.product = product;
        this.brand = brand;
        this.cpu = cpu;
        this.device = device;
        this.carrier = carrier;
        this.currentCarrier = currentCarrier;
        this.networkType = networkType;
        this.buildType = buildType;
        this.buildId = buildId;
        this.bootloaderVersion = bootloaderVersion;
        this.radioVersion = radioVersion;
        this.customData = customData;
        this.localeCountryCode = localeCountryCode;
        this.localeLanguageCode = localeLanguageCode;
        this.localeRaw = localeRaw;
        this.utcOffset = utcOffset;
        this.advertiserId = advertiserId;
        this.integrationConfig = integrationConfig;
    }


    // TODO: unit tests
    public Device clone() {
        Device clone = new Device();
        clone.uuid = uuid;
        clone.osName = osName;
        clone.osVersion = osVersion;
        clone.osBuild = osBuild;
        clone.osApiLevel = osApiLevel;
        clone.manufacturer = manufacturer;
        clone.model = model;
        clone.board = board;
        clone.product = product;
        clone.brand = brand;
        clone.cpu = cpu;
        clone.device = device;
        clone.carrier = carrier;
        clone.currentCarrier = currentCarrier;
        clone.networkType = networkType;
        clone.buildType = buildType;
        clone.buildId = buildId;
        clone.bootloaderVersion = bootloaderVersion;
        clone.radioVersion = radioVersion;
        if (customData != null) {
            clone.customData.putAll(customData);
        }
        clone.localeCountryCode = localeCountryCode;
        clone.localeLanguageCode = localeLanguageCode;
        clone.localeRaw = localeRaw;
        clone.utcOffset = utcOffset;
        clone.advertiserId = advertiserId;
        if (integrationConfig != null) {
            clone.integrationConfig = integrationConfig.clone();
        }
        return clone;
    }

    //region Getters & Setters

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        if (!StringUtils.equal(this.uuid, uuid)) {
            this.uuid = uuid;
        }
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getOsBuild() {
        return osBuild;
    }

    public int getOsApiLevel() {
        return osApiLevel;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        if (!StringUtils.equal(this.manufacturer, manufacturer)) {
            this.manufacturer = manufacturer;
        }
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        if (!StringUtils.equal(this.model, model)) {
            this.model = model;
        }
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        if (!StringUtils.equal(this.board, board)) {
            this.board = board;
        }
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        if (!StringUtils.equal(this.product, product)) {
            this.product = product;
        }
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (!StringUtils.equal(this.brand, brand)) {
            this.brand = brand;
        }
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        if (!StringUtils.equal(this.cpu, cpu)) {
            this.cpu = cpu;
        }
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        if (!StringUtils.equal(this.device, device)) {
            this.device = device;
        }
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        if (!StringUtils.equal(this.carrier, carrier)) {
            this.carrier = carrier;
        }
    }

    public String getCurrentCarrier() {
        return currentCarrier;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        if (!StringUtils.equal(this.networkType, networkType)) {
            this.networkType = networkType;
        }
    }

    public String getBuildType() {
        return buildType;
    }

    public String getBuildId() {
        return buildId;
    }

    public String getBootloaderVersion() {
        return bootloaderVersion;
    }

    public String getRadioVersion() {
        return radioVersion;
    }

    public CustomData getCustomData() {
        return customData;
    }

    public void setCustomData(CustomData customData) {
        this.customData = customData;
    }

    public String getLocaleCountryCode() {
        return localeCountryCode;
    }

    public String getLocaleLanguageCode() {
        return localeLanguageCode;
    }

    public String getLocaleRaw() {
        return localeRaw;
    }

    public String getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(String utcOffset) {
        if (!StringUtils.equal(this.utcOffset, utcOffset)) {
            this.utcOffset = utcOffset;
        }
    }

    public String getAdvertiserId() {
        return advertiserId;
    }

    public IntegrationConfig getIntegrationConfig() {
        return integrationConfig;
    }

    //endregion

}
