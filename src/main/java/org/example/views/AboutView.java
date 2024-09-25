package org.example.views;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.example.DefaultLayout;
import org.vaadin.firitin.appframework.MenuItem;
import org.vaadin.firitin.components.RichText;

@Route(value = "", layout = DefaultLayout.class)
@MenuItem(title = "About this demo", icon = VaadinIcon.QUOTE_LEFT, order = MenuItem.BEGINNING)
public class AboutView extends VerticalLayout {

    public AboutView() {
        add(new RichText().withMarkDown("""
# Handling slow actions in the UI

This is a simple demo app that shows couple of examples and tips how to tackle slow backend calls and asynchronous 
processing in a Vaadin application.
                                                                                               
To see the full source of the example view, use the link in top right corner. If you want to contribute your example or just try this hands-on with IDE [check it out from
GitHub](https://github.com/mstahv/long-running-actions).

                """));
    }
}
