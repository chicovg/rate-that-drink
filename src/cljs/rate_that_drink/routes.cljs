(ns rate-that-drink.routes)

(defrecord RouteInfo [key path label on-logout? on-login?])

(defn to-route [^RouteInfo {:keys [key path]}]
  [path key])

(def route-info
  [(->RouteInfo :home       "/"           "Home"       true  true)
   (->RouteInfo :edit-drink "/drink/{id}" "Edit Drink" false false)
   (->RouteInfo :drinks     "/drinks"     "Drinks"     false true)
   (->RouteInfo :login      "/login"      "Login"      true  false)
   (->RouteInfo :logout     "/logout"     "Logout"     false true)
   (->RouteInfo :new-drink  "/drink"      "Add Drink"  false false)
   (->RouteInfo :profile    "/profile"    "Profile"    false true)
   (->RouteInfo :register   "/register"   "Register"   true  false)])

(def routes (mapv to-route route-info))

(def requires-profile? (->> route-info
                            (remove :on-logout?)
                            (map :key)
                            set))

(defn visible-routes
  [logged-in?]
  (->> route-info
       (filter (fn [{:keys [on-login? on-logout?]}]
                     (or
                      (and logged-in? on-login?)
                      (and (not logged-in?) on-logout?))))
       vec))

(def route-text-map (reduce
                     (fn [map {:keys [key label]}] (assoc map key label))
                     {}
                     route-info))

(defn route->text
  [route]
  (get route-text-map route))
