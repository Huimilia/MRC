package MRC;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import javax.swing.text.html.Option;
import java.util.Collections;

public final class SootUtils {

    private static Scene cachedScene;
    private static String cachedKey = "";

    public static Scene loadScene(String apkPath, String androidSdkPath) {
        String key = apkPath + "|" + androidSdkPath;
        if (key.equals(cachedKey) && cachedScene != null) return cachedScene;

        G.reset();
        Options.v().set_whole_program(true);
        Options.v().set_src_prec(Options.src_prec_apk);

//        Options.v().set_force_android_jar(androidSdkPath + "/android-30/android.jar");
        Options.v().set_android_jars(androidSdkPath);
        Options.v().set_process_dir(Collections.singletonList(apkPath));
//        Options.v().set_soot_classpath("D:\\Maven\\maven-repository\\ca\\mcgil\\sable\\soot\\4.3.0\\soot-4.3.0.jar;");
        Options.v().set_process_multiple_dex(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().setPhaseOption("cg", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "on");

        Options.v().set_whole_program(true);
        Options.v().set_include_all(true);

        Options.v().set_output_dir("sootOutput");
        Scene.v().addBasicClass("androidx.camera.lifecycle.ProcessCameraProvider", SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();
        cachedScene = Scene.v();
        cachedKey   = key;
        return cachedScene;
    }

    private SootUtils() {}
}

