import java.util.Collection;
import java.util.LinkedList;

public class ThreadSafeLinkedList<E> {
    private LinkedList<E> linkedList = new LinkedList<>();

    public synchronized void add(E value) {
        linkedList.add(value);
    }

    public synchronized void addAll(Collection<? extends E> values) {
        linkedList.addAll(values);
    }


    public synchronized LinkedList<E> get() {
        return linkedList;
    }
}
