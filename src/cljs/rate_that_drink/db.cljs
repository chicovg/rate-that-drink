(ns rate-that-drink.db)

(def initial-db {::drinks           []
                 ::drinks-filter    nil
                 ::drinks-page      0
                 ::drinks-page-size 15
                 ::error            {}
                 ::profile          nil
                 ::loading?         {}
                 ::paging           {}})
