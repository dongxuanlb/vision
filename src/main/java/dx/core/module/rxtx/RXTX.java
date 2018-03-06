package dx.core.module.rxtx;

import com.fazecast.jSerialComm.SerialPort;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        this.port = port;
    }

    public void send(String content) {
        byte[] bytes = content.getBytes();
        port.writeBytes(bytes, bytes.length);
    }

    public static void main(String[] args) throws IOException {
        RXTX rxtx = new RXTX(115200);
        Map<Integer, Integer> mapping = new HashMap(){{
            put(16, 1000);
            put(17, 1050);
            put(18, 1460);
            put(19, 990);
        }};
        Integer time = 1000;
        List<Integer> nums = Lists.newArrayList(16, 18);
        String command = mapping.entrySet()
                .stream()
                .map(map -> "#"+map.getKey()+" P"+map.getValue()+" T"+time+"\r")
                .collect(Collectors.joining());
        rxtx.send(command);
    }

}
