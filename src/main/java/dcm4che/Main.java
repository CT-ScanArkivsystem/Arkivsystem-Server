package dcm4che;

public class Main {
    public static void main(String[] args) {

/*
        String originalCommand =
                "dcm4che-5.22.6-bin\\dcm4che-5.22.6\\bin\\dcm2xml.bat" + " " +
                "TIIN_KOND.CT.SPECIALS_TIINS80S_(ADULT).1.1.2017.12.04.10.12.25.734375.30992199.IMA";
*/

        String[] command = new String[] {"TIIN_KOND.CT.SPECIALS_TIINS80S_(ADULT).1.1.2017.12.04.10.12.25.734375.30992199.IMA"};

        Dcm2Xml.start(command);
    }
}
