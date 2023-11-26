## Sortable
In this example we show how to integrate the [Sortable](https://sortablejs.github.io/Sortable/) javascript library with htmx.

To begin we intialize the **Sortable** javascript library:

```javascript
  const sortable = htmx.find('#to-sort');
  new Sortable(sortable, {animation: 150, ghostClass: 'blue-background-class'});
```

We then trigger **POST** to **sortable** on the **end** event to persist changes (if necessary).

{% include examples/sortable_handler.html %}

{% include snippets/sortable0.md %}

{% include footer.html %}
{% include sortable.html %}
