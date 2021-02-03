package no.ntnu.ctscanarkivsystemserver;

import java.io.IOException;

public class DicomJpgTest {

    public static void main(String[] args) throws IOException {

        String[] command = new String[] {
                "dcm4che-5.22.6-bin\\dcm4che-5.22.6\\bin\\dcm2jpg.bat",
                "TIIN_KOND.CT.SPECIALS_TIINS80S_(ADULT).1.1.2017.12.04.10.12.25.734375.30992199.IMA",
                "img.jpg"

        };

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        final Process process = builder.start();
    }
}
