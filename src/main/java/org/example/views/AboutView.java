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
processing in a Vaadin Flow application. UI examples are executed against an example 
[backend service](https://github.com/mstahv/long-running-actions/blob/main/src/main/java/org/example/SlowService.java) 
that simulates slow operations.

Some of the examples utilise the [Viritin](https://vaadin.com/directory/component/flow-viritin) library that provides 
some handy utilities for Vaadin Flow developers. Principles and patterns shown in the examples are however applicable to any 
Vaadin Flow applications and even to non-Vaadin applications.

To see the full source of the example view, use the link in top right corner. If you want to contribute your example or just try this hands-on with IDE [check it out from
GitHub](https://github.com/mstahv/long-running-actions).

                """));
    }
}
