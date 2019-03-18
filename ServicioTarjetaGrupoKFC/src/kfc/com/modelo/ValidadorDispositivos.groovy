package kfc.com.modelo
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

// Ip
import java.io.IOException;
import java.net.InetAddress;

public class ValidadorDispositivos {



	public static  boolean DispositivoConectadoPuertoSerial(String puertoSerial) {

		try {
 
			Enumeration portList = CommPortIdentifier.getPortIdentifiers();

			while (portList.hasMoreElements()) {

				CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
				if (portId.getPortType() == CommPortIdentifier.PORT_PARALLEL) {
					if (portId.getName().equals(puertoSerial))
						return true 
				} else {
					if (portId.getName().equals(puertoSerial))
						return true  
				}

			}
 
			return false 
		} catch (Exception e) {
			return false 
		}

	}

	public static void   DispositivoConectadoIP (String ip) {
 
        try {
            InetAddress ping = InetAddress.getByName(ip);
            if (ping.isReachable(2000)) {
                System.out.println(ip + " - responde!");
            } else {
                System.out.println(ip + " - no responde!");
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

	}
}
