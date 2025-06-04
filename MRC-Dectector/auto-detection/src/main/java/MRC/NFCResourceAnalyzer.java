package MRC;

public abstract class NFCResourceAnalyzer extends ResourceAnalyzer{
    @Override
    protected boolean failIfNoApiCall() {
        return false;
    }

    @Override
    protected boolean requireLoopCheck() {
        // 启用循环检测
        return true;
    }

}
