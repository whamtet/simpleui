## Delete Row

This example shows how to implement a delete button that removes a table row upon completion.

{% include snippets/delete_row0.md %}

{% include examples/delete_row_handler.html %}

The table body has a **hx-confirm** attribute to confirm the delete action. 
It also set the target to be the **closest tr** that is, the closest table row, for all the buttons. 
The swap specification in **hx-swap** says to swap the entire target out and to wait 0.5 seconds after receiving a response. 
This is so that we can use the following CSS:

```
  tr.htmx-swapping {
      opacity: 0;
      transition: opacity 0.5s;
  }
```

{% include footer.html %}
{% include zero_outer.html %}
