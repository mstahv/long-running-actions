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

@Route(layout = DefaultLayout.class)
@MenuItem(title = "Built-in Indicator", icon = VaadinIcon.AMBULANCE)
public class FrameworksActivityDialogView extends VerticalLayout {

    public FrameworksActivityDialogView(SlowService slowService) {
        add(new H1("Let the framework deal it!"));
        add(new RichText().withMarkDown("""
            Vaadin Flow has built-in support for handling slow tasks in the UI. In case a server visit seems to
            take a lot fo time, the client-side will show a loading indicator.
            
            While this approach is very convenient for the developer, it has certain limitations.

             * The UX can't be optimal. The user might not understand why the loading indicator is shown
                or what is happening, they might even go and reload the page when thinking the UI got stuck.
             * The progress indicator is not very informative. There is "some progress" in it, but in reality
               it is a pure guess by the framework and essentially only an indeterminate progressbar.
             * The indicator is not tied to the part of UI that initiated the long running task.
             * The user cannot cancel the operation.
             * Other UI updates are stalled during the long running task.
             * Some UI events are queued on the client side and executed after the long running task is completed (may 
               or may not be a good thing).
            """));

        add(new CodeSnippet(getClass(), 41, 45));

        add(new Button("Start 5000ms task", e -> {
            // This calling slow service method automatically shows frameworks loading indicator
            String result = slowService.slowBlockingMethod(5000);
            // Loading indicator is automatically closed after the execution is done
            Notification.show(result);
        }));

    }
}
