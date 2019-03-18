package kfc.com.modelo
import gnu.io.CommPortIdentifier;
import java.util.ArrayList
import java.util.List

import com.kfc.Main
import com.kfc.conexion.ConexionSqlServer

class Despachador {
	static ConexionSqlServer ocnn

	static void despacharTarjeta(Runtime garbage) {
		// Obtener los registros relacionados a pagos con tarjeta desde canal_movimiento
		if (ocnn == null) {
			ocnn =  ConexionSqlServer.getInstance()
			ocnn.abrirConexion()
			println "Se reestablecio la conexion."

			ColaProcesos oColaP =  ColaProcesos.getInstance()
			oColaP.oCnn = ocnn
			oColaP.limpiarCola(false)

		}

		 
		ColaProcesos oColaP =  ColaProcesos.getInstance()
		oColaP.oCnn = ocnn

		List<ColaProcesos> listaColaProceso =	oColaP.getListado()
		int cantidadColas = listaColaProceso.size()

		if (cantidadColas >0) {
			Main.cantidadConsultasParaLiberarMemoriaTemp=Main.cantidadConsultasParaLiberarMemoria
			println "${cantidadColas} Colas encontradas"
			Tarjetas tarjeta
			for (ColaProcesos cola : listaColaProceso) {
				tarjeta = new Tarjetas( cola, ocnn)
				//tarjeta = Tarjetas.getInstance(cola, ocnn)
				tarjeta.procesar()
			}
			tarjeta = null
			// Limpia Memoria
			garbage.gc()
			System.out.println("Memoria liberada :  "+ garbage.freeMemory() );
			println "Fin proceso colas."
		}else {
 
			if (Main.cantidadConsultasParaLiberarMemoriaTemp == 0) {
				garbage.gc()
				//println "Limpieza por inactividad"
				Main.cantidadConsultasParaLiberarMemoriaTemp=Main.cantidadConsultasParaLiberarMemoria
				
			}
			
		}

	}

}
