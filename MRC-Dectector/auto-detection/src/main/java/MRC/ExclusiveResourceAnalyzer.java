package MRC;

public abstract class ExclusiveResourceAnalyzer extends ResourceAnalyzer {
    @Override
    protected boolean requireLoopCheck() {
        return true;
    }

    @Override
    protected boolean failIfNoApiCall() {
        return true;
    }
}
