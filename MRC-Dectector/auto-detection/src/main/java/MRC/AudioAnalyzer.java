package MRC;

import java.util.*;

public class AudioAnalyzer extends ExclusiveResourceAnalyzer {

    @Override
    protected Set<String> callbackNames() {
        return Set.of(
                "onWindowFocusChanged",
                "onTopResumedActivityChanged",
                "hasWindowFocus",
                "onFocusChange",
                "onAudioFocusChange"
        );
    }

    @Override
    protected Map<String, Set<String>> apiMap() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("android.media.AudioTrack", Set.of("play"));
        map.put("android.media.MediaPlayer", Set.of("start"));
        map.put("android.media.SoundPool", Set.of("play"));
        map.put("androidx.media3.exoplayer.ExoPlayer", Set.of("play", "setPlayWhenReady"));
        return map;
    }
}
