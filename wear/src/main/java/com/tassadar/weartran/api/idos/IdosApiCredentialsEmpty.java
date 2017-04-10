package com.tassadar.weartran.api.idos;

public class IdosApiCredentialsEmpty extends IdosApi.Credentials {

    @Override
    public String getLogin() {
        return "";
    }

    @Override
    public String getPassword() {
        return "";
    }
}
