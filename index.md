SimpleUI is an app development tool for fast product development and even faster page load times.  It uses [htmx](https://htmx.org/) on the frontend.

## Basic example

{% include snippets/hello0.md %}
{% include examples/demo.html %}
---
The core of SimpleUI is the `defcomponent` macro which expands to both:

- An ordinary function
- A rest endpoint.  Arguments are bound based on the http parameter `my-name`.

`defcomponent` enables developers to quickly build rich user interfaces with _no_ javascript.  All code is on the server backend and yet it feels the same as frontend code.

## Getting started

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

## Architecture

{% include architecture.html %}

SimpleUI uses Hypermedia as the Engine of Application State ([HATEOAS](https://en.wikipedia.org/wiki/HATEOAS)), the web as it was originally supposed to be.  Application state is implicitly stored in the html itself, not in a separate javascript layer.  By extending the original html model instead of building a javascript layer over top, we get simplicity and **much** faster page load times.

## Casting parameters

{% include examples/parameter_casting.html %}
---
{% include snippets/hello3.md %}

SimpleUI uses native html forms, so data is submitted as strings.  We can cast it as necessary.  Supported casts include **^:long**, **^:boolean** and **^:double**. See [documentation](https://github.com/whamtet/simpleui#parameter-casting) for details.

We may also cast within the body of `defcomponent`.

```clojure
[:div
  (if ^:boolean (value "grumpy")
    "Cheer up!"
    "How are you?")]
```

## Further reading

Please see the [examples](examples) or [full documentation](https://github.com/whamtet/simpleui).

{% include footer.html %}
{% include zero_outer.html %}
