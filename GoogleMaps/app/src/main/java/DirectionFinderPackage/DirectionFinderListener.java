package DirectionFinderPackage;

import java.util.List;

/**
 * Created by Alexander Julianto on 10/9/2017.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
