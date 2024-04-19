# SimpleUI

Clojure backend for for [htmx](https://htmx.org/).  Previously known as ctmx.

- [Rationale](#rationale)
- [Getting started](#getting-started)
- [Usage](#usage)
    * [component stack](#component-stack)
    * [ids and values](#ids-and-values)
    * [Component Arrays](#component-arrays)
    * [Parameter Casting](#parameter-casting)
    * [Additional Parameters](#additional-parameters)
    * [Commands](#commands)
    * [Action at a distance (hx-swap-oob)](#action-at-a-distance--hx-swap-oob-)
    * [Responses](#responses)
    * [Script Responses](#script-responses)
    * [Hanging Components](#hanging-components)
    * [Extra hints](#extra-hints)
- [Advanced Usage](#advanced-usage)
- [Testing](#testing)
- [License](#license)

## Rationale

[htmx](https://htmx.org/) enables web developers to create powerful webapps without writing any Javascript.  Whenever `hx-*` attributes are included in html the library will update the dom in response to user events.  The architecture is simpler and pages load more quickly than in Javascript-oriented webapps.

SimpleUI is a backend accompaniment which makes htmx even easier to use.  It works in conjunction with [hiccup](https://weavejester.github.io/hiccup/) for rendering and [reitit](https://cljdoc.org/d/metosin/reitit/0.5.10/doc/introduction) for routing.

## Getting started

Getting started is easy with clojure tools and the excellent [kit](https://kit-clj.github.io) framework.

```bash
clojure -Ttools install com.github.seancorfield/clj-new '{:git/tag "v1.2.381"}' :as new
clojure -Tnew create :template io.github.kit-clj :name yourname/guestbook
cd guestbook
make repl
```

```clojure
(kit/sync-modules)
(kit/install-module :kit/simpleui)
```

Quit the process, `make repl` then

```clojure
(go)
```

Visit [localhost:3000](http://localhost:3000).  To reload changes

```clojure
(reset)
```

## Usage

First require the library

```clojure
(require '[simpleui.core :refer :all])
```

The core of SimpleUI is the `defcomponent` macro.

```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])
```

This defines an ordinary function which also expands to an endpoint `/hello`.

To use our endpoint we call `make-routes`

```clojure
;; make-routes generates a reitit handler with the root page at /demo
;; and all subcomponents on their own routes
(make-routes
  "/demo"
  (fn [req]
    (page ;; page renders the rest of the page, htmx script etc
      [:div
       [:label "What is your name?"]
       [:input {:name "my-name" :hx-patch "hello" :hx-target "#hello"}]
       (hello req "")])))
```

![](screenshot.png)

Here the only active element is the text input.  On the input's default action (blur) it will request to `/hello` and replace `#hello` with the server response.  We are using `hello` both as a function and an endpoint.  When called as an endpoint arguments are set based on the http parameter `my-name`.

**The first argument to defcomponent is always the req object**

### component stack

SimpleUI retains a call stack of nested components.  This is used to set ids and values in the sections below.

### ids and values

In the above example we use a fixed id `#hello`.  If a component exists multiple times you may set `id` automatically.

```clojure
[:div.my-component {:id id} ...]
```

SimpleUI also provides optional `path` and `value` functions.

```clojure
[:input {:type "text" :name (path "first-name") :value (value "first-name")}]
[:input {:type "text" :name (path "last-name") :value (value "last-name")}]
```

These are unique for each instance of a component and make it easy to retain state over stateless http requests.

**Note:** `path` and `value` only work when `id` is set at the top level of the component.  SimpleUI uses `id` to record the position of the component in the component stack.

### Component Arrays

If you are using the component stack on a page, you must invoke `simpleui.rt/map-indexed` instead of `clojure.core/map`.
This is because the index of the array forms part of the component stack.

```clojure
(def data [{:first-name "Fred" :last-name "Smith"}
           {:first-name "Ocean" :last-name "Leader"}])

(defcomponent table-row [req index first-name last-name]
  [:tr ...])

...

[:table
  (rt/map-indexed table-row req data)]
```

### Parameter Casting

htmx submits all parameters as strings.  It can be convenient to cast parameters to the required type

```clojure
(defcomponent my-component [req ^:long int-argument ^:boolean boolean-argument] ...)
```

You may also cast within the body of `defcomponent`

```clojure
[:div
  (if ^:boolean (value "grumpy")
    "Cheer up!"
    "How are you?")]
```

Casts available include the following

- **^:long** Casts to long
- **^:long-option** Casts to long (ignores empty string)
- **^:double** Casts to double
- **^:double-option** Casts to double (ignores empty string)
- **^:longs** Casts to array of longs
- **^:doubles** Casts to array of doubles
- **^:array** Puts into an array
- **^:boolean** True when `(contains? #{"true" "on"} argument)`.  Useful with checkboxes.
- **^:boolean-true** True when `(not= argument "false")`
- **^:edn** Reads string into edn
- **^:keyword** Casts to keyword
- **^:nullable** Ensures the strings "", "nil" and "null" are parsed as nil
- **^:trim** Trims string and sets it to nil when empty

### Additional Parameters

In most cases htmx will supply all required parameters.  If you need to include extra ones, set the `hx-vals` attribute.  To serialize the map as json in initial page renders, you should call `simpleui.render/walk-attrs` on your returned html body ([example](https://github.com/whamtet/ctmx-demo/blob/57f9b3c55c8088dc5136b10f5ce1d66e9f6bd152/src/clj/htmx/render.clj#L32)).

```clojure
[:button.delete
  {:hx-delete "trash-can"
   :hx-vals {:hard-delete true}}
   "Delete"]
```

### Commands

Commands provide a shorthand to indicate custom actions.

```clojure
(defcomponent ^:endpoint component [req command]
  (case command
    "print" (print req)
    "save" (save req))
  [:div
    [:button {:hx-post "component:print"} "Print"]
    [:button {:hx-post "component:save"} "Save"]])
```

`command` will be bound to the value after the colon in any endpoints.

### Action at a distance (hx-swap-oob)

Best to avoid, but sometimes too convenient to resist.  htmx provides the `hx-swap-oob` attribute for updating multiple dom elements within a single response.  In SimpleUI we must only provide the additional elements when htmx is updating, not in the initial render

```clojure
(defcomponent my-component [req]
  (list
    (when top-level?
      [:div.side-element
       {:id (path "path/to/side-element")
        :hx-swap-oob "true"}
        ...])
    [:div.main-element {:id id} ...]))
```

Be very careful to only include `hx-swap-oob` elements when `top-level?` is true.

### Responses

By default SimpleUI expects components to return hiccup vectors which are rendered into html.

`nil` returns http **204 - No Content** and htmx will not update the dom.

You may also return an explicit ring map if you wish.  A common use case is to refresh the page after an operation is complete

```clojure
(defcomponent my-component [req]
  (case (:request-method req)
    :post
    (do
      (save-to-db ...)
      simpleui.response/hx-refresh)
    :get ...))
```

`simpleui.response/hx-refresh` sets the "HX-Refresh" header to "true" and htmx will refresh the page.

### Script Responses

htmx will execute any script tags you include.

```clojure
[:script "alert('Application successful')"]
```

You can also mix scripts with visual content.

### Hanging Components

If you don't include components in an initial render, reference them as symbols so they are still available as endpoints.

```clojure
(defcomponent ^:endpoint next-month [req] [:p "next-month"])
(defcomponent ^:endpoint previous-month [req] [:p "previous-month"])

(defcomponent ^:endpoint calendar [req]
              next-month
              previous-month
              [:div#calendar ...])
```

### Extra hints

htmx does not include disabled fields when submitting requests.  If you wish to retain state in this case use the following pattern.

```clojure
[:input {:type "text" :name (path "input") :value (value "input") :disabled disabled?}]
(when disabled?
  [:input {:type "hidden" :name (path "input") :value (value "input")}])
```

## Advanced Usage

SimpleUI makes it possible to build dynamic forms, for details please see [advanced usage](doc/advanced_usage.md).

## Testing

```clojure
lein auto test
```

Integration tests are run with puppeteer against the [demo](demo) subproject.

```clojure
cd demo
clj -M:run
```

In a separate tab

```clojure
cd test-integration
npm i
node index.js
```

## License

Copyright © 2023 Matthew Molloy

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
