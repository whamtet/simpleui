In 2020 Carson Gross released [HTMX](https://htmx.org), which takes a radically different approach to web development than other frameworks.
Instead of programming against a Javascript API, we extend the underlying HTML syntax and eliminate most of our JS.
The default pattern is that each element on the page updates itself, if you click

```html
<div hx-get="my-endpoint">Click me!</div>
```

"my-endpoint" can reply with

```html
<div>You clicked me!</div>
```

Which will replace the original div.  This lends itself naturally to the following pattern

```clojure
(defcomponent ^:endpoint my-div [req]
  (if top-level?
    [:div "You clicked me!"]
    [:div {:hx-get "my-div"} "Click me!"]))
```
We create a pseudo function that also exposes an endpoint to update state.  This deceptively simple approach eliminates a lot of the complexity in JS-oriented web development.  We no longer need

* Frontend state management e.g. redux
* Code to connect events to actions
* A frontend client
* A backend API which is only used by a single frontend
* Javascript compilation / minification.

There is a natural synergy between UI updates and the stateless nature of http, all our state is transmitted with each request.

```clojure
(defcomponent ^:endpoint login-modal [req first-name last-name]
  [:form {:hx-get "login-modal"}
   [:input {:name "first-name" :value first-name}]
   [:input {:name "last-name" :value last-name}]
   [:span (format "Register as %s %s?" first-name last-name)]
   ...])
```

This contrasts with JS-oriented webapps which have to bolt-on a lot of functionality already provided natively by the browser.  We get two big benefits.

* 30 - 40% reduction in frontend development time.
* 90 - 99% reduction in page load size.

The reduction in page load size is particularly important for respecting user experience and channelling prospects into a sales funnel.

## Drawbacks

So what are the drawbacks?  The most obvious is that we are now round tripping to the server for every simple UI update.  We can alleviate this slightly with a sprinkling of Javascript for very simple state changes e.g. switching tabs.  However round-tripping is less concern than you might think because of the nature of UI updates in general.  Typically when we change the UI we are either loading new content or saving changes.  In both cases a frontend app will enter into a transitionary loading state while it pends on a backend request.

![Loading...](loading.png)

Since we're waiting for the backend anyway, why not render there as well?  The limitation of backend rendering is more obvious when you have interdependencies between components on the page.  Consider a BMI calculator that requests a user's height and weight.  It is possible to draw a dependency tree between components.

![BMI](deps.png)

A change in either height or weight triggers an update in BMI as well.  Once the number of nodes requiring an update exceeds 2 or 3 it is easier to render with Javascript directly.  In practice however, only a small percentage of apps reach this level of complexity.  The rest of us should be fine with the [hypertext approach](https://en.wikipedia.org/wiki/HATEOAS).
