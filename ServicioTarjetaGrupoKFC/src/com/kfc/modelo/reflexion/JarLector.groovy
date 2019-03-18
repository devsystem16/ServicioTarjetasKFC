package com.kfc.modelo.reflexion

import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.net.URLClassLoader
import java.lang.reflect.InvocationTargetException

import java.lang.Class
import java.lang.reflect.ReflectPermission
import java.lang.reflect.Type

import com.kfc.conexion.ConexionSqlServer
import com.sun.org.apache.bcel.internal.generic.LoadClass
import com.sun.org.apache.xml.internal.utils.URI

import kfc.com.modelo.Catalogo
import kfc.com.modelo.Constantes
import kfc.com.modelo.LogsApp
import kfc.com.modelo.RequerimientoAutorizacion
import kfc.com.modelo.Respuesta_Autorizacion
import java.lang.reflect.Constructor
import java.lang.reflect.Field;
import javax.print.attribute.standard.PrinterLocation
import javax.sound.sampled.spi.FormatConversionProvider
import java.net.URL
import java.security.CodeSource
import java.sql.Connection
import java.sql.DriverManager
import java.util.ArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class JarLector {


	String path    // Ruta de ubicacion del .jar
	File jar
	URLClassLoader cl
	Class beanClass
	Object objClase
	Method metodo
	ConexionSqlServer ocnn

	private static JarLector Singelton

	// Constructor
	JarLector (String Path) {
		this.path = Path
		jar =  new File(Path)
		cl = new URLClassLoader (new URL("jar", "","file:" + jar.getAbsolutePath()+"!/"))

	}
	JarLector ( ) {
		cl = new URLClassLoader()
	}

	JarLector (String [] Paths) {

		int tamanio = Paths.length
		File url = null
		cl = new URLClassLoader()
		for(int i=0 ; i < tamanio; i++) {
			url = new File(Paths[i].trim());
			cl.addURL(new URL("jar", "","file:" + url.getAbsolutePath()+"!/"))
		}
	}

	// Singelton
	static  JarLector getInstancia(){
		if (Singelton == null)
			Singelton = new JarLector ()
		Singelton
	}

	// Singelton
	static  JarLector getInstancia(String [] paths){
		if (Singelton == null)
			Singelton = new JarLector (paths)
		Singelton
	}

	// Singelton
	static  JarLector getInstancia(String path){
		if (Singelton == null)
			Singelton = new JarLector (path)
		Singelton
	}

	public  String   buscarPaquete  (String claseBuscar) {
		String paquete =claseBuscar
		try {
			if (!claseBuscar.contains(".")) {

				boolean encontro = false
				URL  [] rutas = cl.getURLs()
				for(int i=0 ; i< rutas.length;i++) {

					String location =  rutas[i].path
					location = location.substring(0 ,location.length() -2 )

					URL jarUrl = new URL(location)
					ZipInputStream zip = new ZipInputStream(jarUrl.openStream());
					ZipEntry ze = null;
					while((ze = zip.getNextEntry()) != null){
						String RutaClase = ze.getName();
						if (RutaClase.contains(".class") || RutaClase.contains(".java")) {
							String NombreClase = RutaClase.substring(RutaClase.lastIndexOf("/") +1, RutaClase.lastIndexOf("."))
							if (claseBuscar.equals(NombreClase)) {
								paquete = RutaClase.replaceAll("/", ".").substring(0 , RutaClase.lastIndexOf("."))
								encontro = true
								break
							}
						}
					}

					if (encontro) {
						break
					}

				} // fin de for urls

			} // Fin pregunta si tiene .
		} catch (Exception e) {
			LogsApp.getInstance().Escribir(e.getMessage())
			println e.getMessage()
			e.printStackTrace()
		}
		paquete
	}

	public Object obtieneClase (  String paquete_clase){
		Object beanClass = cl.loadClass (paquete_clase);
		return beanClass
	}

	public  Object  ejecutaMetodoInstanciado (  Object   objetoClase , Object [] paramsConstruct ,  String metodoEjecutar, Object [] ParametrosMetodo  ){

		int numeroParametros = ParametrosMetodo.length


		if (ParametrosMetodo.length == 1) {
			if (ParametrosMetodo [0].equals("") )
			{
				numeroParametros = 0
			} else {
				numeroParametros =ParametrosMetodo.length
			}
		}else {
			numeroParametros =ParametrosMetodo.length
		}


		Class beanClass =  objetoClase.getClass()


		Object respuesta
		Object objClase = objetoClase

		Object [] resultados = 	obtenerVectorTipadoMetodos(  beanClass, ParametrosMetodo)
		Method metodo = null;
		//Method[] metodos = beanClass.getMethods()

		Method[] metodos = beanClass.getDeclaredMethods()

		// Ubicar el metodo.
		int length = metodos.length
		for (int i = 0; i <length; i++) {
			if (metodos[i].getName().equals(metodoEjecutar) && metodos[i].getParameterCount() == numeroParametros ) {
				metodo = metodos[i]
				break
			}
		}




		try {

			if  (resultados.length == 1) {
				if (resultados[0] == "") {
					respuesta =	metodo.invoke( objClase, null)
				}else {
					respuesta =	 metodo.invoke( objClase, resultados)
				}
			}else {
				respuesta =	metodo.invoke( objClase, resultados)
			}

		}  catch (java.io.IOException e) {// InvocationTargetException e) {
			respuesta =respuesta  + "*****Exception  " + e.getMessage() // e.getTargetException().getMessage() + " Catch"
			LogsApp.getInstance().Escribir(respuesta)
		}


		return respuesta



	}
	public  Object  ejecutaMetodo ( Class beanClass , Object [] paramsConstruct ,  String metodoEjecutar, Object [] ParametrosMetodo   ){
		Object respuesta
		Object objClase

		if  (paramsConstruct.length == 1) {
			if (paramsConstruct[0] == "") {
				objClase = beanClass.newInstance()
			}else {

				Object [] parametrosTipados = obtenerVectorTipadoConstructor (beanClass, paramsConstruct)
				objClase = beanClass.newInstance(parametrosTipados)
				//				objClase = beanClass.newInstance(paramsConstruct[0])
			}
		}else {
			Object [] parametrosTipados = obtenerVectorTipadoConstructor (beanClass, paramsConstruct)
			objClase = beanClass.newInstance(parametrosTipados)
			//			objClase = beanClass.newInstance(paramsConstruct)
		}



		int numeroParametros = (ParametrosMetodo[0].equals("")) ? 0: ParametrosMetodo.length

		Method metodo = null;
		//Method[] metodos = beanClass.getMethods()

		Method[] metodos = beanClass.getDeclaredMethods()

		// Ubicar el metodo.
		int length = metodos.length
		for (int i = 0; i <length; i++) {
			if (metodos[i].getName().equals(metodoEjecutar) && metodos[i].getParameterCount() == numeroParametros ) {
				metodo = metodos[i]
				break
			}
		}


		try {

			Object [] resultados =  obtenerVectorTipadoMetodos(beanClass, ParametrosMetodo , metodoEjecutar)// new Object [numeroParametros]


			if  (resultados.length == 1) {
				if (resultados[0] == "") {
					respuesta =	metodo.invoke( objClase, null)

				}else {

					respuesta =	 metodo.invoke( objClase, resultados)
				}
			}else {

				respuesta =	metodo.invoke( objClase, resultados)
			}

		}  catch (java.io.IOException e) {// InvocationTargetException e) {
			println e.getMessage()
			respuesta = "" // e.getTargetException().getMessage() + " Catch"
			LogsApp.getInstance().Escribir( "Exception : " +  e.getMessage())
		}


		return respuesta

	}


	Object [] obtenerVectorTipadoMetodos (Class beanClass, Object [] ParametrosMetodo, String metodoEjecutar) {

		int numeroParametros = ParametrosMetodo.length


		if (ParametrosMetodo.length == 1) {
			if (ParametrosMetodo [0].equals("") )
			{
				numeroParametros = 0
			} else {
				numeroParametros =ParametrosMetodo.length
			}
		}else {
			numeroParametros =ParametrosMetodo.length
		}

		Method metodo
		Method [] metodos = beanClass.getDeclaredMethods()

		for (c in metodos) {

			if (c.getParameterCount() ==numeroParametros && c.getName().equals(metodoEjecutar) ) {
				metodo = c
				break
			}
		}




		Object [] resultados = new Object [ParametrosMetodo.length]
		Class [] parametrosMetodo = metodo.getParameterTypes()

		if (numeroParametros > 0 ) {

			int c = 0
			for (Class p : parametrosMetodo) {

				String tipo  =	p.getSimpleName()
				switch (tipo){
					case  "int":
						resultados [c] =  Integer.parseInt(ParametrosMetodo[c])
						break

					case  "String":
						resultados [c] = ( ParametrosMetodo[c].toString() =="null" ) ? null  : ParametrosMetodo[c].toString()
						break

					case  "CString":
						resultados [c] =  ( ParametrosMetodo[c].toString() =="null" ) ? null  : ParametrosMetodo[c].toString()
						break

					case  "Integer":
						resultados [c] =  ( ParametrosMetodo[c].toString() =="null" ) ? null  :Integer.parseInt(ParametrosMetodo[c])
						break

					case  "Date":
						resultados [c] =( ParametrosMetodo[c].toString() =="null" ) ? null  :  ParametrosMetodo[c].toString()
						break

					case  "Object[]":
						String a =   ParametrosMetodo[c]

						if (a != null) {
							if (a !="") {
								resultados[c] = a.split(">")
							}else {
								resultados[c] = null
							}
						}else {
							resultados[c] = null
						}

						break

					case  "[LObject;":
					// String a =   ParametrosMetodo[c]
					// resultados[c] = a.split(">")
						String a =   ParametrosMetodo[c]
						if (a != null) {
							if (a !="") {
								resultados[c] = a.split(">")
							}else {
								resultados[c] = null
							}
						}else {
							resultados[c] = null
						}

						break

					case  "Object":
						resultados [c] =  (  ParametrosMetodo[c].toString() ==  "null" ) ? null : ParametrosMetodo[c]
						break
					case  "byte[]":
						resultados [c] =  (  ParametrosMetodo[c].toString() ==  "null" )  ? null:  ParametrosMetodo[c].toString().getBytes()
						break

					default :
						resultados [c] = (  ParametrosMetodo[c].toString() ==  "null" )  ? null:  ParametrosMetodo[c]
						break
				}
				c++
			}
		}
		if (resultados.length == 1) {
			if (resultados[0]==  null) {
				resultados  [0] = ""
			}
		}


		return resultados
	}


	Object [] obtenerVectorTipadoConstructor (Class beanClass, Object [] ParametrosConstructor) {

		//		int numeroParametros = ParametrosConstructor.length
		//
		//		if (ParametrosConstructor.length == 1) {
		//			if (ParametrosConstructor[0] =="") {
		//				numeroParametros =0 ;
		//			}
		//		}
		int numeroParametros = ParametrosConstructor.length


		if (ParametrosConstructor.length == 1) {
			if (ParametrosConstructor [0].equals("") )
			{
				numeroParametros = 0
			} else {
				numeroParametros =ParametrosConstructor.length
			}
		}else {
			numeroParametros =ParametrosConstructor.length
		}


		Constructor constructor
		Constructor []  constructores  = beanClass.getConstructors()

		for (c in constructores) {

			if (c.getParameterCount() ==numeroParametros ) {
				constructor = c
				break
			}
		}


		Object [] resultados = new Object [numeroParametros]


		if (numeroParametros > 0 ) {


			Class [] parametrosMetodo = constructor.getParameterTypes()
			int c = 0
			for (Class p : parametrosMetodo) {

				String tipo  =	p.getSimpleName()
				switch (tipo){
					case  "int":
						resultados [c] =  Integer.parseInt(ParametrosConstructor[c])
						break

					case  "String":
						resultados [c] = ( ParametrosConstructor[c].toString() =="null" ) ? null  : ParametrosConstructor[c].toString()
						break

					case  "CString":
						resultados [c] =  ( ParametrosConstructor[c].toString() =="null" ) ? null  : ParametrosConstructor[c].toString()
						break

					case  "Integer":
						resultados [c] =  ( ParametrosConstructor[c].toString() =="null" ) ? null  :Integer.parseInt(ParametrosConstructor[c])
						break

					case  "Date":
						resultados [c] =( ParametrosConstructor[c].toString() =="null" ) ? null  :  ParametrosConstructor[c].toString()
						break

					case  "Object[]":
						String a =   ParametrosConstructor[c]

						if (a != null) {
							if (a !="") {
								resultados[c] = a.split(">")
							}else {
								resultados[c] = null
							}
						}else {
							resultados[c] = null
						}

						break

					case  "[LObject;":
					// String a =   ParametrosMetodo[c]
					// resultados[c] = a.split(">")
						String a =   ParametrosConstructor[c]
						if (a != null) {
							if (a !="") {
								resultados[c] = a.split(">")
							}else {
								resultados[c] = null
							}
						}else {
							resultados[c] = null
						}

						break

					case  "Object":
						resultados [c] =  (  ParametrosConstructor[c].toString() ==  "null" ) ? null : ParametrosConstructor[c]
						break
					case  "byte[]":
						resultados [c] =  (  ParametrosConstructor[c].toString() ==  "null" )  ? null:  ParametrosConstructor[c].toString().getBytes()
						break

					default :
						resultados [c] = (  ParametrosConstructor[c].toString() ==  "null" )  ? null:  ParametrosConstructor[c]
						break
				}
				c++
			}
		}
 
		return resultados
	}

	Object executeMetodoSecuencia (String lineas, Respuesta_Autorizacion respuesta) {

		 //Thread.sleep(3000)
		boolean ClaseInstanciada = false
		Object mensajeRespuesta ;

		Object  [] secuencia = lineas.split(Constantes.SEPARADOR_PROPERTIES)
		Object  [] colaSecuencia  = new Object [secuencia.length]
		for (int i =0 ; i< secuencia.length; i++) {


			String metodo = secuencia[i].toString().substring(0,secuencia[i].toString().indexOf("(")).trim()
			String contenido =secuencia[i].toString().substring(secuencia[i].toString().indexOf("(") +1, secuencia[i].toString().length() -1 )


			switch (metodo) {
				case "obtieneClaseNI" :

					String parametrosClase = contenido.toString().substring(contenido.indexOf("[")+1 ,contenido.toString().length() -1 )
					Object  [] parametros  = parametrosClase.split(">")

					contenido = contenido.substring(0 , contenido.indexOf("["))
					Class beanClass = cl.loadClass (buscarPaquete(contenido.toString()))  // obtieneClase(cl, contenido)

					Object [] ParametrosConClase = new Object [parametros.length]


					for (int  j=0; j< parametros.length ; j++) {
						if (parametros[j].toString().contains("[")) {
							int posc =  Integer.parseInt( parametros[j].toString().replace("[", "").replace("]","") ) //.replaceAll("[\\[\\]]", ""))
							ParametrosConClase [j] =colaSecuencia[posc]
						}
						else {
							ParametrosConClase [j] = parametros [j]
						}
					}


					Object [] parametrosTipados = obtenerVectorTipadoConstructor (beanClass ,ParametrosConClase )
					Object	objClase
					if (parametrosTipados.length == 1 && parametrosTipados[0]== "") {
						objClase  = beanClass.newInstance()
					}else {
						objClase  =	beanClass.newInstance(parametrosTipados)
					}


					colaSecuencia[i] = objClase

					ClaseInstanciada = true // Indica que la clase ya se instancio

					break


				case "obtieneClase" :

					try {
						Object objClase = cl.loadClass (buscarPaquete(contenido.toString()))  // obtieneClase(cl, contenido)
						colaSecuencia[i] = objClase
						ClaseInstanciada = false// Indica que la clase  aun no se instancia y lo hará dentro del metodo ejecuta.
					} catch (Exception e) {
						LogsApp.getInstance().Escribir("No se pudo encontrar la Clase ${contenido} " + e.getMessage())
						e.printStackTrace()
					}

					break




				case "esperar" :


					try
					{
						int sTiempo = Integer.parseInt(contenido)
						Thread.sleep(sTiempo);
					}catch(Exception e){

					}

					break

				case "asignaAtributo":

					Object [] ConfigAsignaAtributo= contenido.split(">")
					int  posicionElemento = -1
					Object objClaseTemp = null

					if (ConfigAsignaAtributo[0].toString().contains("obtieneClase")) {
						String cadena = ConfigAsignaAtributo[0].toString()
						String contenidoTem =cadena.toString().substring(cadena.toString().indexOf("(") +1, cadena.toString().length() -1 ).trim()
						objClaseTemp =cl.loadClass (contenidoTem.toString())
						ClaseInstanciada = false // new
					}else {
						posicionElemento =  Integer.parseInt( ConfigAsignaAtributo[0].toString().replace("[", "").replace("]","") ) // .replaceAll("[\\[\\]]", ""))
					}

					Object elemento = null
					if (posicionElemento == -1) {
						elemento = objClaseTemp;
					}else {
						elemento = colaSecuencia[posicionElemento]
					}


					Object [] atributosSet = ConfigAsignaAtributo[1].split(",")

					respuesta.cargarCatalogo("ENVIO")


					String data =""
					String valor =""
					for (atributo in atributosSet) {
						for (cat in respuesta.catalogo) {

							if (atributo.toString().contains("|")) {

								data =  atributo.toString().substring(0, atributo.toString().indexOf("|"))
								valor =  atributo.toString().substring( atributo.toString().indexOf("|")+1,  atributo.toString().length() )

								if (data.equals(cat.nombre_campo)) {
									setAtributo(elemento ,cat.nombre_campo , valor.toString()  )

									break
								}

							}else {

								if (atributo.toString().equals(cat.nombre_campo)) {
									setAtributo(elemento ,cat.nombre_campo , atributo.toString() )
									break
								}
							}


						}
					}



					colaSecuencia[i] = elemento

					break

				case "ejecutaMetodo" :

					Object [] ConfigEjecutaMetodo = contenido.split(">")

					int  posicionElemento = -1
					Object objClaseTemp = null
					if (ConfigEjecutaMetodo[0].toString().contains("obtieneClase")) {
						String cadena = ConfigEjecutaMetodo[0].toString()
						String contenidoTem =cadena.toString().substring(cadena.toString().indexOf("(") +1, cadena.toString().length() -1 ).trim()
						objClaseTemp =cl.loadClass (contenidoTem.toString()) // obtieneClase(cl, contenidoTem)
						ClaseInstanciada = false // new
					}else {
						// 1) Posicion de referencia anterior.
						posicionElemento =  Integer.parseInt( ConfigEjecutaMetodo[0].toString().replace("[", "").replace("]","") ) // .replaceAll("[\\[\\]]", ""))
					}

				//2) Parametros del contructor.
					Object [] ParametrosConstruc ;
					if (ConfigEjecutaMetodo[1].toString().equals("[]")) {
						ParametrosConstruc =  [""]
					}else {
						String parametrosSinCorchetes = ConfigEjecutaMetodo[1].toString().trim()
						parametrosSinCorchetes = parametrosSinCorchetes.substring(1 , parametrosSinCorchetes.length()-1)
						Object [] vectorParame = parametrosSinCorchetes.split("&")

						ParametrosConstruc = new Object [vectorParame.length]
						for (int j =0 ; j<vectorParame.length; j++ ) {
							if (vectorParame[j].toString().contains("[")) {
								int posc =  Integer.parseInt( vectorParame[j].toString().replace("[", "").replace("]","") ) //.replaceAll("[\\[\\]]", ""))
								ParametrosConstruc [j] =colaSecuencia[posc]
							}else {
								// Validar tipo de dato.
								String dato = vectorParame[j]
								if (dato.toString().contains("*")) {
									ParametrosConstruc [j] =  ( vectorParame[j].toString().replace("*", "")).toString()
								}else {
									ParametrosConstruc [j] = Integer.parseInt(  ( vectorParame[j].toString().replace("*", "")))
								}
								// boolean?
							}
						}
					}
				//3) Metodo a invocar
					String  metodoInvocar = ConfigEjecutaMetodo[2].toString().trim()

				//4) Parametros del Metodo.
					Object [] ParametrosMethod ;
					if (ConfigEjecutaMetodo[3].toString().equals("[]")) {
						ParametrosMethod =  [""]
					}else {

						String parametrosSinCorchetes = ConfigEjecutaMetodo[3].toString().trim()
						parametrosSinCorchetes = parametrosSinCorchetes.substring(1 , parametrosSinCorchetes.length()-1)
						Object [] vectorParame = parametrosSinCorchetes.split("&")

						ParametrosMethod = new Object [vectorParame.length]
						for (int j =0 ; j<vectorParame.length; j++ ) {
							if (vectorParame[j].toString().contains("[")) {
								int posc =  Integer.parseInt( vectorParame[j].toString().replace("[", "").replace("]","") ) //.replaceAll("[\\[\\]]", ""))
								ParametrosMethod [j] =colaSecuencia[posc]
							}else {

								String dato = vectorParame[j]
								if (dato.toString().contains("*")) {
									ParametrosMethod [j] =  ( vectorParame[j].toString().replace("*", "")).toString()
								}else {
									ParametrosMethod [j] =   vectorParame[j]
								}

								//ParametrosMethod = vectorParame;
							}
						}
						//pendiente.
						//ParametrosMethod =params
					}

					Object elemento = null
					if (posicionElemento == -1) {
						elemento = objClaseTemp;
					}else {
						elemento = colaSecuencia[posicionElemento]
					}


					try {

						if (ClaseInstanciada) {
							mensajeRespuesta= ejecutaMetodoInstanciado(elemento , ParametrosConstruc, metodoInvocar,  ParametrosMethod)
						}else {
							
							mensajeRespuesta= ejecutaMetodo(elemento , ParametrosConstruc, metodoInvocar,  ParametrosMethod)
						}

					}
					catch ( InvocationTargetException er) {
						  
					 
						mensajeRespuesta=""
					}

					colaSecuencia[i] =mensajeRespuesta

					break
				case "creaTrama":
					int  posicionElemento = -1
					Object [] ConfigCreaTrama= contenido.split(">")
					posicionElemento =  Integer.parseInt( ConfigCreaTrama[0].toString().replace("[", "").replace("]","") )
					Object objClaseTemp = null

					Object elemento = null
					if (posicionElemento == -1) {
						elemento = objClaseTemp;
					}else {
						elemento = colaSecuencia[posicionElemento]
					}


					respuesta.cargarCatalogo("RESPUESTA")

					String caracterSep =  (respuesta.requerimiento.caracterSeparador.toString().length() == 0 )?  "->" : respuesta.requerimiento.caracterSeparador

					respuesta.requerimiento.caracterSeparador  = caracterSep
					respuesta.respuestatipoObjeto = true
					String trama  ="";
					for (cat in respuesta.catalogo) {
						if (trama.equals("")) {
							trama  = trama + "${GetAtributo(elemento ,cat.nombre_campo  )}"
						}else {
							trama  = trama + "${caracterSep}${GetAtributo(elemento ,cat.nombre_campo  )}"
						}

					}

					mensajeRespuesta = trama
					break


				default :
					LogsApp.getInstance().Escribir("Secuencia [${metodo}] no existe.")
					System.exit(1)
					break

			}


		}
		return mensajeRespuesta//.toString();
	}

	static public Object convertirTipoDato (String tipo, Object Value) {

		switch (tipo){
			case  "int":
				return  (Value.toString() =="null" ) ? null  :Integer.parseInt(Value)
				break

			case  "String":
				return     (Value.toString() =="null" ) ? null  :Value.toString()
				break

			case  "CString":
				return  (Value.toString() =="null" ) ? null  :Value.toString()

				break

			case  "Integer":
				return  (Value.toString() =="null" ) ? null  :Integer.parseInt(Value)

				break

			case  "Date":
				return     (Value.toString() =="null" ) ? null  :Value.toString()
				break

			case  "Object":
				return   ( Value.toString() ==  "null" ) ? null :Value
				break
			case  "byte[]":
				return   (Value.toString() ==  "null" )  ? null:  Value.toString().getBytes()
				break

			default :
				return  ( Value.toString() ==  "null" )  ? null:Value
				break
		}
	}
	public static boolean setAtributo(Object Objeto, String nombreAtributo, Object valorNuevo) {
		Class<?> clazz = Objeto.getClass();
		while (clazz != null) {
			try {
				Field field = clazz.getDeclaredField(nombreAtributo);
				field.setAccessible(true);
				Object tipoDAto  = convertirTipoDato (field.getType().getSimpleName(), valorNuevo)
				field.set(Objeto, tipoDAto);
				return true;
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return false;
	}

	public static Object GetAtributo(Object Objeto, String nombreAtributo) {
		Class<?> clazz = Objeto.getClass();
		while (clazz != null) {
			try {
				Field field = clazz.getDeclaredField(nombreAtributo);
				field.setAccessible(true);
				Object attr = field.get(Objeto)

				return attr
			} catch (NoSuchFieldException e) {
				//				clazz = clazz.getSuperclass();
				return  ""
			} catch (Exception e) {
				return  ""
				//				throw new IllegalStateException(e);
				println new IllegalStateException(e).getMessage()
			}
		}
		return false;
	}

	// Metodo invocador.
	String  invocarMetodo (String Clase , String Metodo, Object [] paramsConstruct, Object [] ParametrosMetodo , int estado) {

		String respuesta =""
		estado =0

		beanClass = cl.loadClass (buscarPaquete( Clase))

		if  (paramsConstruct.length == 1) {
			if (paramsConstruct[0] == "") {
				objClase = beanClass.newInstance()

			}else {
				objClase = beanClass.newInstance(paramsConstruct[0])
			}
		}else {
			objClase = beanClass.newInstance(paramsConstruct)
		}

		int numeroParametros = (ParametrosMetodo[0].equals("")) ? 0: ParametrosMetodo.length



		Method metodo = null;
		//Method[] metodos = beanClass.getMethods()

		Method[] metodos = beanClass.getDeclaredMethods()



		// Ubicar el metodo.
		int length = metodos.length
		for (int i = 0; i <length; i++) {
			if (metodos[i].getName().equals(Metodo) && metodos[i].getParameterCount() == numeroParametros ) {
				metodo = metodos[i]
				break
			}
		}


		try {

			Object [] resultados = new Object [numeroParametros]
			Class [] parametrosMetodo = metodo.getParameterTypes()

			if (numeroParametros > 0 ) {

				int c = 0
				for (Class p : parametrosMetodo) {

					String tipo  =	p.getSimpleName()
					switch (tipo){
						case  "int":
							resultados [c] =   Integer.parseInt(ParametrosMetodo[c])
							break

						case  "String":
							resultados [c] =  ParametrosMetodo[c].toString()
							break

						case  "Integer":
							resultados [c] =  Integer.parseInt(ParametrosMetodo[c])
							break


						case  "Date":
							resultados [c] =ParametrosMetodo[c].toString()
							break


						case  "Object[]":
							String a =   ParametrosMetodo[c]
							resultados[c] = a.split(">")
							break

						case  "[LObject;":
							String a =   ParametrosMetodo[c]
							resultados[c] = a.split(">")
							break

						case  "Object":
							resultados [c] =   ParametrosMetodo[c]
							break

						default :
							resultados [c] =  ParametrosMetodo[c].toString()
							break

					}
					c++
				}
			}


			if  (resultados.length == 1) {
				if (resultados[0] == "") {
					respuesta =	metodo.invoke( objClase, null)

				}else {
					respuesta =	 metodo.invoke( objClase, resultados)
				}
			}else {
				respuesta =	metodo.invoke( objClase, resultados)
			}
			estado =1

		} catch (InvocationTargetException e) {
			respuesta = "Error Response Switch: ${e.getTargetException().getMessage()}"
			estado =0
		}

		return respuesta
	}



}
