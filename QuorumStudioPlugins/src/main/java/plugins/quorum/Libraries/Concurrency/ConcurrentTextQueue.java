package plugins.quorum.Libraries.Concurrency;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A queue of text that any thread can add to while one thread takes it all.
 * The Console uses it: program output arrives on reader threads, and the
 * main thread puts it in the text box each frame.
 *
 * A clear mark goes through the same queue, so clearing keeps its place in
 * line with the text around it.
 */
public class ConcurrentTextQueue {
    public java.lang.Object me_ = null;

    // Compared by identity, so no added text can be mistaken for it.
    private static final String CLEAR = new String("clear");

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();

    // Whether the last RemoveAll passed a clear mark. Only the thread that
    // calls RemoveAll reads it.
    private boolean cleared = false;

    public void Add(String value) {
        if (value != null) {
            queue.add(value);
        }
    }

    public void AddClear() {
        queue.add(CLEAR);
    }

    /**
     * Takes everything in the queue and joins it. Text before a clear mark is
     * dropped, and WasCleared then returns true.
     */
    public String RemoveAll() {
        cleared = false;
        StringBuilder builder = new StringBuilder();
        String next = queue.poll();
        while (next != null) {
            if (next == CLEAR) {
                cleared = true;
                builder.setLength(0);
            } else {
                builder.append(next);
            }
            next = queue.poll();
        }
        return builder.toString();
    }

    public boolean WasCleared() {
        return cleared;
    }

    public boolean IsEmpty() {
        return queue.isEmpty();
    }

    public void Empty() {
        queue.clear();
    }
}
