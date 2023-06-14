public class ExampleRunnableTask extends Thread {
    private ThreadSafeLinkedList list;
    private ParserTask task;

    public ExampleRunnableTask(ThreadSafeLinkedList<Article> list, ParserTask task) {
        this.list = list;
        this.task = task;
    }

    @Override
    public void run() {
        ParserConfig config = new ParserConfig();
        config.setSilent(true);
        Parser parser = new Parser(config);
        ParserJob job = new ParserJob();
        job.addTask(task);
        parser.parse(job);
        list.addAll(parser.getArticles());
    }
}
