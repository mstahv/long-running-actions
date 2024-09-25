package org.example.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.example.CodeSnippet;
import org.example.DefaultLayout;
import org.example.SlowService;
import org.vaadin.firitin.appframework.MenuItem;
import org.vaadin.firitin.components.RichText;
import org.vaadin.firitin.components.notification.VNotification;

@Route(layout = DefaultLayout.class)
@MenuItem(title = "Go asynchronous!", icon = VaadinIcon.ROCKET, order = 1010)
public class GoAsynchrounous extends VerticalLayout {

    public GoAsynchrounous(SlowService slowService) {
        add(new H1("Asynchronous operation keeps the UI responsive"));
        add(new CodeSnippet(getClass(), 25, 47));
        add(new Button("Run slow async action", e -> {
            Notification.show("The will take some time, please hold...");

            e.getSource().getUI().ifPresent(ui -> {
                // generateStringAsync returns a CompletableFuture<String> immediately and the UI is not blocked
                slowService.generateStringAsync(5000).thenAccept(string -> {
                    // This handler executed by a "non-UI thread" later when the string becomes available.
                    // Touching UI components directly here would fail, and thus we need to use UI.access with another
                    // callback to update the UI
                    //
                    // Warning about Component.getUI() method: While the method looks exactly what you want, calling it
                    // is not perfectly safe from non-UI threads!!
                    // To avoid mistakes with Component.getUI(), it is better to use the UI instance from some actual UI
                    // event. Here we are saving a reference from the button click event. UI.getCurrent() also works most
                    // of the time, but it only works if the execution is directly in an event listner (or UI constructor).
                    //
                    // TIP: from an AttachEvent you can get it without the ugly Optional.
                    ui.access(() -> {
                        VNotification.prominent(string);
                    });
                });
            });
        }));

        add(new RichText().withMarkDown("""
                For best possible UX, the UI should be responsive at all times. This means that the user should be able to 
                interact with the UI even when the backend is busy executing some task. User might want to do something else
                while waiting or even cancel the operation. This can be achieved by running the long running action and
                UI updates separately.

                The example leaves the UI ready for any other actions while the long running task is executed. The user can
                for example navigate to other views or initiate multiple long running tasks (Hint: use Button.setDisableOnClick
                to prevent multiple clicks on the button, if not desired).
                
                Concurrency alone can be hard, especially with low level utilities like the Thread. Example on this page uses 
                CompletableFuture (JDK's built-in abstraction to asynchronous processing) to get the value from long running 
                action and updates the UI later when it is ready.
                
                *The concurrency needs to be handled also at the UI level!* In the previous blocking example the http 
                request was alive for the duration of the long action and the return message contained the changes. With 
                asynchronous processing, the browser is not actively waiting for the response. Thus, the Vaadin 
                application needs to have "server push" enabled or to use polling, to get the updates 
                reflected in the browser. This example uses @Push annotation to enable server push in AppShell class.
                
                You'll also need an additional measure to synchronize UI updates, which typically happens with the 
                *UI.access(Runnable)* method, like in this example.
                
                ## Don't forget the user, you've got the power!
                
                Even though the UI now remains responsive, the user might not understand what is happening. Like in the
                previous step, it is still a good idea to inform the user about the long running task. In this example
                only a notification is shown, but you could also use a progress bar or some other indicator.
                
                The good thing is that **you have now a full the control over the UI** during the action. You could for 
                example provide real updates of the progress in your progressbar or a log. If your processing can be 
                stopped, you can even provide a cancel functionality, because the UI is responsive for user input. Or 
                you could "dock" users long running task(s) somewhere in the main layout (top right corner is a commonly
                used location in web apps) or open a minimizable dialog for the user to follow the progress while 
                continuing with other tasks.
                """));

    }

}
