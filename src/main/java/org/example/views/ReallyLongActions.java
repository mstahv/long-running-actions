package org.example.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import org.example.AppWideTasks;
import org.example.CodeSnippet;
import org.example.DefaultLayout;
import org.example.SlowService;
import org.example.Task;
import org.vaadin.firitin.appframework.MenuItem;
import org.vaadin.firitin.components.RichText;
import org.vaadin.firitin.components.button.ActionButton;
import org.vaadin.firitin.components.button.UIFuture;
import org.vaadin.firitin.components.checkbox.VCheckBox;
import org.vaadin.firitin.components.grid.VGrid;
import org.vaadin.firitin.components.notification.VNotification;
import org.vaadin.firitin.components.progressbar.VProgressBar;
import org.vaadin.firitin.fields.EnumSelect;
import org.vaadin.firitin.layouts.HorizontalFloatLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Route(layout = DefaultLayout.class)
@MenuItem(icon = VaadinIcon.CALENDAR_CLOCK, order = 1070)
public class ReallyLongActions extends VerticalLayout {


    private final UIFuture uiFuture;
    Map<Task, VProgressBar> taskToProgressBar = new WeakHashMap<>();
    private TaskGrid grid;
    private CompletableFuture<Void> future;
    private Map<Task, CompletableFuture<String>> taskToSubscribtion = new HashMap<>();

    public ReallyLongActions(AppWideTasks appWideTasks) {
        add(new H1("Very long action"));
        add(new RichText().withMarkDown("""
                What it your actions can last longer than your sessions? If they are system wide, shared with others
                and you session might close between? Then it depends, and this example simulates one solutions for
                that kind of scenario. You'll essentially need to have some sort of API to know currently running
                action and to subscribe to their results (and/or progress). The service in this example allows to 
                subscribe for the result with CompletableFuture, so hooking to some UI action with UiFuture is 
                rather easy. The demo hooks to currently running system wide tasks when arriving to the view
                and registers to get notified when the task is done. By refreshing, you'll also see others's new
                tasks and can subscribe to those as well. The service keeps at least one task running all the time.
                
                Alternatively you could publish application wide events and listen to them in the UI. In this case
                you would then be on your own with UI synchronization. Note, that e.g. Spring's events are synchronous
                by default, so it might make sense to wrap the actual UI updates with e.g. *uiFuture.runAsync(Runnable)*
                not to block other listeners receiving the event (until all UIs in the app are updated).               
                """));

        uiFuture = new UIFuture();
        grid = new TaskGrid(appWideTasks);
        var activeTasks = grid.listTasks();
        activeTasks.forEach(task -> {
            subscribeForResult(task);
        });

        grid.listTasks();

        IntegerField taskDuration = new IntegerField("Task duration (seconds)");
        taskDuration.setMin(1);
        taskDuration.setMax(60);
        taskDuration.setValue(15);
        add(new HorizontalFloatLayout(
                        taskDuration,
                        new Button("Start new task", event -> {
                            var task = appWideTasks.startTask("User initiated task", taskDuration.getValue());
                            subscribeForResult(task);
                            grid.listTasks();
                        }),
                        new Button("Refresh tasks", event -> {
                            grid.listTasks();
                        })
                )
        );

        add(grid);
    }

    public VProgressBar getProgressBar(Task task) {
        return taskToProgressBar.computeIfAbsent(task, t -> {
            VProgressBar progressBar = new VProgressBar();
            progressBar.setPrepareForOverdueInAnimation(false); // This demo has good estimates
            return progressBar;
        });
    }

    private void subscribeForResult(Task task) {
        CompletableFuture<String> subscription = task.subscribe();
        taskToSubscribtion.put(task, subscription);
        uiFuture.of(subscription).thenAccept(result -> {
            Notification.show("Task completed: " + result);
            getProgressBar(task).finish();
            taskToSubscribtion.remove(task);
        });
    }

    public class TaskGrid extends VGrid<Task> {

        private final AppWideTasks appWideTasks;

        public TaskGrid(AppWideTasks appWideTasks) {
            super(Task.class);
            this.appWideTasks = appWideTasks;
            getColumnByKey("listeners").setVisible(false);

            addComponentColumn(task -> {
                VProgressBar progressBar = getProgressBar(task);
                progressBar.animateToEstimate(task.start(), task.start().plusSeconds(task.duration()));
                return progressBar;
            }).setHeader("Progress");
            addComponentColumn(task -> {
                var subscribedCheckbox = new Checkbox();
                subscribedCheckbox.setValue(taskToSubscribtion.containsKey(task));
                subscribedCheckbox.addValueChangeListener(event -> {
                    if (event.getValue()) {
                        subscribeForResult(task);
                    } else {
                        CompletableFuture<String> subscription = taskToSubscribtion.remove(task);
                        task.unSubscribe(subscription);
                        Notification.show("Unsubscribed of task events");
                    }
                    listTasks();
                });
                return subscribedCheckbox;
            }).setHeader("Subscribed for result");

            getColumns().forEach(column -> column.setAutoWidth(true));
        }

        public List<Task> listTasks() {
            List<Task> activeTasks = appWideTasks.getActiveTasks();
            setItems(activeTasks);
            return activeTasks;
        }
    }

}
