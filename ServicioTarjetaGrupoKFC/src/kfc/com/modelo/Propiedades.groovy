package kfc.com.modelo

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties



class Propiedades {

	// Obtengo un objeto propiedades.
	static Properties getPropiedades(String file) {
		try {
			Properties propiedades = new Properties()
			String configPath = Constantes.RUTA_ARCHIVOS + ""+file+".properties"

		 
			propiedades.load(new FileInputStream(configPath))

			if (!propiedades.isEmpty()) {
				return propiedades
			}
			return null

		} catch (IOException ex) {
			System.out.println("Error, No se pudo cargar archivo  '${file}.properties': " + ex.toString())
			LogsApp.getInstance().Escribir("Error, No se pudo cargar archivo  '${file}.properties': " + ex.toString())
			ex.printStackTrace()
		}

		return null
	}

	// Obtengo el dato segun el keys de una propiedad.
	static String get(String file ,  String key) {
		try {
			String  valor = getPropiedades(file).getProperty(key)
			if (valor != null)
				return valor
			else {
				println "No se encuentra el dato [${key}] en el archivo [${file}] ${System.getProperty("line.separator")}Se detuvo el servicio"
				LogsApp.getInstance().Escribir("No se encuentra el dato [${key}] en el archivo [${file}] ${System.getProperty("line.separator")}Se detuvo el servicio")
				System.exit(1)
				return valor
			}

		} catch (Exception e) {
			return  null
		}
	}

	static Object[] getArray(String file  , String key) {
		try {
			String data = getPropiedades(file).getProperty(key).replaceAll(Constantes.backslas, "")
			Object  [] arrayDAta = data.split(Constantes.SEPARADOR_PROPERTIES)
			for (int i =0; i<arrayDAta.length ; i++ ) {
				if ( arrayDAta[i].toString().equals("null"))
					arrayDAta[i] = null
			}
			return arrayDAta // (Object[]) data.split(CONSTANTES.SEPARADOR_PROPERTIES)

		} catch (Exception e) {
			return null
		}
	}

	static Object[] getArray(String cadena) {
		try {
			String data = cadena.replaceAll(Constantes.backslas, "")
			Object  [] arrayDAta = data.split(Constantes.SEPARADOR_PROPERTIES)
			for (int i =0; i<arrayDAta.length ; i++ ) {
				if ( arrayDAta[i].toString().equals("null"))
					arrayDAta[i] = null
			}
			return arrayDAta

		} catch (Exception e) {
			return null
		}
	}

}
