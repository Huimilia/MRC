package MRC;

public abstract class SharedResourceAnalyzer extends ResourceAnalyzer {
    @Override
    protected boolean requireLoopCheck() {
        return false;
    }

    @Override
    protected boolean failIfNoApiCall() {
        return true;
    }
}
