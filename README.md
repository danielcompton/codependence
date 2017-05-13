The irresponsible clojure guild presents...

# codependence

a more flexible data-driven way to manage your app structure

## Why?

- we discovered shortcomings in component while building oolong
- integrant *almost* rectified them

This uses integrant internally, because it's a pretty solid base

This is my second attempt to write this library. The previous attempt
(oolong) relied on component and I ended up disliking the approach.

## How does it work?

Your service configuration takes the form of a map. Keys are keywords naming the services and values can be any clojure data.

If the value is a map, it might be a component! A component has a `:co/tag` entry which allows you to name the responsible component with a keyword:

```edn
{:foo/bar {:co/tag :myapp/bar}}
```

Here we have a component named `:foo/bar`, tagged `:myapp/bar`

We can hook the `:myapp/bar` tag like this:

```clojure
(require '[irresponsible.codependence :as c])
(defmethod c/start-tag :foo/bar
  [_ v] ;; [keyword, component data (without `:co/*` keys)]
  (do-something v))
```

When you start the system, you get back a map where the `:foo/bar` entry's value has been replaced with the result of running `(do-something)`. You are free to return whatever you like from this function. Perhaps you will return a database connection or a webserver in a real world component? Perhaps for complex scenarios starting your component might return a map of which whatever connection or handle is just one part.

There is a corresponding `stop-tag` multimethod we can hook like so:

```clojure
(require '[irresponsible.codependence :as c])
(defmethod c/stop-tag :foo/bar
  [_ v] ;; [keyword, return from start-tag]
  (stop-something v))
```

We can also refer to keys within the map either by the `#co/ref` edn tag if you are using the `c/read-string` function or via the `c/ref` function. This provides us with a means of dependency resolution:

```edn
{:foo/bar  {:co/tag :myapp/bar}
 :baz/quux {:co/tag :myapp/quux :bar #co/ref :foo/bar}}
```

Here the start-tag handler for :baz/quux will find a key `:bar` with the value of the `:foo/bar` key after the start-tag `:myapp/bar` has been run.

Here's a example that tries to make this more clear:

```edn
{:service/print {:co/tag :misc/print :value #co/ref :service/square}
 :service/square {:co/tag :math/square :input #co/ref :data/input}
 :data/input 2}
```

```clojure
(require '[irresponsible.codependence :as c])
(defmethod c/start-tag :math/square
  [_ {:keys [input]}] ; receives the value 2 (by `#co/ref`)
  (* input input)) ; returns a simple integer
(defmethod c/start-tag :misc/print
  [_ {:keys [value]}] ;; receives the value 4 (2 squard) by `#co/ref` (because :math/square has already been started)
  (prn :print-service value)
  value)
```

## Requirements

* Clojure 1.8.0 or newer
* If you're not using a spec-enabled 1.9 alpha, [future-spec](https://github.com/tonsky/clojure-future-spec)

Note: We consider the 1.9 alphas sufficiently production-ready and that the
alpha status corresponds more to the newness and unpolishedness of spec.
The 1.9 series included a ton of bugfixes and performance improvements you
might want to consider having access to.

## Real-world example

This models how we might construct a ring webapp using the aleph webserver. We hope you are familiar with ring!

Dependencies:

```clojure
[org.clojure/clojure "1.8.0"]
[clojure-future-spec "1.9.0-alpha15"]
;; OR just this if you're brave:
[org.clojure/clojure "1.9.0-alpha15"]
```

```clojure
(ns myapp.app
 (:require [irresponsible.codependence :as c]
           [myapp.routes :refer [app-routes]] ;; fictional routes!
           [myapp.middleware :refer [wrap]] ;; fictional middleware wrapper!
           [aleph.http :as http]))

(def config
  {:service/http {:port 8080 :handler (c/ref :http/handler) :co/tag :http/aleph}
   :http/handler {:handler app-routes :middleware wrap :co/tag http/make-handler
                  :middleware-args [:dev] :co/load [myapp.routes myapp.middleware]}})

;; very lightweight aleph component
(defmethod c/start-tag :http/aleph [_ {:keys [handler port]}]
 (http/start-server handler {:port port}))
(defmethod c/stop-tag :http/aleph [_ v]
  (.close v))

;; very lightweight handler middleware-wrapping component
;; we assume it takes a profile argument that indicates which
;; middleware to apply dependent on the environment it is being run in
(defmethod c/start-tag :http/make-handler
  [_ {:keys [handler middleware middleware-args]}]
  (apply middleware handler middleware-args))

(def service nil)

(defn start []
  (alter-var-root #'service
    (fn [_] (c/start! (c/read-string (slurp "config.edn"))))))

(defn stop []
  (alter-var-root #'service
    (fn [v] (c/stop! v))))

```

## Copyright and License

MIT LICENSE

Copyright (c) 2017 James Laver

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

