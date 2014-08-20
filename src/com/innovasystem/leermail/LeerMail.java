/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovasystem.leermail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

/**
 *
 * @author Pett
 */
public class LeerMail {

    private Properties propiedades;
    private Session session;
    private Store store;
    private String servidor;
    private String usuario;
    private String clave;

    public LeerMail() {
        propiedades = new Properties();
        propiedades.setProperty("mail.store.protocol", "imaps");
        propiedades.put("mail.imaps.ssl.trust", "*");
        propiedades.setProperty("mail.imap.port","587");
    }

    public LeerMail(String servidor, String usuario, String clave, String puerto){
        propiedades = new Properties();
        propiedades.setProperty("mail.store.protocol", "imaps");
        propiedades.put("mail.imaps.ssl.trust", "*");
        propiedades.setProperty("mail.imap.port","587");
        this.servidor = servidor;
        this.usuario = usuario;
        this.clave = clave;
    }

    /**
     * Método que trae todos los archivos de una carpeta dada, se puede
     * especificar si trae los archivos adjuntos de los mensajes, si trae los
     * archivos adjuntos esto puede hacer que el metodo demore dependiendo del
     * tamaño de los adjuntos;
     *
     * @param carpeta Carpeta de donde se leeran los mensajes, si se envía nulo
     * trae todo lo de la bandeja de entrada
     * @param traerAdjuntos Especifica si trae archivos adjuntos, si es
     * verdadero este método puede demorar dependiendo del peso de los adjuntos
     * @return
     * @throws ExcepcionValoresIncompletos
     */
    public List<Mensaje> obtenerTodosMensajes(String carpeta, boolean traerAdjuntos) throws ExcepcionValoresIncompletos {
        if (getServidor() == null) {
            throw new ExcepcionValoresIncompletos("Tiene que ingresar el protocolo (SMTP o POP3)");
        }
        if (getUsuario() == null) {
            throw new ExcepcionValoresIncompletos("No ha ingresado el usuario");
        }
        if (getClave() == null) {
            throw new ExcepcionValoresIncompletos("No ha ingresado la clave");
        }

        List<Mensaje> mensajes = null;
        try {
            if (carpeta != null) {
                carpeta = carpeta.toUpperCase();
            } else {
                carpeta = "INBOX";
            }
            session = Session.getInstance(propiedades, null);
            store = session.getStore();
            store.connect(getServidor(), getUsuario(), getClave());
            Folder inbox = store.getFolder(carpeta);

            inbox.open(Folder.READ_ONLY);
            if (inbox.getMessageCount() > 0) {
                mensajes = new ArrayList<>();
                //Message msg = inbox.getMessage(inbox.getMessageCount());
                Message msgs[] = inbox.getMessages();
                for (Message message : msgs) {
                    Mensaje mensaje = new Mensaje();
                    List<String> de = new ArrayList<>();
                    List<byte[]> lstArchivos = null;
                    String contenido = message.getContentType();
                    String contenidoDelMensaje = "";
                    InputStream input;

                    Address[] in = message.getFrom();
                    for (Address address : in) {
                        de.add(address.toString());
                    }

                    if (contenido.contains("multipart")) {
                        // El contenido puede tener archivos adjuntos
                        Multipart multiPart = (Multipart) message.getContent();
                        int numeroAdjuntos = multiPart.getCount();
                        for (int contadorAdjuntos = 0; contadorAdjuntos < numeroAdjuntos; contadorAdjuntos++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(contadorAdjuntos);

                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) && traerAdjuntos) {

                                if (lstArchivos == null) {
                                    lstArchivos = new ArrayList<>();
                                }
                                // Aqui está el archivo adjunto!
                                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                input = part.getInputStream();
                                byte[] buffer = new byte[4096];
                                int byteRead;
                                while ((byteRead = input.read(buffer)) != -1) {
                                    bao.write(buffer, 0, byteRead);
                                }
                                bao.flush();
                                bao.close();
                                input.close();
                                lstArchivos.add(bao.toByteArray());
                            } else {
                                contenidoDelMensaje = obtenerContenidoMensaje(part);
                            }
                        }
                    } else if (contenido.contains("text/plain")
                            || contenido.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            contenidoDelMensaje = content.toString();
                        }
                    }
                    mensaje.setDe(de);
                    mensaje.setAsunto(message.getSubject());
                    mensaje.setCarpeta(carpeta);
                    mensaje.setFechaEnviado(message.getSentDate());
                    mensaje.setMensaje(contenidoDelMensaje);
                    mensaje.setNumero(message.getMessageNumber());
                    mensaje.setPeso(message.getSize());
                    mensaje.setAdjuntos(lstArchivos != null ? lstArchivos : null);
                    mensajes.add(mensaje);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mensajes;
    }

