package kp.rollingcube.ce.campaign;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @author Marc
 */
public final class CampaingLoadSaveState
{
    private final @NonNull Thread thread;
    @Getter private final @NonNull Campaign campaign;
    private final AtomicInteger total = new AtomicInteger(0);
    private final AtomicInteger current = new AtomicInteger(0);
    private final AtomicReference<String> currentDataText = new AtomicReference<>();
    
    @Getter @Setter private StateStartCallback onStartCallback;
    @Getter @Setter private StateUpdateCallback onUpdateCallback;
    @Getter @Setter private StateFinishCallback onFinishCallback;
    
    CampaingLoadSaveState(@NonNull Campaign campaign, @NonNull Path path, TaskFn task)
    {
        this.thread = new Task(campaign, path, task);
        this.campaign = campaign;
    }
    
    public void start()
    {
        if(thread.getState() == Thread.State.NEW)
            thread.start();
    }
    
    public int getTotal() { return total.get(); }
    public int getCurrent() { return current.get(); }
    
    public @NonNull String getCurrentDataText()
    {
        var text = currentDataText.get();
        return text == null ? "" : text;
    }
    
    public void waitUntilFinished()
    {
        try { thread.join(); }
        catch(InterruptedException ex) {}
    }
    
    void addElements(int amount) { this.total.addAndGet(Math.max(0, amount)); }
    void addElement() { this.total.incrementAndGet(); }
    
    void resolveElement() { this.current.incrementAndGet(); sendUpdateSignal(); }
    
    void start(String firstData)
    {
        setCurrentDataText(firstData);
        if(onStartCallback != null)
            onStartCallback.onStart(total.get(), current.get(), getCurrentDataText());
    }
    void start(Path path) { start(path.toString()); }
    
    void finish(String error)
    {
        setCurrentDataText("Done!");
        if(onFinishCallback != null)
            onFinishCallback.onFinish(new OperationResult(error));
    }
    void finish() { finish(null); }
    
    void setCurrentDataText(String text)
    {
        currentDataText.set(text);
    }
    
    void setCurrentDataText(Path path) { setCurrentDataText(path.toString()); }
    
    private void sendUpdateSignal()
    {
        if(onUpdateCallback != null)
            onUpdateCallback.onUpdate(total.get(), current.get(), getCurrentDataText());
    }
    
    
    public static interface StateStartCallback
    {
        void onStart(int total, int current, String text);
    }
    
    public static interface StateUpdateCallback
    {
        void onUpdate(int total, int current, String text);
    }
    
    public static interface StateFinishCallback
    {
        void onFinish(OperationResult result);
    }
    
    public static final class OperationResult
    {
        private final String error;
        
        private OperationResult(String error) { this.error = error; }
        
        public boolean hasError() { return error != null; }
        public @NonNull String getError() { return error == null ? "" : error; }
    }
    
    static interface TaskFn
    {
        void run(Campaign campaign, Path path, CampaingLoadSaveState state);
    }
    
    final class Task extends Thread
    {
        Task(Campaign campaign, Path path, TaskFn task)
        {
            super(() -> task.run(campaign, path, CampaingLoadSaveState.this));
        }
    }
}
