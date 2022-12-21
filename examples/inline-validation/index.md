## Inline Validation

This example shows how to do inline field validation, in this case of an email address.

{% include snippets/inline_validation0.md %}

{% include examples/inline_validation_handler.html %}
---
This form can be lightly styled with this CSS:

    .error-message {
      color:red;
    }
    .error input {
      box-shadow: 0 0 3px #CC0000;
    }
    
    .htmx-request + .htmx-indicator {
      opacity: 1;
    }

{% include footer.html %}
{% include zero_outer.html %}