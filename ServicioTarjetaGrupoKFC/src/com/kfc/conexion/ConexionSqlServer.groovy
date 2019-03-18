package com.kfc.conexion
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import kfc.com.modelo.ArchivoProperties
import kfc.com.modelo.Constantes
import kfc.com.modelo.LogsApp
import kfc.com.modelo.Propiedades

class ConexionSqlServer {

	Connection lConexion
	String urlConexion
	String usuario
	String clave
	String driverClass
	PreparedStatement prepareS = null;
	static ConexionSqlServer instance = null ;


	static ConexionSqlServer getInstance () {
		return  (instance != null) ? ConexionSqlServer : new ConexionSqlServer ();
	}

	Connection obtenerConexion()throws Throwable {
		if (lConexion == null || lConexion.isClosed() ) {
			abrirConexion()
		}
		lConexion
	}

	void abrirConexion() throws Throwable {
		try {

			urlConexion = "jdbc:sqlserver:"+Propiedades.get(Constantes.ARCHIVO_APPLICATION_STATIC,  "conexion.servidor")+";databaseName="+Propiedades.get(Constantes.ARCHIVO_APPLICATION_STATIC,  "conexion.base_datos")
			usuario=Propiedades.get(Constantes.ARCHIVO_APPLICATION_STATIC,  "conexion.usuario")
			clave =Propiedades.get(Constantes.ARCHIVO_APPLICATION_STATIC,  "conexion.passw")
			lConexion = DriverManager.getConnection(urlConexion + ";user=" +usuario+ ";password="+clave+";" )
		 
			
		} catch (Exception e) {
			println e.getMessage()
			LogsApp.getInstance().Escribir(e.getMessage())
			 
		}
		return
	}

	void cerrarConexion() throws Throwable {
		if  (lConexion!= null)
			lConexion.close()
	}

	ResultSet select (String sqlQuery ) {
		ResultSet odr  = null
		prepareS =	lConexion.prepareStatement(sqlQuery)
		prepareS.executeQuery()
	}

	int insert (String sqlQuery ) {
		try {
			Statement statement = lConexion.createStatement();
			return statement.executeUpdate(sqlQuery)
		} catch (Exception e) {
			return -1
		}
	}


	int update (String sqlQuery , Object [] parametros) {
		prepareS =	lConexion.prepareStatement(sqlQuery)
		if (parametros != null) {
			int length = parametros.length -1
			0.upto(length){
				prepareS.setObject( (it+1) ,parametros[it])
			}
		}
		prepareS.executeUpdate()
	}

	ResultSet select (String sqlQuery,  Object [] parametros) {
		ResultSet odr  = null
		prepareS =	lConexion.prepareStatement(sqlQuery)
		if (parametros != null) {

			int length = parametros.length -1
			0.upto(length){

				prepareS.setString( (it+1) ,parametros[it])
			}
		}

		prepareS.executeQuery()
	}

	ResultSet selectSQL (String StoreProcedure , Object [] parametros ) {
		if  (!(StoreProcedure.contains("EXEC") || StoreProcedure.contains("exec"))) {
			select (StoreProcedure , parametros)
		}
		else {
			ResultSet odr  = null
			CallableStatement cst  = lConexion.prepareCall( StoreProcedure)
			if (parametros != null) {
				int length = parametros.length -1
				0.upto(length){
					cst.setObject( (it+1) ,parametros[it])
				}
			}
			odr = cst.executeQuery()
			return odr
		}
	}

	ResultSet selectSQL (String StoreProcedure ) {

		if  (!(StoreProcedure.contains("EXEC") || StoreProcedure.contains("exec"))) {
			return 	select (StoreProcedure  )
		}else {
			ResultSet odr  = null
			CallableStatement cst  = lConexion.prepareCall( StoreProcedure)
			odr = cst.executeQuery()
			return odr
		}
	}
}
