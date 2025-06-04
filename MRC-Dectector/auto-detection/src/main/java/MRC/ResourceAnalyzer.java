package MRC;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * 通用资源分析器框架：
 * 支持检测回调方法中是否调用了目标 API，并可选支持循环体内重复调用检测。
 */
public abstract class ResourceAnalyzer {

    protected abstract Set<String> callbackNames();
    protected abstract Map<String, Set<String>> apiMap();

    /** 是否报告未调用目标 API 为问题（共享资源释放检测为 true） */
    protected boolean failIfNoApiCall() {
        return true;
    }

    protected Set<String> matchedActivities = new HashSet<>();
    public Set<String> getMatchedActivities() {
        return matchedActivities;
    }

    public enum AnalysisResultType {
        NORMAL,
        NO_API_CALL,
        LOOP_CALL,
        BOTH
    }
    protected Set<String> mode1ProblemActivities = new HashSet<>();
    protected Set<String> mode2ProblemActivities = new HashSet<>();

    public Set<String> getMode1ProblemActivities(){
        return mode1ProblemActivities;
    }
    public Set<String> getMode2ProblemActivities(){
        return mode2ProblemActivities;
    }



    boolean indirectApiCallOnly = false;
    boolean globalHasBindToLifecycle = false;




    /** 是否启用循环体内重复调用检测（独占资源检测为 true） */
    protected boolean requireLoopCheck() {
        return false;
    }
//
//    protected void dumpAllMethodNames(File apk, String sdkPath, PrintWriter writer) {
//        Scene sc = SootUtils.loadScene(apk.getAbsolutePath(), sdkPath);
//
//        for (SootClass cls : new ArrayList<>(Scene.v().getApplicationClasses())) {
//            for (SootMethod m : cls.getMethods()) {
//                if (m.isConcrete()) {
//                    writer.println(m.getSignature());
//                }
//            }
//        }
//
//        writer.flush(); // 强制刷新
//    }

//    public AnalysisResultType analyze(File apk, String sdkPath) {
//        // 每次清空分析状态
//        matchedActivities.clear();
//        mode1ProblemActivities.clear();
//        mode2ProblemActivities.clear();
//
//        Scene sc = SootUtils.loadScene(apk.getAbsolutePath(), sdkPath);
//
//        CallGraph cg = Scene.v().getCallGraph();
//
//        Map<String, Set<String>> apis = apiMap();
//
//        Set<String> allApiActivities = new HashSet<>();
//        Set<String> noCallbackActivities = new HashSet<>();
//        Set<String> loopCallActivities = new HashSet<>();
//
//        System.out.println("📋 调用图中所有调用的方法签名：");
//        int count = 0;
//        for (Iterator<Edge> it = Scene.v().getCallGraph().iterator(); it.hasNext(); ) {
//            Edge e = it.next();
//            System.out.println(" - " + e.src().method().getSignature() + " → " + e.tgt().method().getSignature());
//            count++;
//        }
//        System.out.println("调用边总数: " + count);
//
//
//
//
//
//        for (Iterator<Edge> it = cg.iterator(); it.hasNext(); ) {
//            Edge edge = it.next();
//            SootMethod target = edge.tgt();
//
//            if (!target.isConcrete()) continue;
//
//            String clsName = target.getDeclaringClass().getName();
//            String methodName = target.getName();
//
//            if (!apis.getOrDefault(clsName, Set.of()).contains(methodName)) continue;
//
//            SootMethod caller = edge.src();
//            SootClass declaringClass = caller.getDeclaringClass();
//
//            if (!Scene.v().getActiveHierarchy().isClassSubclassOf(declaringClass, Scene.v().getSootClass("android.app.Activity"))) {
//                continue;
//            }
//
//            String activityName = declaringClass.getName();
//            allApiActivities.add(activityName);
//
//            // 判断是否来自回调方法
//            Set<SootMethod> visited = new HashSet<>();
//            boolean fromCallback = reachesCallbackMethod(cg, caller, visited);
//
//            if (!fromCallback) {
//                noCallbackActivities.add(activityName);  // 记录没有回调路径的
//            }
//
//            // 判断是否在 loop 中重复调用
//            if (requireLoopCheck() && containsApiInLoop(caller, apis)) {
//                loopCallActivities.add(activityName);
//            }
//        }
//
//        matchedActivities.addAll(allApiActivities);
//        mode1ProblemActivities.addAll(noCallbackActivities);
//        mode2ProblemActivities.addAll(loopCallActivities);
//
//        // 最终分类结果
//        if (matchedActivities.isEmpty()) {
//            return AnalysisResultType.NO_API_CALL;
//        } else if (!mode1ProblemActivities.isEmpty() && !mode2ProblemActivities.isEmpty()) {
//            return AnalysisResultType.BOTH;
//        } else if (!mode1ProblemActivities.isEmpty()) {
//            return AnalysisResultType.NO_API_CALL;
//        } else if (!mode2ProblemActivities.isEmpty()) {
//            return AnalysisResultType.LOOP_CALL;
//        } else {
//            return AnalysisResultType.NORMAL;
//        }
//    }

//    public AnalysisResultType analyze(File apk, String sdkPath) {
//        matchedActivities.clear();
//        mode1ProblemActivities.clear();
//        mode2ProblemActivities.clear();
//
//        Scene scene = SootUtils.loadScene(apk.getAbsolutePath(), sdkPath);
//        Map<String, Set<String>> apis = apiMap();
//        globalHasBindToLifecycle = detectGlobalBindToLifecycle(scene);
//
//        // 如果已检测到 bindToLifecycle，直接标为正常
//        if (globalHasBindToLifecycle) {
//            matchedActivities.clear();
//            mode1ProblemActivities.clear();
//            mode2ProblemActivities.clear();
//            return AnalysisResultType.NORMAL;
//        }
//
//        for (SootClass cls : new ArrayList<>(scene.getApplicationClasses())) {
//
////            if (!Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(cls, Scene.v().getSootClass("android.app.Activity"))) {
////                continue;
////            }
//            SootClass activityClass = Scene.v().getSootClass("android.app.Activity");
//
//            // 跳过接口/抽象类本身
//            if (cls.isInterface() || cls.isAbstract()) continue;
//
//            // 判断是否继承自 Activity
//            if (!Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(cls, activityClass)) {
//                continue;
//            }
//
//            boolean hasApiCall = false;
//            boolean hasLoopCall = false;
//
//            for (SootMethod method :new ArrayList<>(cls.getMethods())) {
//                if (!method.isConcrete()) continue;
////                if (method.getSignature().toLowerCase().contains("bindtolifecycle")) {
////                    System.out.println("Found!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
////                    globalHasBindToLifecycle=true;
////                    hasApiCall = true;
////                }
//
//                Set<SootMethod> visited = new HashSet<>();
//                if (containsApiCall(method, apis, visited)) {
//                    hasApiCall = true;
//
//                    if (requireLoopCheck() && containsApiInLoop(method, apis)) {
//                        hasLoopCall = true;
//                    }
//                }
////                Set<SootMethod> visited = new HashSet<>();
////                boolean methodCallsApi = containsApiCall(method, apis, visited);
////                if (methodCallsApi) {
////                    hasApiCall = true;
////                    if (requireLoopCheck() && containsApiInLoop(method, apis)) {
////                        hasLoopCall = true;
////                    }
////                } else {
////                    // 该方法本身未命中 API，但它调用的方法里可能包含 API
////                    boolean anyInChainCallsApi = visited.stream().anyMatch(m -> {
////                        String decl = m.getDeclaringClass().getName();
////                        String name = m.getName();
////                        return apis.getOrDefault(decl, Set.of()).contains(name);
////                    });
////
////                    if (anyInChainCallsApi) {
////                        indirectApiCallOnly = true;
////                    }
////                }
//
//            }
//
//            String activityName = cls.getName();
//            if (hasApiCall) {
//                matchedActivities.add(activityName);
//                if (hasLoopCall) {
//                    mode2ProblemActivities.add(activityName);
//                }
//            } else {
//                mode1ProblemActivities.add(activityName);
//            }
//        }
//
//        // 关键逻辑：只要任意类使用 bindToLifecycle，就认为没有问题
//        if (globalHasBindToLifecycle) {
//            matchedActivities.addAll(mode1ProblemActivities); // 全部视为正常
//            mode1ProblemActivities.clear();
//            mode2ProblemActivities.clear();
//            return AnalysisResultType.NORMAL;
//        }
//
//        if (matchedActivities.isEmpty()) {
//            return AnalysisResultType.NO_API_CALL;
//        } else if (!mode1ProblemActivities.isEmpty() && !mode2ProblemActivities.isEmpty()) {
//            return AnalysisResultType.BOTH;
//        } else if (!mode1ProblemActivities.isEmpty()) {
//            return AnalysisResultType.NO_API_CALL;
//        } else if (!mode2ProblemActivities.isEmpty()) {
//            return AnalysisResultType.LOOP_CALL;
//        } else {
//            return AnalysisResultType.NORMAL;
//        }
//
//    }
    public AnalysisResultType analyze(File apk, String sdkPath) {
        matchedActivities.clear();
        mode1ProblemActivities.clear();
        mode2ProblemActivities.clear();

        Scene scene = SootUtils.loadScene(apk.getAbsolutePath(), sdkPath);
        Map<String, Set<String>> apis = apiMap();
//         如果已检测到 bindToLifecycle，直接标为正常
        if (globalHasBindToLifecycle) {
            matchedActivities.clear();
            mode1ProblemActivities.clear();
            mode2ProblemActivities.clear();
            return AnalysisResultType.NORMAL;
        }

        boolean anyApiDetected = false;

        for (SootClass cls : new ArrayList<>(scene.getApplicationClasses())) {
            if (cls.isInterface() || cls.isAbstract()) continue;

            for (SootMethod method : new ArrayList<>(cls.getMethods())) {
                if (!method.isConcrete()) continue;

                if (!callbackNames().contains(method.getName())) continue;

                try {
                    Body body = method.retrieveActiveBody();

                    boolean hasApiCall = containsApiCall(method, apis, new HashSet<>());
                    boolean hasLoopCall = requireLoopCheck() && containsApiInLoop(method, apis);

                    String className = cls.getName();

                    if (hasApiCall) {
                        anyApiDetected = true;
                        matchedActivities.add(className);

                        if (hasLoopCall) {
                            mode2ProblemActivities.add(className);
                        }

                    } else {
                        if (failIfNoApiCall()) {
                            mode1ProblemActivities.add(className);
                        }
                    }

                } catch (Exception e) {
                    System.err.println("分析失败: " + method.getSignature());
                }
            }
        }

        // 最终分类结果
        if (!anyApiDetected) {
            return AnalysisResultType.NO_API_CALL;
        } else if (!mode1ProblemActivities.isEmpty() && !mode2ProblemActivities.isEmpty()) {
            return AnalysisResultType.BOTH;
        } else if (!mode1ProblemActivities.isEmpty()) {
            return AnalysisResultType.NO_API_CALL;
        } else if (!mode2ProblemActivities.isEmpty()) {
            return AnalysisResultType.LOOP_CALL;
        } else {
            return AnalysisResultType.NORMAL;
        }
    }



//    protected boolean hasDirectEntryApi(SootMethod m) {
//
//        if (!m.isConcrete()) return false;
//        try {
//            if (m.getSignature().contains("bindToLifecycle")) {
//                System.out.println(m.getName());
//                return true;
//            }
//        } catch (Exception e) {
//            return false;
//        }
//        return false;
//    }

