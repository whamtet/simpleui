# SimpleUI

Clojure backend for [htmx](https://htmx.org/).  Previously known as ctmx.

<!-- TOC start (generated with https://github.com/derlin/bitdowntoc) -->

- [Rationale](#rationale)
- [Getting started](#getting-started)
- [Usage](#usage)
  * [Authentication, IAM](#authentication-iam)
  * [Parameter Casting](#parameter-casting)
  * [Additional Parameters](#additional-parameters)
  * [Commands](#commands)
  * [top-level?](#top-level)
  * [Updating multiple components](#updating-multiple-components)
  * [Responses](#responses)
  * [Updating Session](#updating-session)
  * [Script Responses](#script-responses)
  * [Unsafe HTML](#unsafe-html)
  * [Hanging Components](#hanging-components)
  * [si-set, si-clear](#si-set-si-clear)
  * [Using SimpleUI from a CDN](#using-simpleui-from-a-cdn)
  * [Extra hints](#extra-hints)
- [Advanced Usage](#advanced-usage)
- [Pros and Cons of SimpleUI](#pros-and-cons-of-simpleui)
- [Testing](#testing)
- [License](#license)

<!-- TOC end -->

<!-- TOC --><a name="rationale"></a>
## Rationale

[htmx](https://htmx.org/) enables web developers to create powerful webapps without writing any Javascript.  Whenever `hx-*` attributes are included in html the library will update the dom in response to user events.  The architecture is simpler and pages load more quickly than in Javascript-oriented webapps.

SimpleUI is a backend accompaniment which makes htmx even easier to use.  It works in conjunction with [hiccup](https://weavejester.github.io/hiccup/) for rendering and [reitit](https://cljdoc.org/d/metosin/reitit/0.5.10/doc/introduction) for routing.

<!-- TOC --><a name="getting-started"></a>
## Getting started

Add the following dependency to your `deps.edn` file:

    io.simpleui/simpleui {:mvn/version "1.6.0"}

Or to your Leiningen `project.clj` file:

    [io.simpleui/simpleui "1.6.0"]

Getting started is easy with clojure tools and the excellent [kit](https://kit-clj.github.io) framework.

```bash
clojure -Ttools install com.github.seancorfield/clj-new '{:git/tag "v1.2.404"}' :as new
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

<!-- TOC --><a name="usage"></a>
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

<!-- TOC --><a name="authentication-iam"></a>
### Authentication, IAM

You may check a user's permissions inside the component, however for page level checks remember that `make-routes` is just generating reitit vectors

```clojure
(make-routes
  "/demo"
  (fn [req] ...))

;; returns
;; ["/demo"
;;   ["/my-component1" my-component1]
;;   ["/my-component2" my-component2]
;; ...]
```

You can attach page level checks using [standard Reitit techniques](https://github.com/metosin/reitit).

<!-- TOC --><a name="parameter-casting"></a>
### Parameter Casting

htmx submits all parameters as strings.  It can be convenient to cast parameters to the required type

```clojure
(defcomponent my-component [req ^:long int-argument ^:boolean boolean-argument] ...)
```

Casts available include the following

- **^:long** Casts to long
- **^:long-option** Casts to long (ignores empty string)
- **^:double** Casts to double
- **^:double-option** Casts to double (ignores empty string)
- **^:longs** Casts to array of longs
- **^:doubles** Casts to array of doubles
- **^:array** Puts into an array
- **^:set** Puts into a set
- **^:boolean** True when `(contains? #{"true" "on"} argument)`.  Useful with checkboxes.
- **^:boolean-true** True when `(not= argument "false")`
- **^:edn** Reads string into edn
- **^:keyword** Casts to keyword
- **^:nullable** Ensures the strings "", "nil" and "null" are parsed as nil
- **^:trim** Trims string and sets it to nil when empty
- **^:json** Parses json
- **^:prompt** Takes value from `hx-prompt` header

<!-- TOC --><a name="additional-parameters"></a>
### Additional Parameters

In most cases htmx will supply all required parameters.  If you need to include extra ones, set the `hx-vals` attribute.  To serialize the map as json on initial render walk the body with `simpleui.render/walk-attrs` ([example](https://github.com/whamtet/simpleui/blob/main/demo/src/clj/demo/middleware/formats.clj#L32)).

```clojure
[:button.delete
  {:hx-delete "trash-can"
   :hx-vals {:hard-delete true}}
   "Delete"]
```

<!-- TOC --><a name="commands"></a>
### Commands

Commands provide a shorthand to indicate custom actions.

```clojure
(defcomponent ^:endpoint component [req command]
  (case command
    "print" (print req)
    "save" (save req)
    nil)
  [:div
    [:button {:hx-post "component:print"} "Print"]
    [:button {:hx-post "component:save"} "Save"]])
```

`command` will be bound to the value after the colon in any endpoints.

<!-- TOC --><a name="top-level"></a>
### top-level?

SimpleUI sets `top-level?` true when a component is being invoked as an endpoint.

```clojure
(defcomponent ^:endpoint my-component [req]
  (if top-level?
    [:div "This is an update"]
    [:div "This is the original render"]))
```

<!-- TOC --><a name="updating-multiple-components"></a>
### Updating multiple components

When you return multiple components as a list, SimpleUI will set [hx-swap-oob](https://htmx.org/attributes/hx-swap-oob/) on all but the last.  Those elements will be swapped in by id at various points on the page.

```clojure
(defcomponent my-component [req]
  (list
   ;; update these as well
   [:div#title ...]
   [:div#sidebar ...]
   ;; main element
   [:div.main-element {:id id} ...]))
```

Be careful to only include `hx-swap-oob` elements when `top-level?` is true.

<!-- TOC --><a name="responses"></a>
### Responses

By default SimpleUI expects components to return hiccup vectors which are rendered into html.

`nil` returns http **204 - No Content** and htmx will not update the dom.

You may also return an explicit ring map if you wish.  A common use case is to refresh the page after an operation is complete

```clojure
(defcomponent ^:endpoint my-component [req]
  (case (:request-method req)
    :post
    (do
      (save-to-db ...)
      simpleui.response/hx-refresh)
    :get ...))
```

`simpleui.response/hx-refresh` sets the "HX-Refresh" header to "true" and htmx will refresh the page.

<!-- TOC --><a name="updating-session"></a>
### Updating Session

When a component returns a response map without a `body` key SimpleUI assumes it is a session update and wraps the response in **204 - No Content**.

```clojure
(defcomponent ^:endpoint my-component [req shopping-item]
  (update session :cart conj shopping-item))
```

The response won't update anything on the page, but the session will be updated.

<!-- TOC --><a name="script-responses"></a>
### Script Responses

htmx will execute any script tags you include.

```clojure
[:script "alert('Application successful')"]
```

You can also mix scripts with visual content.  Once you're inside Javascript you can invoke SimpleUI with the HTMX commands [ajax](https://htmx.org/api/#ajax) and [trigger](https://htmx.org/api/#trigger).

<!-- TOC --><a name="unsafe-html"></a>
### Unsafe HTML

The default hiccup rendering mode blocks HTML strings from being inserted into the DOM.  If you need this disable render-safe

```clojure
(simpleui.config/set-render-safe false)
```

<!-- TOC --><a name="hanging-components"></a>
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

<!-- TOC --><a name="si-set-si-clear"></a>
### si-set, si-clear

SimpleUI contains complex state in forms.  On wizards and multistep forms some elements may disappear while we still wish to retain the state.
To handle this situation create a 'stack' of hidden elements on initial page render

```clojure
[:input#first-name {:type "hidden"}]
[:input#second-name {:type "hidden"}]
...
```

When you proceed from one form to the next you may push onto the stack

```clojure
[:button {:hx-post "next-step"
          :si-set [:first-name :second-name]
          :si-set-class "my-stack"}]
```

`si-set` will [oob-swap](https://htmx.org/attributes/hx-swap-oob/) `first-name` and `second-name` into the hidden `#first-name` and `#second-name` inputs
and set their class to `my-stack`.  If we wish to return to this step in the wizard pop `first-name` and `second-name` back off the stack.

```clojure
[:button {:hx-post "previous-step"
          :hx-include ".my-stack"
          :si-clear [:first-name :second-name]}]
```

`hx-include` class selects the `first-name` and `second-name` fields when rendering `previous-step` and `si-clear` clears the stack.  It is important to clear the stack because multiple inputs with the same name become an array which you may not be expecting.

<!-- TOC --><a name="using-simpleui-from-a-cdn"></a>
### Using SimpleUI from a CDN

You will find that SimpleUI is already very fast and lightweight compared to JS-oriented frameworks, sometimes it is convenient to push that even further.
You can bootstrap into SimpleUI from a CDN page by referencing HTMX and including a hidden element that triggers the backend.

```html
<div hx-get="https://my-backend.com" hx-trigger="load" />
<script src="https://unpkg.com/htmx.org@1.9.12"></script>
```

<!-- TOC --><a name="extra-hints"></a>
### Extra hints

htmx does not include disabled fields when submitting requests.  If you wish to retain state in this case use the following pattern.

```clojure
[:input {:type "text" :name (path "input") :value (value "input") :disabled disabled?}]
(when disabled?
  [:input {:type "hidden" :name (path "input") :value (value "input")}])
```

<!-- TOC --><a name="advanced-usage"></a>
## Advanced Usage

SimpleUI makes it possible to build dynamic forms, for details please see [advanced usage](doc/advanced_usage.md).

<!-- TOC --><a name="pros-and-cons-of-simpleui"></a>
## Pros and Cons of SimpleUI


SimpleUI offers two big advantages over JS-oriented frameworks.  You get about a 30% saving on development time due to the simplified architecture.
No http client, routing library, state management complexity etc.  Even more importantly for users the bundle size is reduced 90 - 99%.  Initial page load is as little as 2kb, HTMX loads asyncronously while the user is absorbing page content.

The limitation of both SimpleUI and HTMX occurs when there are complex dependencies between different parts of the page.  If a change in one element triggers updates in one or two others you can [swap in oob](#updating-multiple-components), but once you do
this too much you're loading half the page with every state change.  This is the same as bad old plain HTML and you should consider switching to a JS-oriented solution.

In practice this situation is rare for business apps and even games can be developed using SimpleUI (e.g. [War of the Ring](https://wotr.online)).

<!-- TOC --><a name="testing"></a>
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

<!-- TOC --><a name="license"></a>
## License

Copyright © 2024 Matthew Molloy

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
