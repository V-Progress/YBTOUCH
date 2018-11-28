package com.ideafactory.client.business.machine;

/**
 * Created by LiuShao on 2016/3/15.
 */
public class LocationBean {

    private String city;
    private String longitude;
    private String altitude;
    private String adress;
    private String adressHeight;

    public String getAdress() {
        return adress;
    }

    void setAdress(String adress) {
        this.adress = adress;
    }

    public String getAdressHeight() {
        return adressHeight;
    }

    void setAdressHeight(String adressHeight) {
        this.adressHeight = adressHeight;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAltitude() {
        return altitude;
    }

    void setAltitude(String altitude) {
        this.altitude = altitude;
    }
}
