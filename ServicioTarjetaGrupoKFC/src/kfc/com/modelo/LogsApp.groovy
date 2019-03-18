package kfc.com.modelo

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties



class LogsApp {


	static Date fechalocal;
	static SimpleDateFormat formatter;

	static String FileName="";
	static BufferedWriter out = null;
	static String SFecha="";
	static String Ruta="";
	static String Carpeta="";

	static public LogsApp instance
	private LogsApp () {}

	public static  LogsApp getInstance () {
		if (instance == null)
			instance = new LogsApp()
		instance
	}

	public static void Escribir(String ps_cadena) throws IOException{
		fechalocal = new Date();
		formatter = new SimpleDateFormat("ddMMyyyy");
		SFecha=formatter.format(fechalocal);


		Ruta= Constantes.RUTA_ARCHIVOS + "${Constantes.backslas}logsTarjetas"

		File carpeta = new File (Ruta)
		if (!carpeta.exists()) {
			carpeta.mkdir();
		}
		carpeta = null 

		FileName=Ruta+"${Constantes.backslas}LogEventos"+SFecha+".log";
		SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		try{
			out = new BufferedWriter(new FileWriter(FileName, true));
			out.write(formato.format(new Date())+" - "+ps_cadena);
			out.newLine();
		}catch(IOException e ){
		}finally{
			if (out!= null)
				out.close();
		}
	}
}
