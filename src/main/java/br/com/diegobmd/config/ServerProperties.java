package br.com.diegobmd.config;

import com.google.inject.Inject;

import javax.inject.Named;

public class ServerProperties {

    @Inject(optional = true)
    @Named("server.port")
    private Integer port = 8080;

    @Inject(optional = true)
    @Named("server.address")
    private String address = "0.0.0.0";

    @Inject(optional = true)
    @Named("server.context-path")
    private String contextPath = "";

    @Inject(optional = true)
    @Named("server.url")
    private String url;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public boolean hasContextPath(){
        return contextPath != null && contextPath.trim().length() > 0;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
