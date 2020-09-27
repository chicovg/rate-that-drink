(ns the-beer-tasting-app.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[the-beer-tasting-app started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[the-beer-tasting-app has shut down successfully]=-"))
   :middleware identity})
