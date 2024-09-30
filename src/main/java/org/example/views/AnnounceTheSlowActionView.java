package org.example.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import org.example.CodeSnippet;
import org.example.DefaultLayout;
import org.example.SlowService;
import org.vaadin.firitin.appframework.MenuItem;
import org.vaadin.firitin.components.RichText;
import org.vaadin.firitin.components.progressbar.VProgressBar;

@Route(layout = DefaultLayout.class)
@MenuItem(title = "Announce it is slow", icon = VaadinIcon.ALARM, order = 1010)
public class AnnounceTheSlowActionView extends VerticalLayout {

    public AnnounceTheSlowActionView(SlowService slowService) {
        add(new H1("Let the user know it is slow!"));
        add(new RichText().withMarkDown("""
                A slight improvement to the previous example is to inform the user that the action is slow. Something as 
                simple as a notification or an indeterminate progressbar can make a big difference in the UX. You might also
                want to hide or disable some UI components while the action is ongoing.
                            
                A tiny problem is that Vaadin synchronises UI changes only after activated listeners are executed. 
                This can be overcome by a tiny hack: an empty JS execution to cause an additional server round-trip.
                """));

        add(new CodeSnippet(getClass(), "executeJsHack"));

        int componentIndex = getComponentCount();
        add(new Button("Run a slow action", e -> {
            // CodeSnippet: executeJsHack
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            addComponentAtIndex(componentIndex, progressBar);

            // 🤬You must know Matti gets angry if he sees getElement() call in the application code
            // Hide this into your own helper or check the next examples...
            getElement().executeJs("").then(e2 -> {
                // Built-in loading indicator will be shown because next function will take 5 secs to execute
                String result = slowService.slowBlockingMethod(5000);
                Notification.show(result);
                remove(progressBar);
            });
            // CodeSnippetEnd: executeJsHack
        }));

        add(new RichText().withMarkDown("""
                Viritin's VProgressBar has a helper that...
                
                 * removes the code smell of the *getElement().executeJs("")* call from your application code. 
                 * automatically removes the progressbar from the UI when the action is finished
                 * .. and makes sure the frameworks built-in indicator is not shown (would look bit broken with two 
                   indicators).
                """));
        add(new CodeSnippet(getClass(), "vProgressBar"));

        var executionResults = new Div();
        add(executionResults);
        add(new Button("Run a slow action with VProgressBar", e -> {
            // CodeSnippet: vProgressBar
            executionResults.add(VProgressBar.indeterminateForTask(() -> {
                // The progressbar is visible only during this task; framework indicator hidden
                String str = slowService.slowBlockingMethod(5000);
                executionResults.add(new Paragraph(str));
            }).withMinWidth("100px"));
            // CodeSnippetEnd: vProgressBar
        }));

        add(new RichText().withMarkDown("""
                I can already promise your users will be happier with this kind of tiny enhancements, but the UI updates are
                still blocked *during the action* and UI events are not handled until the action is done.
                """));

    }
}