    /**
     * Método que trae todos los archivos de la carpeta INBOX
     *
     * @param traerAdjuntos Especifica si se traen los archivos adjuntos, si es
     * verdadero esto hace que el metodo demore dependiendo del peso de los
     * adjuntos
     * @return
     * @throws ExcepcionValoresIncompletos
     */
    public List<Mensaje> obtenerTodosMensajesInbox(boolean traerAdjuntos) throws ExcepcionValoresIncompletos {
        if (getServidor() == null) {
            throw new ExcepcionValoresIncompletos("Tiene que ingresar el protocolo (SMTP o POP3)");
        }
        if (getUsuario() == null) {
            throw new ExcepcionValoresIncompletos("No ha ingresado el usuario");
        }
        if (getClave() == null) {
            throw new ExcepcionValoresIncompletos("No ha ingresado la clave");
        }

        List<Mensaje> mensajes = null;
        try {
            String carpeta = "INBOX";
            session = Session.getInstance(propiedades, null);
            store = session.getStore();
            store.connect(getServidor(), getUsuario(), getClave());
            Folder inbox = store.getFolder(carpeta);

            inbox.open(Folder.READ_ONLY);
            if (inbox.getMessageCount() > 0) {
                mensajes = new ArrayList<>();
                //Message msg = inbox.getMessage(inbox.getMessageCount());
                Message msgs[] = inbox.getMessages();
                for (Message message : msgs) {
                    Mensaje mensaje = new Mensaje();
                    List<String> de = new ArrayList<>();
                    List<byte[]> lstArchivos = null;
                    String contenido = message.getContentType();
                    String contenidoDelMensaje = "";
                    InputStream input;
                    Address[] in = message.getFrom();
                    for (Address address : in) {
                        de.add(address.toString());
                    }

                    if (contenido.contains("multipart")) {
                        // El contenido puede tener archivos adjuntos
                        Multipart multiPart = (Multipart) message.getContent();
                        int numeroAdjuntos = multiPart.getCount();
                        for (int contadorAdjuntos = 0; contadorAdjuntos < numeroAdjuntos; contadorAdjuntos++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(contadorAdjuntos);

                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) && traerAdjuntos) {

                                if (lstArchivos == null) {
                                    lstArchivos = new ArrayList<>();
                                }
                                // Aqui está el archivo adjunto!
                                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                input = part.getInputStream();
                                byte[] buffer = new byte[4096];
                                int byteRead;
                                while ((byteRead = input.read(buffer)) != -1) {
                                    bao.write(buffer, 0, byteRead);
                                }
                                bao.flush();
                                bao.close();
                                input.close();
                                lstArchivos.add(bao.toByteArray());
                            } else {
                                contenidoDelMensaje = obtenerContenidoMensaje(part);
                            }
                        }
                    } else if (contenido.contains("text/plain")
                            || contenido.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            contenidoDelMensaje = content.toString();
                        }
                    }
                    mensaje.setDe(de);
                    mensaje.setAsunto(message.getSubject());
                    mensaje.setCarpeta(carpeta);
                    mensaje.setFechaEnviado(message.getSentDate());
                    mensaje.setMensaje(contenidoDelMensaje);
                    mensaje.setNumero(message.getMessageNumber());
                    mensaje.setPeso(message.getSize());
                    mensaje.setAdjuntos(lstArchivos != null ? lstArchivos : null);
                    mensajes.add(mensaje);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mensajes;
    }

