package MRC;

import soot.Scene;
import soot.SootClass;

import java.io.File;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;

public class mrc {

    public static void main(String[] args) {

        String apkDirectoryPath = "F:\\MRC\\Evaluation\\Dataset\\F-droid\\one_month";
        String androidSdkPath   = "D:\\Android\\AndroidSDK\\AndroidSDK\\platforms";

        File apkDirectory = new File(apkDirectoryPath);
        File[] apkFiles = apkDirectory.listFiles((dir, name) -> name.endsWith(".apk"));

        Map<String, ExclusiveResourceAnalyzer> exclusive = new HashMap<>();
        exclusive.put("Camera", new CameraAnalyzer());
        exclusive.put("Microphone", new MicrophoneAnalyzer());
        exclusive.put("AudioExclusive", new AudioAnalyzer());
        exclusive.put("Fingerprint",new FingerprintAnalyzer());
//        exclusive.put("NFC",new NFCAnalyzer());

        Map<String, SharedResourceAnalyzer> shared = new HashMap<>();
        shared.put("AudioShared", new AudioAnalyzer_shared());
        shared.put("Touchscreen",new TouchscreenAnalyzer());

        Map<String, NFCResourceAnalyzer> nfc_resource = new HashMap<>();
        nfc_resource.put("NFC",new NFCAnalyzer());

        Map<String, Integer> useCount = new HashMap<>();
        Map<String, Integer> issueCount = new HashMap<>();

        Map<String, Integer> exclusiveUse = new HashMap<>();
        Map<String, Integer> exclusiveIssue = new HashMap<>();

        Map<String, Integer> sharedUse = new HashMap<>();
        Map<String, Integer> sharedIssue = new HashMap<>();

        Map<String, Set<String>> exclusiveApkMap = new HashMap<>();
        Map<String, Set<String>> exclusiveIssueMap = new HashMap<>();

        Map<String, Set<String>> nfcApkMap = new HashMap<>();
        Map<String, Set<String>> nfcIssueMap = new HashMap<>();


        Map<String, Set<String>> sharedApkMap = new HashMap<>();
        Map<String, Set<String>> sharedIssueMap = new HashMap<>();

//        Set<String> totalApks = new HashSet<>();
//        Set<String> issueApks = new HashSet<>();

        Set<String> analyzedApks = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        class ResourceStats {
            public int totalApkCount = 0;
            public int totalActivities = 0;
            public int totalMatchedActivities = 0;

            public int totalProblemActivitiesMode1 = 0;
            public int totalProblemActivitiesMode2 = 0;

            public int totalProblemApkMode1 = 0;
            public int totalProblemApkMode2 = 0;
            public int appsWithProblem = 0;

            public double sumMode1ProblemRatio = 0.0;
            public double sumMode2ProblemRatio = 0.0;
            public int apkWithMatchedActivities = 0;
        }

        Map<String, ResourceStats> exclusiveStatsMap = new HashMap<>();
            Map<String, ResourceStats> sharedStatsMap = new HashMap<>();
            Map<String, ResourceStats> nfcStatsMap = new HashMap<>();


        for (File apk : apkFiles) {
            System.out.println("\n=== Analyzing APK: " + apk.getName());
            analyzedApks.add(apk.getName());

            Callable<Boolean> task = () -> {
                try {
                    List<String> features = ManifestUtils.extractFeatures(apk.getAbsolutePath());
                    List<String> permissions = ManifestUtils.extractPermissions(apk.getAbsolutePath());
                    List<String> activities = ManifestUtils.extractActivities(apk.getAbsolutePath());


                    boolean hasCamera = features.stream().anyMatch(f -> f.contains("camera"));
                    boolean hasMic = features.contains("android.hardware.microphone");
                    boolean hasOutput = features.contains("android.hardware.audio.output");
                    boolean hasNFC = features.contains("android.hardware.nfc");
                    boolean hasFp = features.contains("android.hardware.fingerprint");

                    hasCamera |= permissions.contains("android.permission.CAMERA");
                    hasMic |= permissions.contains("android.permission.RECORD_AUDIO");
                    hasOutput |= permissions.stream().anyMatch(p ->
                            p.contains("MODIFY_AUDIO_SETTINGS") ||
                                    p.contains("BLUETOOTH") ||
                                    p.contains("BLUETOOTH_ADMIN") ||
                                    p.contains("BLUETOOTH_CONNECT"));
                    hasFp |= permissions.contains("android.permission.USE_FINGERPRINT") ||
                            permissions.contains("android.permission.USE_BIOMETRIC");
                    boolean hasTs = permissions.stream().anyMatch(p ->
                                            p.contains("android.permission.BIND_INPUT_METHOD") ||
                                                    p.contains("android.permission.BIND_ACCESSIBILITY_SERVICE") ||
                                                    p.contains("android.permission.BIND_AUTOFILL_SERVICE") ||
                                                    p.contains("android.permission.INTERACT_ACROSS_USERS_FULL")
                                    );


                    String apkName = apk.getName();

                    for (Map.Entry<String, ExclusiveResourceAnalyzer> entry : exclusive.entrySet()) {
                        String name = entry.getKey();
                        ExclusiveResourceAnalyzer analyzer = entry.getValue();

                        boolean match =
                                (name.equals("Camera") && hasCamera) ||
                                        (name.equals("Microphone") && hasMic) ||
                                        (name.equals("AudioExclusive") && hasOutput) ||
                                        (name.equals("Fingerprint") && hasFp);

                        if (match) {
                            ResourceAnalyzer.AnalysisResultType result = analyzer.analyze(apk, androidSdkPath);

                            ResourceStats stats = exclusiveStatsMap.computeIfAbsent(name, k -> new ResourceStats());

                            stats.totalApkCount++;
                            stats.totalActivities += activities.size();

                            int matched = analyzer.getMatchedActivities().size();
                            int mode1 = analyzer.getMode1ProblemActivities().size();
                            int mode2 = analyzer.getMode2ProblemActivities().size();

                            stats.totalMatchedActivities += matched;
                            stats.totalProblemActivitiesMode1 += mode1;
                            stats.totalProblemActivitiesMode2 += mode2;

                            if (mode1 > 0) stats.totalProblemApkMode1++;
                            if (mode2 > 0) stats.totalProblemApkMode2++;
                            if (mode1 > 0 || mode2 > 0) stats.appsWithProblem++;

                            if(matched > 0)
                            {
                                stats.apkWithMatchedActivities++;
                                stats.sumMode1ProblemRatio += (double) mode1 / matched;
                                stats.sumMode2ProblemRatio += (double) mode2 / matched;
                            }
                        }
                    }
                    for (Map.Entry<String, SharedResourceAnalyzer> entry : shared.entrySet()) {
                        String name = entry.getKey();
                        SharedResourceAnalyzer analyzer = entry.getValue();

                        boolean match =
                                (name.equals("Touchscreen") && hasTs) ||
                                        (name.equals("AudioShared") && hasOutput) ;

                        if (match) {
                            ResourceAnalyzer.AnalysisResultType result = analyzer.analyze(apk, androidSdkPath);

                            ResourceStats stats = sharedStatsMap.computeIfAbsent(name, k -> new ResourceStats());

                            stats.totalApkCount++;
                            stats.totalActivities += activities.size();

                            int matched = analyzer.getMatchedActivities().size();
                            int mode1 = analyzer.getMode1ProblemActivities().size();

                            stats.totalMatchedActivities += matched;
                            stats.totalProblemActivitiesMode1 += mode1;

                            if (mode1 > 0) {
                                stats.totalProblemApkMode1++;
                                stats.appsWithProblem++;
                            }

                            if(matched > 0)
                            {
                                stats.apkWithMatchedActivities++;
                                stats.sumMode1ProblemRatio += (double) mode1 / matched;
                            }
                        }
                    }
                    for (Map.Entry<String, NFCResourceAnalyzer> entry : nfc_resource.entrySet()) {
                        String name = entry.getKey();
                        NFCResourceAnalyzer analyzer = entry.getValue();

                        boolean match = hasNFC;

                        if (match) {
                            ResourceAnalyzer.AnalysisResultType result = analyzer.analyze(apk, androidSdkPath);

                            ResourceStats stats = nfcStatsMap.computeIfAbsent(name, k -> new ResourceStats());

                            stats.totalApkCount++;
                            stats.totalActivities += activities.size();

                            int matched = analyzer.getMatchedActivities().size();
                            int mode2 = analyzer.getMode2ProblemActivities().size();

                            stats.totalMatchedActivities += matched;
                            stats.totalProblemActivitiesMode2 += mode2;

                            if (mode2 > 0) stats.totalProblemApkMode2++;
                            if (mode2 > 0) stats.appsWithProblem++;

                            if (matched > 0) {
                                stats.apkWithMatchedActivities++;
                                stats.sumMode2ProblemRatio += (double) mode2 / matched;
                            }
                        }
                    }
                    System.out.println("\n===== Snapshot of Current APK Resource Statistics =====");
                    System.out.printf("Number of APKs analyzed so far: %d\n", analyzedApks.size());

                    System.out.println("📦 [Exclusive Resource Statistics]");
                    System.out.printf("%-20s %-6s %-6s %-6s %-6s %-6s %-6s %-6s %-8s %-8s\n",
                            "Resource", "APKs", "Issues", "M1APK", "M2APK", "Acts", "Hit", "M1Act", "M1Ratio", "M2Ratio");

                    for (Map.Entry<String, ResourceStats> entry : exclusiveStatsMap.entrySet()) {
                        String name = entry.getKey();
                        ResourceStats stats = entry.getValue();
                        System.out.printf("%-20s %-6d %-6d %-6d %-6d %-6d %-6d %-6d %8f%% %8f%%\n",
                                name,
                                stats.totalApkCount,
                                stats.appsWithProblem,
                                stats.totalProblemApkMode1,
                                stats.totalProblemApkMode2,
                                stats.totalActivities,
                                stats.totalMatchedActivities,
                                stats.totalProblemActivitiesMode1,
                                stats.apkWithMatchedActivities == 0 ? 0.0 : stats.sumMode1ProblemRatio / stats.apkWithMatchedActivities * 100,
                                stats.apkWithMatchedActivities == 0 ? 0.0 : stats.sumMode2ProblemRatio / stats.apkWithMatchedActivities * 100
                        );
                    }
                    System.out.println("\n📦 [Shared Resource Statistics]");
                    System.out.printf("%-20s %-6s %-10s %-6s %-6s %-6s %-8s %-8s\n",
                            "Resource", "APKs", "Issue APKs", "M1APK", "Acts", "Hit", "M1Act", "M1Ratio");

                    for (Map.Entry<String, ResourceStats> entry : sharedStatsMap.entrySet()) {
                        String name = entry.getKey();
                        ResourceStats stats = entry.getValue();
                        System.out.printf("%-20s %-6d %-10d %-6d %-6d %-6d %-8d %8f%%\n",
                                name,
                                stats.totalApkCount,
                                stats.appsWithProblem,
                                stats.totalProblemApkMode1,
                                stats.totalActivities,
                                stats.totalMatchedActivities,
                                stats.totalProblemActivitiesMode1,
                                stats.apkWithMatchedActivities == 0 ? 0.0 : stats.sumMode1ProblemRatio / stats.apkWithMatchedActivities * 100
                        );
                    }
                    System.out.println("\n📦 [NFC Resource Statistics]");
                    System.out.printf("%-20s %-6s %-6s %-6s %-6s %-6s %-8s %-8s\n",
                            "Resource", "APKs", "Issues", "M2APK", "Acts", "Hit", "M2Act", "M2Ratio");


                    for (Map.Entry<String, ResourceStats> entry : nfcStatsMap.entrySet()) {
                        String name = entry.getKey();
                        ResourceStats stats = entry.getValue();
                        System.out.printf("%-20s %-6d %-6d %-6d %-6d %-6d %-8d %8f%%\n",
                                name,
                                stats.totalApkCount,
                                stats.appsWithProblem,
                                stats.totalProblemApkMode2,
                                stats.totalActivities,
                                stats.totalMatchedActivities,
                                stats.totalProblemActivitiesMode2,
                                stats.apkWithMatchedActivities == 0 ? 0.0 : stats.sumMode2ProblemRatio / stats.apkWithMatchedActivities * 100
                        );
                    }



                    return true;

                } finally {
                    soot.G.reset();
                }
            };
        }

        executor.shutdown();

        }

}
