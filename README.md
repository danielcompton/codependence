The irresponsible clojure guild presents...

# codependence

a more flexible data-driven way to manage your app structure

## Why?

- because component has severe shortcomings
- because oolong showed the severe shortcomings of component too well
- because integrant didn't quite rectify them

This uses integrant internally, because it's a pretty solid base

This is my second attempt to write this library. The previous attempt
(oolong) relied on component and I ended up disliking the approach.

## How does it work?

It's like integrant, except instead of giving special meaning to the
keys in the map, they are simply unique identifiers and code is
invoked in response to the value being a map with special keys
(`:co/load`, `:co/tag`)

## Requirements

* Clojure 1.8.0 or newer
* If you're not using a spec-enabled 1.9 alpha, [future-spec](https://github.com/tonsky/clojure-future-spec)

Note: We consider the 1.9 alphas sufficiently production-ready and that the
alpha status corresponds more to the newness and unpolishedness of spec.
The 1.9 series included a ton of bugfixes and performance improvements you 
might want to consider having access to.

## Just show me already!

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

;; very lightweight handler middleware-wrapping component
;; we assume it takes a profile argument that indicates which
;; middleware to apply dependent on the environment it is being run in
(defmethod c/start-tag :http/make-handler
  [_ {:keys [handler middleware middleware-args]}]
  (apply middleware handler middleware-args))
```

## Copyright and License

MIT LICENSE

Copyright (c) 2017 James Laver

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

