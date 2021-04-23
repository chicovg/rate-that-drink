(ns rate-that-drink.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [rate-that-drink.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[rate-that-drink started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[rate-that-drink has shut down successfully]=-"))
   :middleware wrap-dev})
