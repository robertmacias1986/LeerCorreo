package com.innovasystem.leermail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBase {

	public Connection conectarBase(String host, String protocolo, Integer puertoBase, String usuarioBase, String claveBase,
			String sid) {
		Connection conexion = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conexion = DriverManager
					.getConnection(
							"jdbc:oracle:thin:@(description=(address=(host="+host+")(protocol="+protocolo+")(port="+puertoBase+"))(connect_data=(sid="+sid+")))",
							usuarioBase, claveBase);
		} catch (ClassNotFoundException e1) {
			System.out.println("PROBLEMA EN EL DRIVER" + e1.toString());
		} catch (SQLException e2) {
			System.out.println("PROBLEMAS EN LA CONEXION" + e2.toString());
		}
		return conexion;
	}
	
	public Connection conectarBase(String urlBase, String usuarioBase, String claveBase) {
		Connection conexion = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conexion = DriverManager.getConnection(urlBase,usuarioBase,claveBase);
		} catch (ClassNotFoundException e1) {
			System.out.println("PROBLEMA EN EL DRIVER" + e1.toString());
		} catch (SQLException e2) {
			System.out.println("PROBLEMAS EN LA CONEXION" + e2.toString());
		}
		return conexion;
	}

}
