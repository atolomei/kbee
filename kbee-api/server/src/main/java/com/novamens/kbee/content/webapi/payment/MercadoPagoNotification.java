package com.novamens.kbee.content.webapi.payment;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MercadoPagoNotification {
    @JsonProperty("id")
    public int id;

    @JsonProperty("live_mode")
    public boolean live_mode;

    @JsonProperty("type")
    public String type;

    @JsonProperty("date_created")
    public Date date_created;

    @JsonProperty("application_id")
    public int application_id;

    @JsonProperty("user_id")
    public int user_id;

    @JsonProperty("version")
    public int version;

    @JsonProperty("api_version")
    public String api_version;

    @JsonProperty("action")
    public String action;

    private Map<String, String> data;


    @JsonAnyGetter
    public Map<String, String> getData() {
        return data;
    }

    @JsonAnySetter
    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isLive_mode() {
        return live_mode;
    }

    public void setLive_mode(boolean live_mode) {
        this.live_mode = live_mode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public int getApplication_id() {
        return application_id;
    }

    public void setApplication_id(int application_id) {
        this.application_id = application_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getApi_version() {
        return api_version;
    }

    public void setApi_version(String api_version) {
        this.api_version = api_version;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


}
