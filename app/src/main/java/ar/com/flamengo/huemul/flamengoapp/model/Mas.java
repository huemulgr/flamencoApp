package ar.com.flamengo.huemul.flamengoapp.model;

public class Mas {

    private String mac;
    private String nombre;
    private String estado;

    public String getMac() {
        return mac;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
}
