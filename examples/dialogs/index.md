## Dialogs

Dialogs can be triggered with the [hx-prompt](https://htmx.org/attributes/hx-prompt) 
and [hx-confirm](https://htmx.org/attributes/hx-confirm) attributes. 
These are triggered by the user interaction that would trigger the AJAX request, 
but the request is only sent if the dialog is accepted.
---
{% include examples/dialogs_handler.html %}

{% include snippets/dialogs0.md %}

{% include footer.html %}
{% include zero_outer.html %}