## Bulk Update

This demo shows how to implement a common pattern where rows are selected and then bulk updated. This is accomplished by putting a form around a table, with checkboxes in the table.

{% include snippets/bulk_update0.md %}

{% include examples/bulk_update_handler.html %}

We use the **.htmx-settling** class to flash the rows when they change status

```
.htmx-settling tr.Inactive {
  background: lightcoral;
  transition: all 0s;
}
.htmx-settling tr.Active {
  background: darkseagreen;
  transition: all 0s;
}
tr {
  transition: all 1.2s;
}
```

{% include footer.html %}
{% include outer.html %}
