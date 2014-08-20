/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovasystem.leermail;

public class ExcepcionValoresIncompletos extends Exception {

    public ExcepcionValoresIncompletos() {
        super();
    }

    public ExcepcionValoresIncompletos(String message) {
        super(message);
    }

    public ExcepcionValoresIncompletos(String mensajeUsuario, Throwable cause) {
        super(mensajeUsuario, cause);
    }

    public ExcepcionValoresIncompletos(Throwable cause) {
        super(cause);
    }
    
    
}
