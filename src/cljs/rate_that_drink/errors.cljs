(ns rate-that-drink.errors)

(def error-texts
  {::session-expired {:header "Your session has expired"
                      :message "Please log in again."}
   ::unknown         {:header "We are sorry"
                      :message "An unknown error has occurred."}})
