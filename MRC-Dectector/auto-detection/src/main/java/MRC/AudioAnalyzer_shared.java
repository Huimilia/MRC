package MRC;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class AudioAnalyzer_shared extends SharedResourceAnalyzer {

    @Override
    protected Set<String> callbackNames() {
        return Set.of(
                "onAudioFocusChange",
                "onWindowFocusChanged",
                "onTopResumedActivityChanged",
                "hasWindowFocus",
                "onFocusChange"
        );
    }
    @Override
    protected Map<String, Set<String>> apiMap() {
        Map<String, Set<String>> map = new HashMap<>();

        map.put("android.media.AudioManager", Set.of("abandonAudioFocusRequest"));
        map.put("android.media.MediaPlayer", Set.of("pause", "release"));
        map.put("android.media.AudioTrack", Set.of("pause", "stop", "release"));
        map.put("android.media.SoundPool", Set.of("pause", "release"));
        map.put("androidx.media3.exoplayer.ExoPlayer", Set.of("pause", "stop", "release"));

        return map;
    }
}
