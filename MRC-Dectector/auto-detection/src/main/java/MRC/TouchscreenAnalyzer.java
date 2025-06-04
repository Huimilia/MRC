package MRC;

import java.util.*;

public class TouchscreenAnalyzer extends SharedResourceAnalyzer {

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
        // API that should be called when keyboard input is released
        Map<String, Set<String>> map = new HashMap<>();
        map.put("android.view.inputmethod.InputMethodManager", Set.of("hideSoftInputFromWindow"));
        map.put("android.view.inputmethod.InputMethodService", Set.of("onDestroy"));
        return map;
    }
}
