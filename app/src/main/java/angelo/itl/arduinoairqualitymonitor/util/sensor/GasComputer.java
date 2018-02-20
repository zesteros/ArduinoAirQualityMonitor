package angelo.itl.arduinoairqualitymonitor.util.sensor;

import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;

/**
 * Created by Angelo on 16/12/2016.
 */

public class GasComputer {
    private static GlobalVariable global;

    private static double analogRead;

    private double resistance;
    //  return ((1024./(float)val) * 5. - 1.)*RLOAD;
    private double ppm;
    //scaleFactorCO * pow((getResistance()/r0CO), -exponentCO);
    //scaleFactors[i] * Math.pow((resistance / defaultR0[i]), exponents[i];
    private double r0;
    //getResistance() * pow((atmCO/scaleFactorCO), (1./exponentCO));
    //resistance * Math.pow((
    private double atmosphereValues[] = {
            1,
            407.57,
            22.5,
            15,
            2.9,
            16
    };
    private static double[] scaleFactors = {
            662.9382,//co
            116.6020682,//co2
            75.3103,//eth
            102.694,//nh4
            43.7748,//tol
            33.1197//ace
    };
    private static double[] exponents = {
            4.0241,
            2.769034857,
            3.1459,
            2.48818,
            3.42936,
            3.36587
    };
    private static double[] defaultR0 = {
            69.65,
            553.232,
            240.293,
            164.8282,
            130.726,
            224.6261
    };
    public static double[] getGasesPPM(){
        double [] gasesPpm = new double[scaleFactors.length];
        global = GlobalVariable.getInstance();
        analogRead = global.getMQ135AnalogRead();
        //scaleFactorCO * pow((getResistance()/r0CO), -exponentCO);
        //scaleFactors[i] * Math.pow((resistance / defaultR0[i]), exponents[i];
        for (int i = 0; i < scaleFactors.length; i++)
            gasesPpm[i] =
                    scaleFactors[i] * Math.pow
                            (
                                    (getResistance(analogRead) / defaultR0[i]),
                                    -exponents[i]
                            );
        return gasesPpm;
    }

    public static double getResistance(double analogRead) {
        //  default formula *** return ((1024./(float)val) * 5. - 1.)*RLOAD;
        // lorf formula *** r = ((1023. * _rload * _vc) / ((float)val * _vref)) - _rload;
        //return (((1023 *10 * 5)/ (analogRead)*5)) - 10;
        return ((1023 / analogRead)*5) * 10;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }
    /*/// To convert readed resistance into ohms
#define RLOAD 10.0
/// R0 for AIR
#define r0Air 1
/// R0 for CO **measured with 24hrs of exposure**
#define r0CO 69.65
/// R0 for CO2 **realized 24 hrs of exposure**
#define r0CO2 553.232
/// R0 for Ethanol **measured with 24hrs of exposure**
#define r0Ethanol 240.293
/// R0 for Ammonium **measured with 24hrs of exposure**
#define r0NH4 164.8282
/// R0 for Toluene **measured with 24hrs of exposure**
#define r0Toluene 130.726
/// R0 for Acetone **measured with 24hrs of exposure**/

    public GasComputer() {
        global = GlobalVariable.getInstance();
        this.analogRead = global.getMQ135AnalogRead();
    }
}
