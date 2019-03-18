package kfc.com.modelo

import com.kfc.conexion.ConexionSqlServer

import java.sql.ResultSet

class Configuracion_Canal_Movimiento {
	ConexionSqlServer ocnn
	def ipSwitchT
	def timeOutSwitchT
	def intentosConexionPinPad
	def puertoSwitchT
	def puertoCOMPinPad
	def temporizador


	Configuracion_Canal_Movimiento (ConexionSqlServer ocnn) {
		this.ocnn = ocnn
	}
	Configuracion_Canal_Movimiento () {
	}


	// Obtiene el listado de configuracion del switch desde la tavla Configuracion_canal_movimiento
	void cargarConfiguracion () {

		ResultSet odr  = ocnn.select( Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "query.configuracion_switch") )

		Configuracion_Canal_Movimiento obj  =  new Configuracion_Canal_Movimiento ()
		if (odr != null) {
			while (odr.next()) {
				String dato =odr.getObject("cco_codigo")
				String valor =odr.getObject("cco_valor")

				switch (dato.trim()){
					case  "IpSwitchT":
						ipSwitchT = valor
						break
					case  "TimeOutSwitchT":
						this.timeOutSwitchT = valor
						break
					case  "IntentosConexionPinPad":
						this.intentosConexionPinPad =valor
						break
					case  "PuertoSwitchT":
						this.puertoSwitchT = valor
						break
					case  "PuertoCOMPinPad":
						this.puertoCOMPinPad = valor
						break
					case  "Temporizador":
						this.temporizador =valor
						break
				}
			}
		}
		if (odr != null) {
			if (!odr.isClosed()) {
				odr.close()
			}
		}
	}
}
