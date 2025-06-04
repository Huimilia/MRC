package MRC;

import java.util.*;

public class NFCAnalyzer  extends NFCResourceAnalyzer{
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
        map.put("android.nfc.NfcAdapter", Set.of("enableForegroundDispatch"));
        return map;
    }
}
