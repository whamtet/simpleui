## Active Search
This example actively searches a contacts database as the user enters text.

{% include snippets/active_search0.md %}

The input issues a **POST** to **rows** on the keyup event and sets the body of the table to be the resulting content.

We add the **delay:500ms** modifier to the trigger to delay sending the query until the user stops typing. 
Additionally, we add the **changed** modifier to the trigger to ensure we don't send new queries when the user doesn't change the value of the input (e.g. they hit an arrow key).

Finally, we show an indicator when the search is in flight with the **hx-indicator** attribute.

{% include examples/active_search_handler.html %}
{% include footer.html %}
{% include zero_inner.html %}