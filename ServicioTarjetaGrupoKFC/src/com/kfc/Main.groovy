package com.kfc


//import javax.swing.JOptionPane
import java.beans.DesignMode
import java.sql.Connection
import com.kfc.conexion.ConexionSqlServer
import com.kfc.modelo.reflexion.JarLector

import groovy.transform.Field
import kfc.com.modelo.ArchivoProperties
import kfc.com.modelo.ColaProcesos
import kfc.com.modelo.Constantes
import kfc.com.modelo.Despachador
import kfc.com.modelo.LogsApp
import kfc.com.modelo.Propiedades
import kfc.com.modelo.ValidadorDispositivos

import sun.security.krb5.internal.EncTicketPart
import sun.security.util.Length
import java.lang.reflect.InvocationTargetException



import java.time.Duration;
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.jar.Attributes
import java.util.jar.Manifest
import javax.swing.*;
public class Main {

	static public byte cantidadConsultasParaLiberarMemoria =0
	static public byte cantidadConsultasParaLiberarMemoriaTemp = 0
	static Runtime garbage = Runtime.getRuntime();


	private static ServerSocket SERVER_SOCKET;

	static main(args) {
 
		
		try {
			SERVER_SOCKET = new ServerSocket();
			SERVER_SOCKET = new ServerSocket(1616);
			
		} catch (IOException x) {
			LogsApp.getInstance().Escribir("El servicio de tarjetas YA se encuentra en ejecucion")
			JOptionPane.showMessageDialog( null ,"El servicio de tarjetas YA se encuentra en ejecucion"  , "Error" , JOptionPane.ERROR_MESSAGE) //JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}

		println "Iniciando Servicio..."
		
		// Creo Conexion SQl SERVER
		ConexionSqlServer conexion = ConexionSqlServer.getInstance()
		conexion.obtenerConexion()

		LogsApp.getInstance().Escribir("SERVICIO TARJETA INICIADO")
		// Constuye el archivo de configuración .properties que contiene las configuraciones para el envio de requerimientos de pagos con tarjeta.
		ArchivoProperties p = new ArchivoProperties()
		p.oCnn = conexion
		p.construir(false)


		// Limpieza de colas en espera.
		ColaProcesos oColaP =  ColaProcesos.getInstance()
		oColaP.oCnn = conexion
		oColaP.limpiarCola(false)
		// Fin Limpieza de colas en espera.

		cantidadConsultasParaLiberarMemoria=Integer.parseInt( Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC,  Constantes.tiempoInactividadParaLimpiar))
		cantidadConsultasParaLiberarMemoriaTemp =cantidadConsultasParaLiberarMemoria

		def speed=   Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC,  Constantes.TIMER_LOOP_APP)
		int sTiempo = Integer.parseInt(speed)
		Main  principal	 = new Main ()
		Despachador.ocnn = conexion

		garbage.gc()
		System.out.println("Memoria Liberada:  "+ garbage.freeMemory() );
		println "Servicio Iniciado, en espera de transacciones."
		
		while (true)
		{
			try {
				if (p.verificar() == 1)
					p.construir(true)
			} catch (Exception e) {
				p.oCnn = null
				garbage.gc()
			}

			principal.Ejecutardemonio()
			Thread.sleep(sTiempo)

			cantidadConsultasParaLiberarMemoriaTemp --

		}
	}

	void Ejecutardemonio() {
		try {
			Despachador.despacharTarjeta(garbage)
		} catch (Exception e) {
			println "Exception dentro del servicio (!controlado)" + e.getMessage()
			Despachador.ocnn = null ;
			garbage.gc()
		}
	}
}