    /**
     * Método que trae todos los archivos de una carpeta dada, se puede
     * especificar si trae los archivos adjuntos de los mensajes, si trae los
     * archivos adjuntos esto puede hacer que el metodo demore dependiendo del
     * tamaño de los adjuntos;
     *
     * @param carpeta Carpeta de donde se leeran los mensajes, si se envía nulo
     * trae todo lo de la bandeja de entrada
     * @param traerAdjuntos Especifica si trae archivos adjuntos, si es
     * verdadero este método puede demorar dependiendo del peso de los adjuntos
     * @param fecha Dia en el que se va a buscar todos los mensajes
     * @return
     * @throws ExcepcionValoresIncompletos
     */
    public List<Mensaje> obtenerTodosMensajesPorDia(String carpeta, boolean traerAdjuntos, Date fecha) throws ExcepcionValoresIncompletos {
        if (getServidor() == null) {
            throw new ExcepcionValoresIncompletos("Tiene que ingresar el protocolo (SMTP o POP3)");
        }
        if (getUsuario() == null) {
            throw new ExcepcionValoresIncompletos("No ha ingresado el usuario");
        }
        if (getClave() == null) {
            throw new ExcepcionValoresIncompletos("No ha ingresado la clave");
        }

        List<Mensaje> mensajes = null;
        try {
            if (carpeta != null) {
                carpeta = carpeta.toUpperCase();
            } else {
                carpeta = "INBOX";
            }
            session = Session.getInstance(propiedades, null);
            store = session.getStore();
            store.connect(getServidor(), getUsuario(), getClave());
            Folder inbox = store.getFolder(carpeta);

            SearchTerm fechaBusqueda = new ReceivedDateTerm(ComparisonTerm.EQ, fecha);
            inbox.open(Folder.READ_ONLY);
            if (inbox.getMessageCount() > 0) {
                mensajes = new ArrayList<>();
                //Message msg = inbox.getMessage(inbox.getMessageCount());
                Message msgs[] = inbox.search(fechaBusqueda);
                for (Message message : msgs) {
                    Mensaje mensaje = new Mensaje();
                    List<String> de = new ArrayList<>();
                    List<byte[]> lstArchivos = null;
                    String contenido = message.getContentType();
                    String contenidoDelMensaje = "";
                    InputStream input;
                    Address[] in = message.getFrom();
                    for (Address address : in) {
                        de.add(address.toString());
                    }

                    if (contenido.contains("multipart")) {
                        // El contenido puede tener archivos adjuntos
                        Multipart multiPart = (Multipart) message.getContent();
                        int numeroAdjuntos = multiPart.getCount();
                        for (int contadorAdjuntos = 0; contadorAdjuntos < numeroAdjuntos; contadorAdjuntos++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(contadorAdjuntos);

                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) && traerAdjuntos) {

                                if (lstArchivos == null) {
                                    lstArchivos = new ArrayList<>();
                                }
                                // Aqui está el archivo adjunto!
                                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                input = part.getInputStream();
                                byte[] buffer = new byte[4096];
                                int byteRead;
                                while ((byteRead = input.read(buffer)) != -1) {
                                    bao.write(buffer, 0, byteRead);
                                }
                                bao.flush();
                                bao.close();
                                input.close();
                                lstArchivos.add(bao.toByteArray());
                            } else {
                                contenidoDelMensaje = obtenerContenidoMensaje(part);
                            }
                        }
                    } else if (contenido.contains("text/plain")
                            || contenido.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            contenidoDelMensaje = content.toString();
                        }
                    }
                    mensaje.setDe(de);
                    mensaje.setAsunto(message.getSubject());
                    mensaje.setCarpeta(carpeta);
                    mensaje.setFechaEnviado(message.getSentDate());
                    mensaje.setMensaje(contenidoDelMensaje);
                    mensaje.setNumero(message.getMessageNumber());
                    mensaje.setPeso(message.getSize());
                    mensaje.setAdjuntos(lstArchivos != null ? lstArchivos : null);
                    mensajes.add(mensaje);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return mensajes;
    }

