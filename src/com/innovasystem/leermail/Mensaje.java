/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.innovasystem.leermail;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Pett
 */
public class Mensaje {
    
    private Date fechaEnviado;
    private String asunto;
    private String mensaje;
    private String para;
    private List<String> de;
    private Integer numero;
    private List<byte[]>adjuntos;
    private Integer peso;
    private String carpeta;

    /**
     * Fecha en la que se ha enviado el mensaje
     * @return the fechaEnviado
     */
    public Date getFechaEnviado() {
        return fechaEnviado;
    }

    /**
     * @param fechaEnviado the fechaEnviado to set
     */
    public void setFechaEnviado(Date fechaEnviado) {
        this.fechaEnviado = fechaEnviado;
    }

    /**
     * Asunto del mensaje
     * @return the asunto
     */
    public String getAsunto() {
        return asunto;
    }

    /**
     * @param asunto the asunto to set
     */
    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    /**
     * Contenido del mensaje
     * @return the mensaje
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * @param mensaje the mensaje to set
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /**
     * Persona que envía en mensaje
     * @return the para
     */
    public String getPara() {
        return para;
    }

    /**
     * @param para the para to set
     */
    public void setPara(String para) {
        this.para = para;
    }

    /**
     * Detinatarios del mensaje
     * @return the de
     */
    public List<String> getDe() {
        return de;
    }

    /**
     * @param de the para to set
     */
    public void setDe(List<String> de) {
        this.de = de;
    }

    /**
     * Número del mensaje segun el contenido de la carpeta
     * @return the numero
     */
    public Integer getNumero() {
        return numero;
    }

    /**
     * @param numero the numero to set
     */
    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    /**
     * Archivos adjuntos que tiene el mensaje
     * @return the adjuntos
     */
    public List<byte[]> getAdjuntos() {
        return adjuntos;
    }

    /**
     * @param adjuntos the adjuntos to set
     */
    public void setAdjuntos(List<byte[]> adjuntos) {
        this.adjuntos = adjuntos;
    }

    /**
     * Peso en bytes del mensaje
     * @return the peso
     */
    public Integer getPeso() {
        return peso;
    }

    /**
     * @param peso the peso to set
     */
    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    /**
     * Carpeta en donde se encuentra el mensaje
     * @return the carpeta
     */
    public String getCarpeta() {
        return carpeta;
    }

    /**
     * @param carpeta the carpeta to set
     */
    public void setCarpeta(String carpeta) {
        this.carpeta = carpeta;
    }
    
}
