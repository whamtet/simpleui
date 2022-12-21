## Tabs (Using HATEOAS)

This example shows how easy it is to implement tabs using ctmx. 
Following the principle of [Hypertext As The Engine Of Application State](https://en.wikipedia.org/wiki/HATEOAS),
the selected tab is a part of the application state. 
Therefore, to display and select tabs in your application, simply include the tab markup in the returned HTML.

{% include snippets/tabs_hateoas0.md %}
{% include examples/tabs_hateoas_handler.html %}

## Tabs (Using Hyperscript)
Tabs are a good example of a static component.  
We can use [hyperscript](https://hyperscript.org/) instead of server requests for increased performance.

{% include snippets/tabs_hateoas1.md %}
Try clicking the second set of tabs and notice the performance difference.
{% include examples/tabs_hateoas_handler2.html %}

{% include footer.html %}
{% include zero_outer.html %}
{% include hyperscript.html %}
