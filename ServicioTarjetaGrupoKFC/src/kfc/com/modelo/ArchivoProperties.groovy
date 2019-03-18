package kfc.com.modelo

import com.kfc.conexion.ConexionSqlServer

import java.sql.ResultSet

class ArchivoProperties {
	ConexionSqlServer oCnn


	int verificar () {

		if (oCnn == null) {
			oCnn =  ConexionSqlServer.getInstance()
			oCnn.abrirConexion()
		}
		//
		int resultado =0

		ResultSet odr =	 oCnn.selectSQL(Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, Constantes.QUERY_ACTUALIZAR_APP) )
		if (odr != null) {

			if (odr.next()) {
				resultado = odr.getInt("actualizar")
				odr.close()
				oCnn.insert(Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, Constantes.QUERY_INACTIVAR_ACTUALIZACION) )
			}

			if (!odr.isClosed()) {
				odr.close()
				odr = null
			}else {
				odr = null
			}
		}

		return resultado
	}

	// Construye el archivo de configuración de la aplicación el cual contiene los métodos a usar desde el jar para el envío de transacciones con pagos mediante tarjetas.
	void construir (sobrescribirArchivo) {

		try {

			// ResultSet odr =	 oCnn.selectSQL("EXEC switch.configuracionInicialProperties")
			String ruta = Constantes.RUTA_ARCHIVOS +""+ Constantes.ARCHIVO_CONFIGURACION_DINAMICO
			File archivo = new File(ruta);
			BufferedWriter fichero;

			if (sobrescribirArchivo) {
				if(archivo.exists() ) {
					archivo.delete()
				}
			}else {

				if(archivo.exists() ) {
					println "Se utilizo un archivo de configuracion existente."
					return
				}

			}
 
			// si el archico existe y

			fichero = new BufferedWriter(new FileWriter(archivo));
			String cadena = Propiedades.get(Constantes.ARCHIVO_APPLICATION_STATIC, "cadena.id")
			println "Actualizando archivo de configuracion..."
			
			ResultSet odr =  oCnn.selectSQL("EXEC switch.configuracionInicialPropertiesDispositivos ${cadena}"  )
			while (odr.next()) {

				String newString = new String(odr.getString("recurso").toString().getBytes("UTF-8"), "UTF-8");
				fichero.write( newString)
				fichero.newLine()
			}

			fichero.newLine()
			fichero.close()
			println "Archivo creado correctamente. [${ruta}]"


		} catch (Exception e) {
			println e.getMessage()
		}


	}
}







