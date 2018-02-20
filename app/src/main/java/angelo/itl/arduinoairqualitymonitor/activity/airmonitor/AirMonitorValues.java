package angelo.itl.arduinoairqualitymonitor.activity.airmonitor;

/**
 * Created by Angelo on 06/12/2016.
 */

public interface AirMonitorValues {
    String format = "yyyy-MM-dd HH:mm:ss";
    String NO_SMOKE_INDICATOR = "1.0";
    String TYPE_GENERAL = "general";
    String TYPE_GASES = "ppmGases";
    String SAVE_DATA_AUTOMATICALLY = "save_data_automatically";
    String GAS_UNITY = "measure_gas_unity";
    String GAS_UNITY_DEFAULT = "PPM";
    String MEASURE_VOLT_UNITY = "measure_volt_unity";
    String MEASURE_VOLT_UNITY_DEFAULT = "mV";
    String UPDATE_INTERVAL = "update_interval";
    String UPDATE_INTERVAL_DEFAULT = "1000";
    String AUTOMATIC_LIMITS = "automatic_limits";
    String MGM3 = "Mg/M3";

    enum Quality {GOOD, REGULAR, BAD};//enum to get air Quality
    //co,co2,  ethanol, nh4,toluene,acetone
    float[] normalValues = {1f, 407.57f, 22.5f, 15f, 2f, 16f};
    /*Health effects limits according to:
    * http://www.detectcarbonmonoxide.com/co-health-risks/
    * http://www.engineeringtoolbox.com/co2-comfort-level-d_1024.html
    * http://nj.gov/health/eoh/rtkweb/documents/fs/0844.pdf
    * http://www1.agric.gov.ab.ca/$department/deptdocs.nsf/all/agdex8271
    * https://www.osha.gov/SLTC/toluene/exposure_limits.html
    * https://www.cdc.gov/niosh/docs/81-123/pdfs/0004.pdf
    * */
    // co,  co2, ethanol,nh4,toluene,acetone
    float[] molecularWeights =
            //co,  co2,     ethanol,    nh4,       toluene,acetone
            {28.01f, 44.01f, 46.06844f, 18.03846f, 92.14f, 58.08f};
}
