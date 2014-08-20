package com.innovasystem.leermail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Robert Macías - InnovaSystem
 * Clase que revisa bandeja de entrada de mensajes de una fecha dada<br/><br/>
 * 1. Servidor de Correo (puede ser SMTP o POP3)<br/>
 * 2. Usuario de correo electronico<br/>
 * 3. Clave de correo electronico<br/>
 * 4. Fecha de busqueda de correo<br/>
 * 5. Puerto para servidor de correo<br/>
 * 6. URL de base de datos x Ej. jdbc:oracle:thin:@//soldb:1521/orcl<br/>
 * 7. Usuario de base de datos<br/>
 * 8. Clave de base de datos<br/>
 * 9. Esquema de la base de datos<br/>
 * 10. Nombre de la tabla a que se va a insertar<br/> 
 * 11. Si trae los archivos adjuntos o no<br/>
 */
public class LeerCorreos {

	public static void main(String... args) {
		try {
			if (args.length > 0) {
				String servidorCorreo = args[0];
				String usuarioCorreo = args[1];
				String claveCorreo = args[2];
				Date fechaBusquedaCorreo = new SimpleDateFormat("dd/MM/yyyy").parse(args[3]);
				String puertoCorreo = args[4];

				String urlBase = args[5];
				String usuarioBase = args[6];
				String claveBase = args[7];
				String esquemaTabla = args[8];
				String nombreTabla = args[9];
				
				boolean traerAdjuntos = Boolean.getBoolean(args[10]);

				LeerMail leer = new LeerMail(servidorCorreo, usuarioCorreo, claveCorreo, puertoCorreo);
				List<Mensaje> mensajes;

				mensajes = leer.obtenerTodosMensajesPorDia("INBOX", traerAdjuntos, fechaBusquedaCorreo);

				if (mensajes != null) {
					ConexionBase conexion = new ConexionBase();
					Connection c = conexion.conectarBase(urlBase, usuarioBase, claveBase);
					PreparedStatement ps = null;
					System.out.println("Hay " + mensajes.size() + " mensajes");
					for (Mensaje m : mensajes) {
						// System.out.println(m.getMensaje());
						if (m.getAdjuntos() != null) {
							System.out.println("\n Y tiene un adjunto!");
						}
						String de = "";
						for (String origenMensajes : m.getDe()) {
							de = de + origenMensajes + ";";
						}
						String sentencia = "INSERT INTO " + (esquemaTabla + "." + nombreTabla)
								+ "(ASUNTO,TEXTO,FECHA,DIRECCION,NUMERO)" + "VALUES('" + m.getAsunto() + "','"
								+ m.getMensaje() + "','" + m.getFechaEnviado() + "','" + de + "','" + m.getNumero()
								+ "')";

						ps = c.prepareStatement(sentencia);
						ResultSet r = ps.executeQuery();
						r.next();
						r.close();
						ps.close();

						System.out.println(sentencia);
					}
				} else {
					System.out.println("No hay mensajes nuevos");
				}
			} else {
				System.out.println("No ha enviado los datos completos");
			}
		} catch (ExcepcionValoresIncompletos e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
