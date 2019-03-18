package kfc.com.modelo

class Catalogo {

	def nombre_campo
	def posicion
	def longitud
	def tipo_dato
	def caracter_relleno
	def orientacion_relleno
	def tabla
	def campo
	// Valor asignado  a esa configuracion.
	def values
	
	
	

	public Catalogo(Object nombre_campo, Object posicion, Object longitud, Object tipo_dato, Object caracter_relleno,
			Object orientacion_relleno, Object tabla, Object campo) {
		super();
		this.nombre_campo = nombre_campo;
		this.posicion = posicion;
		this.longitud = longitud;
		this.tipo_dato = tipo_dato;
		this.caracter_relleno = caracter_relleno;
		this.orientacion_relleno = orientacion_relleno;
		this.tabla = tabla;
		this.campo = campo;
	}
	Catalogo () {
	}
	Catalogo (def nombre_campo , def posicion , def tipo_dato , def longitud){
		this.nombre_campo = nombre_campo
		this.posicion = posicion
		this.tipo_dato = tipo_dato
		this.longitud = longitud
	}
}
