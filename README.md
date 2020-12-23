# clj-htmx

Backend helpers for [htmx](https://htmx.org/).

## Rationale

[htmx](https://htmx.org/) enables web developers to create powerful webapps without writing any javascript.  Whenever `hx-*` attributes are included on the webpage the library will update the dom in response to user events.  The architecture is simpler and pages load more quickly than in javascript oriented webapps.

clj-htmx is a backend accompaniment which makes htmx even easier to use.  It works in conjunction with [hiccup](https://weavejester.github.io/hiccup/) for rendering and [reitit](https://cljdoc.org/d/metosin/reitit/0.5.10/doc/introduction) for routing.

## Demo

Clone and run [clj-htmx-demo](https://github.com/whamtet/clj-htmx-demo).

## Usage

First require the library

```clojure
(require '[clj-htmx.core :refer :all])
```

The core of clj-htmx is the `defendpoint` macro.

```clojure
(defendpoint hello [req my-name]
  [:div#hello "Hello " my-name])
```

This defines an ordinary function which also expands to an endpoint `/hello`.

To use our endpoint we call `make-routes`

```clojure
(make-routes
  "/demo"
  (fn [req]
    (render/html5-response
      [:div {:style "padding: 10px"}
        [:label {:style "margin-right: 10px"}
          "What is your name?"]
        [:input {:type "text"
                 :name "my-name"
                 :hx-patch "hello"
                 :hx-target "#hello"
                 :hx-swap "outerHTML"}]
         (hello req "")])))
```

![](screenshot.png)

Here the only active element is the text input.  On the input's default action (blur) it will request to `/hello` and replace `#hello` with the server response.  We are using `hello` both as a function and an endpoint.  When called as an endpoint arguments are set based on the html `name` attribute.

**The first argument to defendpoint is always the req object**

### Responses

If `defendpoint` returns `nil` it will be wrapped in **204 - No Content** and htmx will not update the dom.

towrite other response examples

### Casting

Because htmx submits parameters as strings it can be convenient to cast `defendpoint` arguments e.g.

```clojure
(defendpoint my-endpoint [req ^:int integer-argument ^:boolean boolean-argument])
```

Supported casts include

* `^:int` Integer
* `^:lower` Lower-case string
* `^:trim` Trimmed string
* `^:boolean` True when argument = "true" or "on" (for checkboxes).

### defcomponent

Nested calls to `defendpoint` must also be `defendpoint` to macroexpand correctly.  If you do not wish to expose intermediate functions as endpoints use `defcomponent` instead.

### handling nested data

towrite

## License

Copyright © 2020 Matthew Molloy

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
