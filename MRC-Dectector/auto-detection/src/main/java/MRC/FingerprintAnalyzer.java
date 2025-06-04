package MRC;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FingerprintAnalyzer extends ExclusiveResourceAnalyzer{
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
        map.put("android.hardware.biometrics.BiometricPrompt", Set.of("authenticate"));
        map.put("androidx.biometric.BiometricPrompt", Set.of("authenticate"));
        map.put("android.hardware.fingerprint.FingerprintManager", Set.of("authenticate"));

        return map;
    }
}
