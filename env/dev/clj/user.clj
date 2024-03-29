(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [rate-that-drink.config :refer [env]]
   [clojure.pprint]
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [rate-that-drink.core :refer [start-app]]
   [rate-that-drink.db.core]
   [conman.core :as conman]
   [luminus-migrations.core :as migrations]
   [shadow.cljs.devtools.server :as server]
   [shadow.cljs.devtools.api :as shadow]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'rate-that-drink.core/repl-server))

(defn stop
  "Stops application."
  []
  (mount/stop-except #'rate-that-drink.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db
  "Restarts database."
  []
  (mount/stop #'rate-that-drink.db.core/*db*)
  (mount/start #'rate-that-drink.db.core/*db*)
  (binding [*ns* 'rate-that-drink.db.core]
    (conman/bind-connection rate-that-drink.db.core/*db* "sql/queries.sql")))

(defn reset-db
  "Resets database."
  []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate
  "Migrates database up for all outstanding migrations."
  []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback
  "Rollback latest database migration."
  []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration
  "Create a new up and down migration file with a generated timestamp and `name`."
  [name]
  (migrations/create name (select-keys env [:database-url])))

(defn cljs-repl
  ([]
   (cljs-repl :app))
  ([build-id]
   (server/start!)
   (shadow/watch build-id)
   (shadow/nrepl-select build-id)))
