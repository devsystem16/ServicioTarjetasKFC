package kfc.com.modelo

import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import jdk.nashorn.internal.parser.JSONParser

import org.apache.groovy.parser.antlr4.GroovyParser.ThisFormalParameterContext
import org.json.JSONArray
import org.json.JSONObject
import com.kfc.conexion.ConexionSqlServer

class RequerimientoAutorizacion {

	def rqaut_id
	def rqaut_fecha
	def rqaut_ip
	def rqaut_puerto
	def rqaut_trama
	def rqaut_trama_separator
	def rqaut_movimiento
	def tpenv_id
	def IDFormapagoFactura
	def IDEstacion
	def IDUsersPos
	def IDStatus
	def replica
	def nivel
	String caracterSeparador
	String medioAutorizador
	Configuracion_Canal_Movimiento  switchConfig //configuracion_Canal_Movimiento
	ConexionSqlServer ocnn
	ArrayList<Catalogo> catalogo

	RequerimientoAutorizacion ( ) {
	}
	RequerimientoAutorizacion (ConexionSqlServer ocnn) {
		this.ocnn = ocnn
	}

	boolean tramaEnvioValida () {
		boolean respuesta = true ;
		
		try {

			
			String tramafaltante = Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "valida.tramafaltante")
			
			
			
			Loadcatalogo("ENVIO")
			int longitudTrama =0;
			String caracterSeparador =  this.caracterSeparador
			if (caracterSeparador == "" && this.getSoloTrama().contains("->") ) {
				caracterSeparador ="->"
			}
			for (var in this.catalogo) {
				longitudTrama += var.longitud + caracterSeparador.length()
			}
			int tramaLon = this.getSoloTrama().length()

			
//			if (tramaLon != longitudTrama) {
//				println "La trama configurada tiene ${longitudTrama} caracteres y la que se recive contiene ${tramaLon} caracteres"
//				LogsApp.getInstance().Escribir("La trama configurada tiene ${longitudTrama} caracteres y la que se recive contiene ${tramaLon} caracteres")
//				respuesta = false
//			}else {
//				respuesta = true
//				}
				
				
			if (tramafaltante.equals("si")) {
				if (tramaLon  != longitudTrama) {
					println "La trama configurada tiene ${longitudTrama} caracteres y la que se recive contiene ${tramaLon} caracteres"
					LogsApp.getInstance().Escribir("La trama configurada tiene ${longitudTrama} caracteres y la que se recive contiene ${tramaLon} caracteres")
					respuesta = false
					
				}else {
					respuesta = true
				}
			}
			
		} catch (Exception e) {
			respuesta = false
		}
		
		
		
