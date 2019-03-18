package kfc.com.modelo
import java.time.Duration;
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.sql.ResultSet
import java.util.ArrayList
import org.json.JSONArray
import org.json.JSONObject
import com.kfc.conexion.ConexionSqlServer
import com.kfc.modelo.reflexion.JarLector

class Respuesta_Autorizacion {
	String tramaRespuesta
	ConexionSqlServer ocnn
	ArrayList<Catalogo> catalogo
	RequerimientoAutorizacion requerimiento
	ArrayList<MensajesRespuestas> ListadomensajesRespuesta
	String [] posicionRepsuesta
	MensajesRespuestas mensajeRespuesta

	boolean respuestatipoObjeto = false

	private static Respuesta_Autorizacion instance

	static  Respuesta_Autorizacion getInstancia( ){
		if (instance == null)
			instance = new Respuesta_Autorizacion ( )
		instance
	}


	void Loadcatalogo(String tipo) {
		String trama =  requerimiento.rqaut_trama
		// Determino el tipo de trama (compra, anulacion, recuperaTransaccion)
		String tipoTrama = trama.substring(trama.lastIndexOf("@")+1, trama.length())
		String jsonString = Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "JAR.${tipoTrama.toUpperCase()}.${requerimiento.medioAutorizador.toUpperCase()}.CATALOGO.${tipo}")


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
	void cargarCatalogo (String tipo) {

		String trama =  requerimiento.rqaut_trama
		// Determino el tipo de trama (compra, anulacion, recuperaTransaccion)
		String tipoTrama = trama.substring(trama.lastIndexOf("@")+1, trama.length())
		String jsonString =  Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "JAR.${tipoTrama.toUpperCase()}.${requerimiento.medioAutorizador.toUpperCase()}.CATALOGO.${tipo}")

		if (jsonString.equals(""))
		{
			insetarBug("No se ha configurado el catalogo: ${requerimiento.medioAutorizador.toUpperCase()}.${tipoTrama.toUpperCase()} (stopped service)")
			println "No hay catalogo de ${tipo} para:${requerimiento.medioAutorizador.toUpperCase()} - ${tipoTrama.toUpperCase()}"
			LogsApp.getInstance().Escribir("------------Exception: No se ha configurado el catalogo:  ${tipo} para:${requerimiento.medioAutorizador.toUpperCase()}.${tipoTrama.toUpperCase()}")
			System.exit(1)
			return
		}


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

