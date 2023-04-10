(ns com.pfeodrippe.wally.webdriver
  (:require
   [clojure.test :refer [deftest testing is use-fixtures]]
   [garden.selectors :as s]
   [wally.main :as w]
   [wally.selectors :as ws]
   [recife.analyzer :as ra]
   [recife.core :as r]
   [recife.helpers :as rh]
   [recife.webdriver :as rw]))

(def global
  {::playing? false})

(r/defproc play
  (fn [{::keys [playing?] :as db}]
    (when (not playing?)
      (assoc db ::playing? true))))

(r/defproc pause
  (fn [{::keys [playing?] :as db}]
    (when playing?
      (assoc db ::playing? false))))

(comment

  @(r/run-model global #{play pause} {:trace-example true})
  (r/states-from-result (r/get-result))
  (r/random-traces-from-result (r/get-result) {:max-number-of-traces 10
                                               :max-number-of-states 10})

  (let [init (fn  [_]
               (w/navigate "file:///Users/paulo.feodrippe/dev/wally-tests/audioplayer.html"))
        procs-mapping {::play
                       {:handler (fn [_]
                                   (w/click :play-pause))}}
        states-mapping {::playing?
                        (fn [aa]
                          (def aa aa)
                          true)}]
    (rw/analyze (r/get-result) {:init init
                                :procs-mapping procs-mapping
                                :states-mapping states-mapping}))

  ())

;; TODO:
;; - [ ] Create spec as in https://docs.quickstrom.io/en/latest/tutorials/first.html
;;   - [x] Play
;;   - [x] Pause
;;   - [ ] Tick
;; - [ ] Wait on event
