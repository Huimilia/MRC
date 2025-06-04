package MRC;
import net.dongliu.apk.parser.ApkFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManifestUtils {

    public static List<String> extractFeatures(String apkPath) {
        List<String> features = new ArrayList<>();
        try (ApkFile apkFile = new ApkFile(new File(apkPath))) {
            String xml = apkFile.getManifestXml();
            Pattern pattern = Pattern.compile("<uses-feature[^>]*android:name=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(xml);
            while (matcher.find()) {
                features.add(matcher.group(1));
            }
        } catch (Exception e) {
            System.err.println("Extracted uses-feature failed: " + e.getMessage());
        }
        return features;
    }
    public static List<String> extractPermissions(String apkPath) {
        List<String> permissions = new ArrayList<>();
        try (ApkFile apkFile = new ApkFile(new File(apkPath))) {
            String xml = apkFile.getManifestXml();
            Pattern pattern = Pattern.compile("<uses-permission[^>]*android:name=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(xml);
            while (matcher.find()) {
                permissions.add(matcher.group(1));
            }
        } catch (Exception e) {
            System.err.println("Extracted uses-permission failed: " + e.getMessage());
        }
        return permissions;
    }
    public static List<String> extractActivities(String apkPath) {
        List<String> activities = new ArrayList<>();
        try (ApkFile apkFile = new ApkFile(new File(apkPath))) {
            String xml = apkFile.getManifestXml();
            Pattern pattern = Pattern.compile("<activity[^>]*android:name=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(xml);
            while (matcher.find()) {
                String name = matcher.group(1);
                if (name.startsWith(".")) {
                    String pkg = apkFile.getApkMeta().getPackageName();
                    name = pkg + name;
                }
                activities.add(name);
            }
        } catch (Exception e) {
            System.err.println("Extracted activity failed: " + e.getMessage());
        }
        return activities;
    }



}
