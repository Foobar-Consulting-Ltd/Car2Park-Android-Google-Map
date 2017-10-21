package DirectionFinderPackage;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


/**
 * Created by Alexander Julianto on 10/9/2017.
 */

public class Route {
    public String distance;
    public String duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}