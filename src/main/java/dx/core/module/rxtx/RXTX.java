package dx.core.module.rxtx;

import com.fazecast.jSerialComm.SerialPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RXTX {

    static Logger LG = LogManager.getLogger();

    SerialPort port;

    public RXTX(int rate) {
        this(SerialPort.getCommPorts()[0], rate);
    }

    public RXTX(String portName, int rate) {
        this(SerialPort.getCommPort(portName), rate);
    }

    private RXTX(SerialPort port, int rate) {
        port.setComPortParameters(rate, 8, 1, 0);
        if (!port.openPort()) {
            LG.error("Open Port {} Failed!", port.getDescriptivePortName());
        }
    }

    public void send(String content) {
        byte[] bytes = content.getBytes();
        port.writeBytes(bytes, bytes.length);
    }

    public static void main(String[] args) {
        RXTX rxtx = new RXTX(115200);
        rxtx.send("#P0");
    }

}
