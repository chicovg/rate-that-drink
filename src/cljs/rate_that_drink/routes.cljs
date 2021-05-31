(ns rate-that-drink.routes)

(def route-info
  [[:home     "/"         "Home"]
   [:drinks   "/drinks"   "Drinks"]
   [:login    "/login"    "Login"]
   [:logout   "/logout"   "Logout"]
   [:profile  "/profile"  "Profile"]
   [:register "/register" "Register"]])

(def routes (mapv
             (fn [[key link _]] [link key])
             route-info))

(def route-text-map (reduce
                     (fn [map [key _ label]] (assoc map key label))
                     {}
                     route-info))

(defn route->text
  [route]
  (get route-text-map route))
