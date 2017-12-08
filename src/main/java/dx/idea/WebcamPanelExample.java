package dx.idea;

import java.awt.*;

import javax.swing.*;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

/**
 * Created by 洞玄 on 2017/12/4.
 *
 * @author 洞玄
 * @date 2017/12/04
 */
public class WebcamPanelExample {

    public static void main(String[] args) throws InterruptedException {

        Dimension[] nonStandardResolutions = new Dimension[] {
            new Dimension(1920, 1080),
            new Dimension(1000, 500),
        };


        Webcam webcam = Webcam.getDefault();
        webcam.setCustomViewSizes(nonStandardResolutions);
        webcam.setViewSize(new Dimension(320, 240));

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        JFrame window = new JFrame("Test webcam panel");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }

}