    private boolean detectGlobalBindToLifecycle(Scene scene) {
        for (SootClass cls : scene.getApplicationClasses()) {
            for (SootMethod method : cls.getMethods()) {
                if (!method.isConcrete()) continue;
                try {
                    Body body = method.retrieveActiveBody();
                    for (Unit u : body.getUnits()) {
                        if (u instanceof Stmt stmt && stmt.containsInvokeExpr()) {
                            InvokeExpr inv = stmt.getInvokeExpr();
                            String methodName = inv.getMethodRef().name();
                            if (methodName.equalsIgnoreCase("bindToLifecycle")) {
                                System.out.println("Found bindToLifecycle in " + method.getSignature());
                                return true;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    private Set<SootMethod> findCallGraphLoops(Scene scene) {
        CallGraph cg = scene.getCallGraph();
        Map<SootMethod, Set<SootMethod>> graph = new HashMap<>();

        for (Iterator<Edge> it = cg.iterator(); it.hasNext(); ) {
            Edge edge = it.next();
            SootMethod src = edge.getSrc().method();
            SootMethod tgt = edge.getTgt().method();
            if (!src.isConcrete() || !tgt.isConcrete()) continue;

            graph.computeIfAbsent(src, k -> new HashSet<>()).add(tgt);
        }

        Set<SootMethod> inCycle = new HashSet<>();
        Set<SootMethod> visited = new HashSet<>();

        for (SootMethod m : graph.keySet()) {
            dfsCycleDetect(m, graph, visited, new HashSet<>(), inCycle);
        }

        return inCycle;
    }
    private void dfsCycleDetect(SootMethod current,
                                Map<SootMethod, Set<SootMethod>> graph,
                                Set<SootMethod> visited,
                                Set<SootMethod> stack,
                                Set<SootMethod> result) {
        if (stack.contains(current)) {
            result.add(current);
            return;
        }
        if (!visited.add(current)) return;

        stack.add(current);
        for (SootMethod neighbor : graph.getOrDefault(current, Collections.emptySet())) {
            dfsCycleDetect(neighbor, graph, visited, stack, result);
        }
        stack.remove(current);
    }

    private boolean isInCallGraphCycle(CallGraph cg, SootMethod current, Set<SootMethod> visited, SootMethod origin) {
        if (!visited.add(current)) return false;

        Iterator<Edge> edges = cg.edgesOutOf(current);
        while (edges.hasNext()) {
            SootMethod target = edges.next().tgt();
            if (target.equals(origin)) {
                return true; // cycle detected
            }
            if (isInCallGraphCycle(cg, target, visited, origin)) {
                return true;
            }
        }

        return false;
    }


    public static class ApiCallAnalysisResult {
        public Set<String> activities = new HashSet<>();
        public boolean hasCallbackEntry = false;
    }

//    private boolean reachesCallbackMethod(CallGraph cg, SootMethod method, Set<SootMethod> visited) {
//        if (!visited.add(method)) return false;
//
//        // 如果当前方法就是你定义的回调名之一，则认为找到了入口
//        if (callbackNames().contains(method.getName())) return true;
//
//        // 递归向上追溯调用链
//        for (Iterator<Edge> inEdges = cg.edgesInto(method); inEdges.hasNext(); ) {
//            Edge edge = inEdges.next();
//            SootMethod parent = edge.src();
//            if (reachesCallbackMethod(cg, parent, visited)) {
//                return true;
//            }
//        }
//
//        return false;
//    }

//    public ApiCallAnalysisResult findActivitiesWithCallbackToApi(Scene scene) {
//        ApiCallAnalysisResult result = new ApiCallAnalysisResult();
//        CallGraph cg = scene.getCallGraph();
//
//        for (Iterator<Edge> it = cg.iterator(); it.hasNext(); ) {
//            Edge edge = it.next();
//            SootMethod tgt = edge.tgt();
//            if (!tgt.isConcrete()) continue;
//
//            String cls = tgt.getDeclaringClass().getName();
//            String name = tgt.getName();
//
//            // 是目标 API 方法？
//            if (apiMap().getOrDefault(cls, Set.of()).contains(name)) {
//                SootMethod caller = edge.src();
//                Set<SootMethod> visited = new HashSet<>();
//                boolean fromCallback = reachesCallbackMethod(cg, caller, visited);
//
//                if (fromCallback) {
//                    result.hasCallbackEntry = true;
//                }
//
//                SootClass declaring = caller.getDeclaringClass();
//                if (Scene.v().getActiveHierarchy().isClassSubclassOf(declaring, Scene.v().getSootClass("android.app.Activity"))) {
//                    result.activities.add(declaring.getName());
//                }
//            }
//        }
//
//        return result;
//    }

    protected boolean containsApiCall(SootMethod method, Map<String, Set<String>> apis, Set<SootMethod> visited) {
        if (!method.isConcrete() || visited.contains(method)) return false;
        visited.add(method);

        try {
            Body body = method.retrieveActiveBody();
            for (Unit u : body.getUnits()) {
                if (u instanceof Stmt stmt && stmt.containsInvokeExpr()) {
                    InvokeExpr inv = stmt.getInvokeExpr();
                    String cls = inv.getMethodRef().declaringClass().getName();
                    String name = inv.getMethodRef().name();

                    // 命中目标 API
                    if (apis.getOrDefault(cls, Set.of()).contains(name)) {
                        return true;
                    }

                    // 递归分析 callee 方法体（如存在）
                    try {
                        SootMethod callee = inv.getMethod();
                        if (containsApiCall(callee, apis, visited)) return true;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        return false;
    }




    private boolean containsApiInLoop(SootMethod method, Map<String, Set<String>> apis) {
        if (!method.isConcrete()) return false;


        try {
            Body body = method.retrieveActiveBody();
            UnitGraph cfg = new ExceptionalUnitGraph(body);

            // 遍历所有边，查找是否存在回边
            for (Unit u : cfg) {
                if (!(u instanceof Stmt stmt) || !stmt.containsInvokeExpr()) continue;

                InvokeExpr inv = stmt.getInvokeExpr();
                String cls = inv.getMethodRef().declaringClass().getName();
                String name = inv.getMethodRef().name();

                if (apis.containsKey(cls) && apis.get(cls).contains(name)) {
                    // 检查该调用是否在循环结构中
                    for (Unit pred : cfg.getPredsOf(u)) {
                        if (dominates(cfg, u, pred)) {
                            return true; // 存在回边，说明在循环中
                        }
                    }
                }
            }

        } catch (Exception e) {
            // body broken
        }

        return false;
    }



    protected boolean hasLoopDuplicate(Body body, Map<String, Set<String>> apis) {
        UnitGraph cfg = new ExceptionalUnitGraph(body);
        Map<String, Integer> freq = new HashMap<>();

        for (Unit u : body.getUnits()) {
            if (!(u instanceof Stmt stmt) || !stmt.containsInvokeExpr()) continue;

            InvokeExpr inv = stmt.getInvokeExpr();
            String cls = inv.getMethodRef().declaringClass().getName();
            String name = inv.getMethodRef().name();

            if (apis.containsKey(cls) && apis.get(cls).contains(name)) {
                if (isInLoop(cfg, u)) {
                    String key = cls + "." + name;
                    freq.put(key, freq.getOrDefault(key, 0) + 1);
                    if (freq.get(key) > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isInLoop(UnitGraph cfg, Unit unit) {
        for (Unit pred : cfg) {
            List<Unit> succs = cfg.getSuccsOf(pred);
            if (succs.contains(unit) && dominates(cfg, unit, pred)) {
                return true;
            }
        }
        return false;
    }

    private boolean dominates(UnitGraph cfg, Unit dominator, Unit node) {
        try {
            DominatorsFinder<Unit> finder = new MHGDominatorsFinder<>(cfg);
            return finder.getDominators(node).contains(dominator);
        } catch (Exception e) {
            return false;
        }
    }

}