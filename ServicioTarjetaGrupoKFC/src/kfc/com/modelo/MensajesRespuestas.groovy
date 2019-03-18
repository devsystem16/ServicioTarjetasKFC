package kfc.com.modelo



class MensajesRespuestas {
	String codigo
	String mensaje
	String secuencia
	
	public MensajesRespuestas () {
		this.codigo = "ER"
		this.mensaje  ="Mensaje no especificado."
		this.secuencia ="INSERTAR"
		
	}
	public MensajesRespuestas(String codigo, String mensaje, String secuencia) {
		super();
		this.codigo = codigo;
		this.mensaje = mensaje;
		this.secuencia = secuencia;
	}

	private static MensajesRespuestas instance

	static  MensajesRespuestas getInstancia( ){
		if (instance == null)
			instance = new MensajesRespuestas ( )
		instance
	}
}
