package org.example.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import org.example.CodeSnippet;
import org.example.DefaultLayout;
import org.example.SlowService;
import org.vaadin.firitin.appframework.MenuItem;
import org.vaadin.firitin.components.RichText;
import org.vaadin.firitin.components.button.ActionButton;
import org.vaadin.firitin.components.button.UIFuture;
import org.vaadin.firitin.components.checkbox.VCheckBox;
import org.vaadin.firitin.components.notification.VNotification;
import org.vaadin.firitin.components.progressbar.VProgressBar;
import org.vaadin.firitin.fields.EnumSelect;
import org.vaadin.firitin.layouts.HorizontalFloatLayout;

import java.util.function.Supplier;

@Route(layout = DefaultLayout.class)
@MenuItem(icon = VaadinIcon.ACADEMY_CAP, order = 1070)
public class AsyncHelpers extends VerticalLayout {

    public AsyncHelpers(SlowService slowService) {
        add(new H1("Less error prone boilerplate code with helper classes"));

        add(new RichText().withMarkDown("""
                I admit, the amount of boilerplate code needed for a properly implemented asynchronous processing has made
                me make compromises with the UX. To improve the situation, I baked together couple of helpers to 
                [Viritin](https://vaadin.com/directory/component/flow-viritin) (Maven "coordinates": 
                *in.virit:viritin:2.9.0*) that can help you provide better UX with less code. The helpers are new kids 
                on the block, so I'm eager to hear your feedback or improvement ideas!
                
                *PRO TIP: Don't hesitate to copy-paste the implementations to your project if you can't add a dependency to your project.*
                
                ## ActionButton, an easy to use trigger for async actions
                
                ActionButton is a button designed especially to trigger slow actions. It handles the UI updates
                and provides some built-in (customizable) functionality to make the UX better. Most of the time you can
                forget everything about UI.access, server-push, etc.
                """));

        add(new CodeSnippet(getClass(), "trivialActionButton"));
        // CodeSnippet: trivialActionButton
        var trivialActionButton = new ActionButton<>("Run slow action", () -> slowService.generateStringAsync(3000));
        // The provided actions must NOT modify the UI (at least without the "usual UI.access ceremonies")!
        // Optional pre- and post-UI update hooks are provided for that purpose, here showing string returned from
        // the CompletableFuture in a notification
        trivialActionButton.setPostUiUpdate(str -> Notification.show(str));
        // CodeSnippetEnd: trivialActionButton
        add(trivialActionButton);

        add(new RichText().withMarkDown("""
                The constructor takes in a supplier providing CompletableFuture. You can also set it using
                *setCompletableFutureAction()* or use the shorthands setAction(Supplier) or setAction(Runnable) which
                can be handy is old-school.
                
                With pre and post UI hooks provide you an easy way to make UI updates before and after the long running
                action. The hooks are executed in the UI thread, so you can safely touch the UI components. 
                
                By default the button is disabled while the action is ongoing and a small progressbar is shown below
                the button. You can provide an estimated duration for the action, which is used to show an animation in
                the progressbar or to use updateProgressAsync() to show real progress of the action.
                
                In case you are using some more sophisticated method to inform your user of the progress disable the 
                progress bar *setShowProgressBar(false)*. In the example below, that allows you to test the button in
                various configurations, there is an example of a dialog with a progress bar and a cancel button. The
                code is bit messy (see via top-right corner), but might give you some ideas how to handle the progress in your own app.
                
                """));

        var showDialog = new VCheckBox("Show dialog during task");
        var showNotificationOnStart = new VCheckBox("Show notification on start");
        var disableUI = new VCheckBox("Disable UI during task");
        var builtInProgressbar = new VCheckBox("Built in progress bar").withValue(true)
                .withTooltip("On by default, implicitly off if preUiAction defined (like dialog in this example). Toggle this to enable explicitly.");
        var estimate = new VCheckBox("Define estimated time to 4 secs (really 5)");
        add(new HorizontalFloatLayout(showDialog, showNotificationOnStart, disableUI, builtInProgressbar, estimate));

        Dialog taskInProgressDialog = new Dialog();
        taskInProgressDialog.setDraggable(true);
        taskInProgressDialog.setResizable(true);
        taskInProgressDialog.setModal(false);
        taskInProgressDialog.setCloseOnOutsideClick(false);
        taskInProgressDialog.setWidth("30vw");
        taskInProgressDialog.setHeaderTitle("Computing things, please wait...");
        taskInProgressDialog.add(new Paragraph("This dialog is optional and built in preTaskAction. You can move this " +
                "dialog around and keep doing other things.."));

        VProgressBar progressBarInDialog = new VProgressBar();
        progressBarInDialog.setIndeterminate(true);
        taskInProgressDialog.add(progressBarInDialog);
        taskInProgressDialog.add(new Button("Test UI button", event -> {
            Notification.show("I'm alive!");
        }));

        // The actual component usage
        ActionButton actionButton = new ActionButton("Compute things...");

        Supplier<String> basicSlowAction = () -> {
            // This is the actual task, executed later in a separate thread
            // DO NOT MODIFY UI HERE!
            return slowService.slowBlockingMethod(5000);
        };
        Supplier<String> trackableSlowAction = () -> {
            // This is the actual task, executed later in a separate thread
            // DO NOT MODIFY UI HERE!
            return slowService.slowBlockingMethodWithNotifier(progress -> {
                // The action button provides an API to update the progress bar, synced automatically, no need for UI.access
                actionButton.updateProgressAsync(progress, 0, 1);
            });
        };
        Select<Supplier<String>> supplierSelect = new Select<>();
        supplierSelect.setLabel("Select action:");
        supplierSelect.setItems(basicSlowAction, trackableSlowAction);
        supplierSelect.setItemLabelGenerator(s -> s == basicSlowAction ? "Basic slow action" : "Trackable slow action");
        supplierSelect.addValueChangeListener(event -> {
            actionButton.setAction(event.getValue());
        });
        supplierSelect.setValue(basicSlowAction);
        add(supplierSelect);

        // Optionally you can provide an Executor that will be used to run the task
        // nonBlockingTaskButton.setExecutor(Executors.newSingleThreadExecutor());

        // The task can also return a CompletableFuture
        //nonBlockingTaskButton.setCompletableFutureAction(() -> computeSlowString());

        builtInProgressbar.addValueChangeListener(event -> {
            actionButton.setShowProgressBar(event.getValue());
        });

        actionButton.setPreUiUpdate(() -> {
            // In this task one can modify UI, this task is optional
            if (showNotificationOnStart.getValue()) {
                Notification.show("Starting the task...");
            }
            if (showDialog.getValue()) {
                taskInProgressDialog.open();
            }
            if (disableUI.getValue()) {
                UI.getCurrent().setEnabled(false);
            }
            if (estimate.getValue()) {
                if(builtInProgressbar.getValue()) {
                    actionButton.setEstimatedDuration(4000);
                }
                progressBarInDialog.setMax(4000);
                progressBarInDialog.animateToEstimate();
                progressBarInDialog.setIndeterminate(false);
            }
            if(builtInProgressbar.getValue()) {
                actionButton.setShowProgressBar(builtInProgressbar.getValue());
            }
        });
        actionButton.setPostUiUpdate(s -> {
            // In this task one can modify UI
            VNotification.prominent("Slow string:" + s);
            taskInProgressDialog.close();
            if (disableUI.getValue()) {
                UI.getCurrent().setEnabled(true);
            }

        });

        add(actionButton);

        taskInProgressDialog.add(new Button("Cancel", event -> {
            actionButton.getCompletableFuture().cancel(true);
            taskInProgressDialog.close();
        }));

        add(new RichText().withMarkDown("""
                ## UIFuture

                UIFuture is a bit lower level helper class that makes it easy to run a long running task and update the 
                UI when the. It is also used by the ActionButton, but not all long running tasks are triggered by a 
                button click. With this helper you can trigger it from anywhere in your code.
                
                Features:
                
                * Builds on [CompletableFuture](https://www.baeldung.com/java-completablefuture), so you can 
                  (optionally) get the result of your long running action for further processing. Slow processing can 
                  also be provided as a [Supplier](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/Supplier.html) or a [Runnable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Runnable.htmll).
                * The helper takes care of the UI locking for you, as long as you use the non-async hooks in the
                  returned CompletableFuture.
                * The helper automatically enables polling if server-push is not configured.
                * In case you happen to be using the "manual push mode", it will automatically push the UI updates.
                
                You'll provide the action for UIFuture as an existing CompletableFuture, Supplier or as a Runnable. The
                helper returns a CompletableFuture for which you can safely hook UI actions (just avoid the async
                methods, or you'll need to tackle UI locking yourself).
                
                Below you can see a simple demo and code example of triggering actions from a Select using UIFuture.
                """));

        add(new Paragraph("Instantiate UI future early, in a normal UI code; it locks itself to the current UI:"));
        add(new CodeSnippet(getClass(), "instantiatingUIFuture"));
        add(new Paragraph("Here we are using the most trivial API (Runnable). For the returned CompletableFuture, you" +
                "can safely add UI modifications. Check full source code for Supplier and CompletableFuture examples."));
        add(new CodeSnippet(getClass(), "UIFutureRunnable"));

        // CodeSnippet: instantiatingUIFuture
        // UI is tied with UI.getCurrent() when instantiated by default, but you can also explicitly provide it
        UIFuture uiFuture = new UIFuture();
        // CodeSnippetEnd: instantiatingUIFuture

        // You can set the executor for the task, but most often not needed, JVM defaults are good for most cases
        // uiFuture.setExecutor(Executors.newSingleThreadExecutor());

        enum SlowAction {
            Runnable,
            Supplier,
            CompletableFuture
        }

        var slowActions = new EnumSelect<>(SlowAction.class);
        slowActions.setLabel("Select a slow action to run:");
        slowActions.addValueChangeListener(event -> {
            Notification.show("Starting " + event.getValue().name() + "...");
            switch (event.getValue()) {
                case Runnable:
                    // CodeSnippet: UIFutureRunnable
                    uiFuture.runAsync(() -> {
                        // This is where you heavy lifting should be done, we'll be slacking for 3 seconds
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        // Modifying UI here would fail, use thenRun or thenAccept to update UI
                    }).thenRun(() -> {
                        // Look ma, no dance with UI.access needed!
                        VNotification.prominent("Runnable done!");
                        // You can chain multiple UI update actions here, but DO NOT use the
                        // async version methods, or if you do, do the dance!
                    });
                    // CodeSnippetEnd: UIFutureRunnable
                    break;
                case Supplier:
                    // You can also supply a value for the UI update
                    uiFuture.supplyAsync(() -> slowService.slowBlockingMethod(3000))
                            .thenAccept(str -> VNotification.prominent("Supplier done, value: " + str));
                    break;
                case CompletableFuture:
                    // Or same with a CompletableFuture
                    uiFuture.of(slowService.generateStringAsync(3000))
                            .thenAccept(str -> VNotification.prominent("CompletableFuture done, value: " + str));
                    break;
            }
        });
        add(slowActions);
    }
}
