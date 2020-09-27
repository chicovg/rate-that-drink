(ns the-beer-tasting-app.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [the-beer-tasting-app.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[the-beer-tasting-app started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[the-beer-tasting-app has shut down successfully]=-"))
   :middleware wrap-dev})