		try {
			if (tipo.equals("ENVIO")) {
				String caracterSeparador  = requerimiento.caracterSeparador
				if ( caracterSeparador.equals("")) {
					caracterSeparador ="->"
				}
				Object [] values  = requerimiento.getSoloTrama_separator().split(caracterSeparador)
				for	(int i=0 ; i < catalogo.size() ; i++ ) {
					catalogo[i].values = values[i]
				}
			}
		} catch (Exception e) {
			LogsApp.getInstance().Escribir("Exception------ Trama de  envio no valida. " + e.getMessage())
		}


	}

	public boolean completarTramaSegunCatalogo (String trama) {
		if(requerimiento.caracterSeparador.toString().length() >0) {

			String [] VectorTrama  = trama.split(requerimiento.caracterSeparador.toString());
			if (VectorTrama.length == catalogo.size()) {

				int length = VectorTrama.length ;
				for(int i=0 ; i< length ; i++ ) {

					String caracterRelleno =((catalogo[i].caracter_relleno.toString() == "N")? " " : catalogo[i].caracter_relleno.toString()).toString().replace("ESPACIO"," ")

					// CASO QUE FALTA COMPLETAR CARACTERES EN EL DATO.
					if(VectorTrama[i].length() < catalogo[i].longitud) {
						if ( catalogo[i].orientacion_relleno.equals("I")) {
							//Relleno hacia la izquierda
							for(int j=0 ; j<catalogo[i].longitud ; j ++) {
								VectorTrama[i] = caracterRelleno + VectorTrama[i]  ;
							}
						}else {
							// Relleno hacia la Derecha.
							for(int j=0 ; j<catalogo[i].longitud ; j ++) {
								VectorTrama[i] = VectorTrama[i]  + caracterRelleno   ;
							}
						}
					}else {
						// CASO QUE SOBRAN CARACTERES EN EL DATO.
						if ( catalogo[i].orientacion_relleno.equals("I")) {
							String cadena =VectorTrama[i]
							int inicioNuevaCadena =  cadena.length() - catalogo[i].longitud
							VectorTrama[i] =  cadena.substring(inicioNuevaCadena , cadena.length())
						}else {
							VectorTrama[i] = VectorTrama[i].toString().substring(0 ,  catalogo[i].longitud)
						}
					}
				}
				this.tramaRespuesta = String.join(requerimiento.caracterSeparador.toString(), VectorTrama);
				return true
			}
			else
			{
				return false
			}
		} // Validacion= Tiene caracter separador..

		return false
	}

	public void insetarBugTIME_OUT (String mensajeBug) {
		LogsApp.getInstance().Escribir("Insertando respuesta en BD.")
		String SqlQuery  = "INSERT INTO SWT_Respuesta_Autorizacion (rsaut_trama,rsaut_fecha,rsaut_movimiento,raut_observacion,IDStatus,SWT_Respuesta_AutorizacionVarchar1) VALUES ( '${mensajeBug}',GETDATE() , '${requerimiento.rqaut_movimiento}','${mensajeBug}', ${Constantes.ESTADO_SWT_Respuesta_Autorizacion_ERROR},'TIME OUT')"
		ocnn.insert(SqlQuery)
		LogsApp.getInstance().Escribir("Respuesta insertada en BD")
	}

	public void insetarBug (String mensajeBug) {
		LogsApp.getInstance().Escribir("Insertando respuesta en BD.")
		String SqlQuery  = "INSERT INTO SWT_Respuesta_Autorizacion (rsaut_trama,rsaut_fecha,rsaut_movimiento,raut_observacion,IDStatus,SWT_Respuesta_AutorizacionVarchar1) VALUES ( '${mensajeBug}',GETDATE() , '${requerimiento.rqaut_movimiento}','${mensajeBug}', ${Constantes.ESTADO_SWT_Respuesta_Autorizacion_ERROR},'ERROR')"
		ocnn.insert(SqlQuery)
		LogsApp.getInstance().Escribir("Respuesta insertada en BD")
	}
	public void  InsertarTramaRespuestaAutorizacionERROR () {
		LogsApp.getInstance().Escribir("Insertando respuesta en BD.")
		String SqlQuery  = "INSERT INTO SWT_Respuesta_Autorizacion"
		String param ="(rsaut_movimiento, rsaut_trama,raut_observacion,rsaut_fecha,ttra_codigo,IDStatus,SWT_Respuesta_AutorizacionVarchar1)"
		String values ="VALUES ('${requerimiento.rqaut_movimiento}','${this.tramaRespuesta}',  '${mensajeRespuesta.mensaje}',  GETDATE() , '${mensajeRespuesta.codigo}' , ${Constantes.ESTADO_SWT_Respuesta_Autorizacion_ERROR},'${getEstadoSegunSecuencia(mensajeRespuesta)}')"
		println  "Query::::.   ${SqlQuery} ${param} ${values}"
		ocnn.insert("${SqlQuery} ${param} ${values}")
		LogsApp.getInstance().Escribir("Respuesta insertada en BD")
	}

	String getEstadoSegunSecuencia (MensajesRespuestas mns) {
		mns.secuencia.toString().toUpperCase().equals("APROBADA") ? "APROBADA":"NO APROBADO"
	}


	public void  InsertarObjetoRespuestaAutorizacion () {
		LogsApp.getInstance().Escribir("Insertando respuesta en BD.")
		// Generar el Insert segun la configuracion del catalogo.
		String SqlQuery  = "INSERT INTO SWT_Respuesta_Autorizacion"
		String param ="(rsaut_trama,rsaut_movimiento,raut_observacion,rsaut_fecha,IDStatus,SWT_Respuesta_AutorizacionVarchar1,"
		String values ="VALUES ('${this.tramaRespuesta}','${requerimiento.rqaut_movimiento}', '${mensajeRespuesta.mensaje}',GETDATE(), ${Constantes.ESTADO_SWT_Respuesta_Autorizacion_OK},'${getEstadoSegunSecuencia(mensajeRespuesta)}',"
		for (Catalogo c  : catalogo) {
			if (!c.tabla.equals("indefinido")) {
				param = param +  " ${c.campo},"
				values =values+  "'${c.values}',"
			}
		}
		param = param.substring(0 , param.length()-1) + ")"
		values = values.substring(0 , values.length()-1) + ")"


		println "${SqlQuery} ${param} ${values}"
		ocnn.insert("${SqlQuery} ${param} ${values}")
		LogsApp.getInstance().Escribir("Respuesta insertada en BD")
	}

	public void  InsertarTramaRespuestaAutorizacion () {
		LogsApp.getInstance().Escribir("Insertando respuesta en BD.")
		String tramaR = tramaRespuesta
		// Asignar valores al catalogo de trama.
		int conteo =1
		int limite = catalogo.size()
		for (Catalogo c  : catalogo) {
			if (conteo == limite) {
				requerimiento.caracterSeparador=""
			}
			c.values = tramaR.substring(0,c.longitud)
			tramaR   = tramaR.substring(  c.longitud + requerimiento.caracterSeparador.length())
			conteo ++
		}

		//requerimiento.getTipoTransaccion()

		// Generar el Insert segun la configuracion del catalogo.
		String SqlQuery  = "INSERT INTO SWT_Respuesta_Autorizacion"
		String param ="(rsaut_trama,rsaut_movimiento,raut_observacion,rsaut_fecha,IDStatus,SWT_Respuesta_AutorizacionVarchar1,"

		String values =""
		if(requerimiento.getTipoTransaccion().contains("REVERSO")) {
			values ="VALUES ('${this.tramaRespuesta}','${requerimiento.rqaut_movimiento}', '${mensajeRespuesta.mensaje}',GETDATE(), ${Constantes.ESTADO_SWT_Respuesta_Autorizacion_OK},'REVERSO',"
		}else {
			values ="VALUES ('${this.tramaRespuesta}','${requerimiento.rqaut_movimiento}', '${mensajeRespuesta.mensaje}',GETDATE(), ${Constantes.ESTADO_SWT_Respuesta_Autorizacion_OK},'${getEstadoSegunSecuencia(mensajeRespuesta)}',"
		}


		for (Catalogo c  : catalogo) {
			if (!c.tabla.equals("indefinido")) {
				param = param +  " ${c.campo},"
				values =values+  "'${c.values}',"
			}
		}
		param = param.substring(0 , param.length()-1) + ")"
		values = values.substring(0 , values.length()-1) + ")"


		//		if (ocnn == null) {
		//			ocnn =  ConexionSqlServer.getInstance()
		//			ocnn.abrirConexion()
		//		 }else if (ocnn.lConexion.) {
		//
		//		}

		//println "${SqlQuery} ${param} ${values}"
		ocnn.insert("${SqlQuery} ${param} ${values}")

		LogsApp.getInstance().Escribir("Respuesta insertada en BD")
	}

	// Obtiene la longitud de la trama sin tomar en cuenta el caracter separador.
	int  getLongitudTrama () {
		int longitud =0 ;
		for (Catalogo c  : catalogo) {
			longitud += c.longitud  + requerimiento.caracterSeparador.length()
		}
		if(requerimiento.caracterSeparador.length() >0) {
			longitud = longitud - requerimiento.caracterSeparador.length()
		}
		return longitud
	}

	void obtenerMensajesRespuesta () {
		posicionRepsuesta	= Propiedades.get( Constantes.ARCHIVO_CONFIGURACION_DINAMIC,  "posicionTiporespuesta.${requerimiento.getTipoTransaccion()}.${requerimiento.medioAutorizador}").split(",")
		ListadomensajesRespuesta = new ArrayList<MensajesRespuestas>();
		try {
			Object [] params  = [requerimiento.tpenv_id]
			ResultSet odr =	 ocnn.selectSQL(Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC,  "query.obtenerMensajesRespuesta"),params )
			if (odr != null) {
				while (odr.next()) {
					ListadomensajesRespuesta.add( new MensajesRespuestas(odr.getString("codigo"), odr.getString("mensaje"), odr.getString("secuencia")))
				}
				if (!odr.isClosed()) {
					odr.close()
				}
			}
		} catch (Exception e) {
			println  e.getMessage()
		}
	}

	void procesarRespuestaSwitch (String TramaRespuesta) {
		
		cargarCatalogo("RESPUESTA")

		// Obtener los codigos de respuesta configurados de las tramas 00=ok;  01=error; 0? = etc.
		this.tramaRespuesta = TramaRespuesta
		obtenerMensajesRespuesta()

		String codigoRespuesta = this.tramaRespuesta.toString().substring(Integer.parseInt( posicionRepsuesta[0]), Integer.parseInt( posicionRepsuesta[1]))

		mensajeRespuesta =  MensajesRespuestas.getInstancia()
		for (mr in ListadomensajesRespuesta) {
			if (mr.codigo.equals(codigoRespuesta)) {
				mensajeRespuesta = mr
				break
			}
		}
		LogsApp.getInstance().Escribir("[Mensaje de respuesta]  Codigo:  ${mensajeRespuesta.codigo}, Mensaje:  ${mensajeRespuesta.mensaje} ,   Secuencia:  ${mensajeRespuesta.secuencia} ")
		
		LogsApp.getInstance().Escribir("Procesando respuesta obtenida.")
		if (mensajeRespuesta.secuencia.equals("INSERTAR") ||mensajeRespuesta.secuencia.equals("APROBADA") ) {
			println "insertar con normalidad."


			LogsApp.getInstance().Escribir("Entra a funcion Validar trama de respuesta del Switch.")
			//La longitud de la trama de respuesta cumple la longitud de la trama configurada en el catalogo ?
			if (this.getLongitudTrama() == this.tramaRespuesta .toString().length() ) {

				LogsApp.getInstance().Escribir("Trama de respuesta del Switch longitud OK")
				println "Trama: ${this.tramaRespuesta .toString()} "
				//LogsApp.getInstance().Escribir("si cumple longitud - Trama insertada mediante ejecución normal: " +this.tramaRespuesta)
				InsertarTramaRespuestaAutorizacion ()

			}else { // Si no cumplio la longitud
				LogsApp.getInstance().Escribir("Trama de respuesta del Switch longitud ERROR")
				
				// Arreglar longitud de la trama,
				if (requerimiento.caracterSeparador.toString() > 0) {

					LogsApp.getInstance().Escribir("-> Intento de completar trama de respuesta Switch segun catalogo configurado")
					
					if ( completarTramaSegunCatalogo(this.tramaRespuesta.toString()) ) {
						LogsApp.getInstance().Escribir("-> -> Se auto completo la trama Correctamente: ")
						println "Trama: ${this.tramaRespuesta .toString()} "
						InsertarTramaRespuestaAutorizacion ()
					}else {
						
						LogsApp.getInstance().Escribir("No se pudo completar la trama de respuesta Switch segun catalogo configurado")
						
					 
						InsertarTramaRespuestaAutorizacionERROR ()
					}

				} else {
					// Inserta Transaccion Error
					println "Trama: ${this.tramaRespuesta .toString()} "
					LogsApp.getInstance().Escribir("Se inserto trama (Con longitud incorrecta obtendia por el switch): ${this.tramaRespuesta}")
					InsertarTramaRespuestaAutorizacionERROR ()
				}
			}
		}else {
			LogsApp.getInstance().Escribir("Ejecucion recursiva iniciada.")
			ejecutar(
					mensajeRespuesta.secuencia
					, codigoRespuesta
					, Integer.parseInt( Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, Constantes.TRANSACCION_INTENTOS))
					)
			LogsApp.getInstance().Escribir("Ejecucion recursiva terminada.")
		}
	}

	void ejecutar(String secuencia, String codigoRespuesta , int intentos ) {

		boolean TIME_OUT = false

		secuencia =	requerimiento.asignarValoresSecuencia(secuencia)

		if ( (secuencia.equals("INSERTAR") || mensajeRespuesta.secuencia.equals("APROBADA") ) || intentos ==0) {

			println "Insert normal Recursivo"

			if (this.getLongitudTrama() == this.tramaRespuesta.toString().length() ) {

				//	LogsApp.getInstance().Escribir("RC si cumple longitud - Trama insertada mediante ejecución normal: " +this.tramaRespuesta)
				InsertarTramaRespuestaAutorizacion ()

			}else {

				if (requerimiento.caracterSeparador.toString() > 0) {
					if ( completarTramaSegunCatalogo(this.tramaRespuesta.toString())) {
						LogsApp.getInstance().Escribir("RC - Se auto completo la trama: " +this.tramaRespuesta)
						println "Trama: ${this.tramaRespuesta .toString()} "
						InsertarTramaRespuestaAutorizacion ()
					}else {
						InsertarTramaRespuestaAutorizacionERROR()
					}


				}
				else {
					// inserta Transaccion Errror.
					println "Trama: ${this.tramaRespuesta .toString()} "
					LogsApp.getInstance().Escribir("RC Se inserto trama (error controlado): ${this.tramaRespuesta}")
					InsertarTramaRespuestaAutorizacionERROR ()
				}

			}

		}else {
			LogsApp.getInstance().Escribir("Reintentando envio de transaccion:  intentos restantes: ${intentos}  Secuencia -> ${secuencia}")

			String jar =Propiedades.get(Constantes.ARCHIVO_CONFIGURACION_DINAMIC, "jar.ruta")
			JarLector j = JarLector.getInstancia(jar.split("->"))

			Respuesta_Autorizacion res = new  Respuesta_Autorizacion()
			res  = this


			String NextramaRespuesta =""

			final Duration timeout = Duration.ofSeconds( requerimiento.getTimeOut() )
			ExecutorService executor = Executors.newSingleThreadExecutor()

			final Future<String> handler = executor.submit(new Callable() {
						@Override
						public String call() throws Exception {
							return j.executeMetodoSecuencia(secuencia ,  res)
						}
					})
			try {
				NextramaRespuesta=handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
			} catch (TimeoutException e) {
				println e.getMessage()
				TIME_OUT = true
				handler.cancel(true)
			}
			executor.shutdown()



			if (!TIME_OUT) {


				String NetxcodigoRespuesta = NextramaRespuesta.toString().substring(Integer.parseInt( posicionRepsuesta[0]), Integer.parseInt( posicionRepsuesta[1]))
				LogsApp.getInstance().Escribir("Nueva Trama respuesta: " + NextramaRespuesta)


				// actualizar la trama  a la nueva obtenida.
				this.tramaRespuesta = NextramaRespuesta
				mensajeRespuesta =     MensajesRespuestas.getInstancia()
				for (mr in ListadomensajesRespuesta) {
					if (mr.codigo.equals(NetxcodigoRespuesta)) {
						mensajeRespuesta = mr
						break
					}
				}

				LogsApp.getInstance().Escribir("[Mensaje de respuesta RC]  Codigo:  ${mensajeRespuesta.codigo}, Mensaje:  ${mensajeRespuesta.mensaje} ,   Secuencia:  ${mensajeRespuesta.secuencia} ")


				// Si el codigo resultante es igual al anterior quiere decir que esta cayendo en un bucle de la misma respusta
				// entonces se aumentara el numero de reintentos para que no sea algo infinito.
				if (mensajeRespuesta.codigo.equals(codigoRespuesta)) {
					intentos = intentos- 1
				}
				ejecutar(mensajeRespuesta.secuencia ,NetxcodigoRespuesta ,intentos )




			}else {
				insetarBugTIME_OUT("TIME OUT");
				LogsApp.getInstance().Escribir("Se obtubo un TIME_OUT esperando respuesta")
			}



		}


	}


}
