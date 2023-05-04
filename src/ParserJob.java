import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ParserJob {
    private LinkedHashSet<ParserTask> _tasks = new LinkedHashSet<>();

    public void addTasks(Collection<? extends ParserTask> c){
        _tasks.addAll(c);
    }

    public void addTask(ParserTask task){
        _tasks.add(task);
    }

    public Set<ParserTask> getTasks(){
        return _tasks;
    }

    public void reset(){
        _tasks.clear();
    }
}