    /**
     * Método que trae todos los archivos de una carpeta dada, se puede
     * especificar si trae los archivos adjuntos de los mensajes, si trae los
     * archivos adjuntos esto puede hacer que el metodo demore dependiendo del
     * tamaño de los adjuntos;
     *
     * @param carpeta Carpeta de donde se leeran los mensajes, si se envía nulo
     * trae todo lo de la bandeja de entrada
     * @param traerAdjuntos Especifica si trae archivos adjuntos, si es
     * verdadero este método puede demorar dependiendo del peso de los adjuntos
     * @param fechaInicio
     * @param fechaFin
     * @return
     * @throws ExcepcionValoresIncompletos
     */
    public List<Mensaje> obtenerTodosMensajesPorRangoFechas(String carpeta, boolean traerAdjuntos, Date fechaInicio, Date fechaFin) throws ExcepcionValoresIncompletos {
        if (getServidor() == null) {
            throw new ExcepcionValoresIncompletos("Tiene que ingresar el protocolo (SMTP o POP3)");
        }
        if (getUsuario() == null) {
            throw new ExcepcionValoresIncompletos("No ha ingresado el usuario");
        }
        if (getClave() == null) {
            throw new ExcepcionValoresIncompletos("No ha ingresado la clave");
        }

        List<Mensaje> mensajes = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            fechaInicio = sdf.parse(sdf.format(fechaInicio));
            fechaFin = sdf.parse(sdf.format(fechaFin));

            if (carpeta != null) {
                carpeta = carpeta.toUpperCase();
            } else {
                carpeta = "INBOX";
            }
            session = Session.getInstance(propiedades, null);
            store = session.getStore();
            store.connect(getServidor(), getUsuario(), getClave());
            Folder inbox = store.getFolder(carpeta);
            inbox.open(Folder.READ_ONLY);
            SearchTerm fechaFuturo = new ReceivedDateTerm(ComparisonTerm.LT, fechaFin);
            SearchTerm fechaPasado = new ReceivedDateTerm(ComparisonTerm.GT, fechaInicio);
            SearchTerm andTerm = new AndTerm(fechaFuturo, fechaPasado);
            if (inbox.getMessageCount() > 0) {
                mensajes = new ArrayList<>();
                //Message msg = inbox.getMessage(inbox.getMessageCount());
                Message msgs[] = inbox.search(andTerm);
                for (Message message : msgs) {
                    Mensaje mensaje = new Mensaje();
                    List<String> de = new ArrayList<>();
                    List<byte[]> lstArchivos = null;
                    String contenido = message.getContentType();
                    String contenidoDelMensaje = "";
                    InputStream input;
                    Address[] in = message.getFrom();
                    for (Address address : in) {
                        de.add(address.toString());
                    }

                    if (contenido.contains("multipart")) {
                        // El contenido puede tener archivos adjuntos
                        Multipart multiPart = (Multipart) message.getContent();
                        int numeroAdjuntos = multiPart.getCount();
                        for (int contadorAdjuntos = 0; contadorAdjuntos < numeroAdjuntos; contadorAdjuntos++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(contadorAdjuntos);

                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())&&traerAdjuntos) {
                                    if (lstArchivos == null) {
                                        lstArchivos = new ArrayList<>();
                                    }
                                    // Aqui está el archivo adjunto!
                                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                    input = part.getInputStream();
                                    byte[] buffer = new byte[4096];
                                    int byteRead;
                                    while ((byteRead = input.read(buffer)) != -1) {
                                        bao.write(buffer, 0, byteRead);
                                    }
                                    bao.flush();
                                    bao.close();
                                    input.close();
                                    lstArchivos.add(bao.toByteArray());
                                
                            } else {
                                contenidoDelMensaje = obtenerContenidoMensaje(part);
                            }
                        }
                    } else if (contenido.contains("text/plain")
                            || contenido.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            contenidoDelMensaje = content.toString();
                        }
                    }
                    mensaje.setDe(de);
                    mensaje.setAsunto(message.getSubject());
                    mensaje.setCarpeta(carpeta);
                    mensaje.setFechaEnviado(message.getSentDate());
                    mensaje.setMensaje(contenidoDelMensaje);
                    mensaje.setNumero(message.getMessageNumber());
                    mensaje.setPeso(message.getSize());
                    mensaje.setAdjuntos(lstArchivos != null ? lstArchivos : null);
                    mensajes.add(mensaje);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(LeerMail.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mensajes;
    }

    private String obtenerContenidoMensaje(Part p) throws MessagingException, IOException{
        String contenido = null;
        if (p.isMimeType("text/*")) {
            contenido = (String)p.getContent();
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = obtenerContenidoMensaje(bp);
                } else if (bp.isMimeType("text/html")) {
                    String s = obtenerContenidoMensaje(bp);
                    if (s != null)
                        return s;
                } else {
                    return obtenerContenidoMensaje(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = obtenerContenidoMensaje(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }
        return contenido;
    }
    
    /**
     * @return the props
     */
    public Properties getPropiedades() {
        return propiedades;
    }

    /**
     * @param propiedades the props to set
     */
    public void setPropiedades(Properties propiedades) {
        this.propiedades = propiedades;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * @return the store
     */
    public Store getStore() {
        return store;
    }

    /**
     * @param store the store to set
     */
    public void setStore(Store store) {
        this.store = store;
    }

    /**
     * @return the servidor
     */
    public String getServidor() {
        return servidor;
    }

    /**
     * @param servidor the servidor to set
     */
    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    /**
     * @return the usuario
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the clave
     */
    public String getClave() {
        return clave;
    }

    /**
     * @param clave the clave to set
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

}