		return respuesta  
	}

	void Loadcatalogo(String tipo) {
		String trama =  this.rqaut_trama
		// Determino el tipo de trama (compra, anulacion, recuperaTransaccion)
		String tipoTrama = trama.substring(trama.lastIndexOf("@")+1, trama.length())
		String jsonString = Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "JAR.${tipoTrama.toUpperCase()}.${this.medioAutorizador.toUpperCase()}.CATALOGO.${tipo}")


		JSONObject jsonObject = new JSONObject(jsonString);
		JSONArray jsonArray = jsonObject.getJSONArray("catalogo");
		catalogo = new ArrayList<Catalogo>();
		int length = jsonArray.length()
		for(int i=0;i<length;i++){
			try {
				JSONObject json = jsonArray.getJSONObject(i)
				catalogo.add( new Catalogo(
						json.getString("nombre_campo")
						,json.getInt("posicion")
						,json.getInt("longitud")
						,json.getString("tipo_dato")
						,json.getString("caracter_relleno")
						,json.getString("orientacion_relleno")
						,json.getString("tabla")
						,json.getString("campo")
						))
			} catch (Exception e) {
				e.printStackTrace()
			}
		}

	}

	boolean existeReverso () {
		Object registros =  0
		String sqlQuery = Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "query.verifica.reverso")

		if (sqlQuery.equals("")) {
			return false
		}
		try {
			sqlQuery =  sqlQuery.replace("prm_rsaut_movimiento",  this.rqaut_movimiento.toString())
			sqlQuery =	sqlQuery.replace("prm_rqaut_id",this.rqaut_id.toString())
					.replace("fechaActual",  Fecha.actual())
					.replace("prm_rqaut_fecha",this.rqaut_fecha.toString())
					.replace("prm_rqaut_ip",this.rqaut_ip.toString())
					.replace("prm_rqaut_puerto",this.rqaut_puerto.toString())
					.replace("prm_rqaut_trama",this.rqaut_trama.toString())
					.replace("prm_rqaut_trama_separator",this.rqaut_trama_separator.toString())
					.replace("prm_rqaut_movimiento",this.rqaut_movimiento.toString())
					.replace("prm_tpenv_id",this.tpenv_id.toString())
					.replace("prm_IDFormapagoFactura",this.IDFormapagoFactura.toString())
					.replace("prm_IDEstacion",this.IDEstacion.toString())
					.replace("prm_IDUsersPos",this.IDUsersPos.toString())
					.replace("prm_IDStatus",this.IDStatus.toString())
					.replace("prm_replica",this.replica.toString())
					.replace("SwitchIp",this.switchConfig.ipSwitchT.toString())
					.replace("?",  this.getSoloTrama().toString())
					.replace("Trama",  this.getSoloTrama().toString())
					.replace("PuertoSwitchT", this.switchConfig.puertoSwitchT.toString())
					.replace("TimeOutSwitchT", this.switchConfig.timeOutSwitchT.toString())
					.replace("LocalIp", Constantes.LOCAL_IP.toString())
		} catch (Exception e) {
			println "eerror " + e.printStackTrace()
		}

		ResultSet odr  = ocnn.select(sqlQuery )
		if (odr != null) {
			if (odr.next()) {
				registros = odr.getObject("valor")
			}
		}

		if (odr != null) {
			if (!odr.isClosed()) {
				odr.close()
			}
		}

		if ( registros   >0) {
			return true
		}else {
			return false
		}


	}


	void requerimientoPendiente (ColaProcesos cola) {

		Object [] params = [cola.imp_ip_estacion, Fecha.actual()]

		ResultSet odr  = ocnn.select( Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "query.RequerimientoPendientes"),params)

		if (odr != null) {
			if (odr.next()) {

				this.rqaut_id = odr.getObject("rqaut_id")
				this.rqaut_fecha = odr.getObject("rqaut_fecha")
				this.rqaut_ip = odr.getObject("rqaut_ip")
				this.rqaut_puerto = odr.getObject("rqaut_puerto")
				this.rqaut_trama_separator = odr.getObject("rqaut_trama")
				this.rqaut_trama = odr.getObject("rqaut_trama").toString().replace("->", "")
				this.rqaut_movimiento = odr.getObject("rqaut_movimiento")
				this.IDEstacion = odr.getObject("est_id")
				this.IDUsersPos = odr.getObject("usr_id")
				this.tpenv_id = odr.getObject("tpenv_id")
				this.IDStatus = odr.getObject("std_id")

				this.medioAutorizador= Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC,  "dispositivo.${this.tpenv_id}").replace(" ", ".")
				this.caracterSeparador  = Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "JAR.${getTipoTransaccion().toUpperCase()}.${medioAutorizador.replace(" ", ".")}.CARACTER.SEPARADOR").toString().replace("ESPACIO", " ")
			}
		}

		if (odr != null) {
			if (!odr.isClosed()) {
				odr.close()
			}
		}
	}


	String getLineasDeSecuencia () {
		String lineasDeEjecucion =Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "JAR.${this.medioAutorizador.toUpperCase().replace(" ", ".")}.${this.getTipoTransaccion().toUpperCase()}.SECUENCIA")
				.replace("SwitchIp",this.switchConfig.ipSwitchT)
				.replace("?",  this.getSoloTrama())
				.replace("Trama",  this.getSoloTrama())
				.replace("PuertoSwitchT", this.switchConfig.puertoSwitchT)
				.replace("TimeOutSwitchT", this.switchConfig.timeOutSwitchT)
				.replace("LocalIp", Constantes.LOCAL_IP)
				.replace("fechaActual",  Fecha.actual())
 
	}

	String asignarValoresSecuencia (String lineasDeEjecucion) {
		lineasDeEjecucion
				.replace("SwitchIp",this.switchConfig.ipSwitchT)
				.replace("?",  this.getSoloTrama())
				.replace("Trama",  this.getSoloTrama())
				.replace("PuertoSwitchT", this.switchConfig.puertoSwitchT)
				.replace("TimeOutSwitchT", this.switchConfig.timeOutSwitchT)
				.replace("LocalIp", Constantes.LOCAL_IP)
				.replace("fechaActual",  Fecha.actual())
		return lineasDeEjecucion


	}
	String getTipoTransaccion () {
		String trama  =this.rqaut_trama.toString()
		trama.substring(trama.lastIndexOf("@")+1, trama.length()).toUpperCase().replace(" ", ".")
	}

	int getTimeOut () {
		try {
			Integer.parseInt( Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "JAR.${this.medioAutorizador}.${this.getTipoTransaccion()}.TIME.OUT"))
		} catch (Exception e) {
			0
		}

	}
	String getSoloTrama () {
		String trama  =this.rqaut_trama.toString()
		trama.substring(0, trama.lastIndexOf("@") )
	}
	String getSoloTrama_separator () {
		String trama  =this.rqaut_trama_separator.toString()
		trama.substring(0, trama.lastIndexOf("@") )
	}

	void actualizarEstado () {
		Object [] prm =  [this.rqaut_id , this.rqaut_ip]
		int registros = ocnn.update( Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC,  "query.update_req_aut"), prm)
		println "Actualiza estado de Requerimineto 61 cod: ${rqaut_id}"
	}
	
	void destruirTrama () {
		
		LogsApp.getInstance().Escribir("Trama del movimiento : ${this.rqaut_movimiento} ID: ${rqaut_id} Destruida.")
		Object [] prm =  [this.rqaut_id , this.rqaut_ip]
		int registros = ocnn.update( "update SWT_Requerimiento_Autorizacion  set rqaut_trama =  '${ Encriptador.Encriptar (getSoloTrama())}'  WHERE   rqaut_id = ? and   rqaut_ip = ?" , prm)
		println "Actualiza estado de Requerimineto 61 cod: ${rqaut_id}"
	}
	
	
	void actualizarEstadoProcesado () {
 
		Object [] prm =  [this.rqaut_id , this.rqaut_ip]
		int registros = ocnn.update( Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC,  "query.update_req_aut_process"), prm)
		println "Actualiza estado de Requerimineto 42 cod: ${rqaut_id}"
	}
	void cargarCatalogo () {

		String trama =  this.rqaut_trama
		// Obtengo el medio autorizador usado en la transaccion desde la tabla SWT_Requerimiento_Autorizacion DB.
		//String medioAutorizador = Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC ,"dispositivo.${this.tpenv_id}"  )

		// Determino el tipo de trama (compra, anulacion, recuperaTransaccion)
		String tipoTrama = trama.substring(trama.lastIndexOf("@")+1, trama.length())

		String jsonString = Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "JAR.${tipoTrama.toUpperCase()}.${medioAutorizador.toUpperCase()}.CATALOGO.RESPUESTA")
		JSONObject jsonObject = new JSONObject(jsonString);
		JSONArray jsonArray = jsonObject.getJSONArray("catalogo");
		catalogo = new ArrayList<Catalogo>();
		int length = jsonArray.length()
		for(int i=0;i<length;i++){
			try {
				JSONObject json = jsonArray.getJSONObject(i)
				catalogo.add( new Catalogo(
						json.getString("nombre_campo")
						,json.getInt("posicion")
						,json.getInt("longitud")
						,json.getString("tipo_dato")
						,json.getString("caracter_relleno")
						,json.getString("orientacion_relleno")
						,json.getString("tabla")
						,json.getString("campo")
						))
			} catch (Exception e) {
				e.printStackTrace()
			}
		}
	}

	// Obtiene la longitud de la trama tomando en cuenta el caracter separador.!!
	int  getLongitudTrama () {
		int longitud =0 ;
		for (Catalogo c  : catalogo) {
			longitud += c.longitud  + caracterSeparador.length()
		}
		if(caracterSeparador.length() >0) {
			longitud = longitud - caracterSeparador.length()
		}
		return longitud
	}



	public void  InsertarTramaRespuestaAutorizacion (String tramaRespuesta) {

		// Asignar valores al catalogo de trama.
		int conteo =1
		int limite = catalogo.size()
		for (Catalogo c  : catalogo) {
			if (conteo == limite) {
				caracterSeparador=""
			}
			c.values = tramaRespuesta.substring(0,c.longitud)
			tramaRespuesta   = tramaRespuesta.substring(  c.longitud + caracterSeparador.length())
			conteo ++
		}

		// Generar el Insert segun la configuracion del catalogo.
		String SqlQuery  = "INSERT INTO SWT_Respuesta_Autorizacion"
		String param ="( rsaut_movimiento,raut_observacion,rsaut_fecha,"
		String values ="VALUES ( '${this.rqaut_movimiento}', '${tramaRespuesta}',  GETDATE(),"
		for (Catalogo c  : catalogo) {
			if (!c.tabla.equals("indefinido")) {
				param = param +  " ${c.campo},"
				values =values+  "'${c.values}',"
			}
		}
		param = param.substring(0 , param.length()-1) + ")"
		values = values.substring(0 , values.length()-1) + ")"

		ocnn.insert("${SqlQuery} ${param} ${values}")


	}
}
