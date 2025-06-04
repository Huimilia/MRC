package MRC;

import java.util.*;

public class MicrophoneAnalyzer extends ExclusiveResourceAnalyzer {

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

        // 常见的麦克风输入 API
        map.put("android.media.AudioRecord", Set.of("startRecording"));
        map.put("android.media.MediaRecorder", Set.of("start"));

        return map;
    }
}
