package MRC;

import java.util.*;

public class CameraAnalyzer extends ExclusiveResourceAnalyzer {

    @Override
    protected Set<String> callbackNames() {
        return Set.of(
                "onWindowFocusChanged",
                "onTopResumedActivityChanged",
                "hasWindowFocus",
                "onFocusChange"
        );
    }

    @Override
    protected Map<String, Set<String>> apiMap() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("android.hardware.Camera", Set.of("open"));
        map.put("android.hardware.camera2.CameraManager", Set.of("openCamera"));

        return map;
    }
}
