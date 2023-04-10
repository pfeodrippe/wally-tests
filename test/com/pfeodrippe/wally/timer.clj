(ns com.pfeodrippe.wally.timer
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :refer [deftest testing is use-fixtures]]
   [garden.selectors :as s]
   [recife.analyzer :as ra]
   [recife.core :as r]
   [recife.helpers :as rh]
   [recife.webdriver :as rw]
   [wally.main :as w]
   [wally.selectors :as ws]))

#_(def global
  {::playing? false
   ::time 0})

#_(r/defproc play
  (fn [{::keys [playing?] :as db}]
    (when (not playing?)
      (assoc db ::playing? true))))

#_(r/defproc pause
  (fn [{::keys [playing?] :as db}]
    (when playing?
      (assoc db ::playing? false))))

#_(r/defproc tick
  (fn [{::keys [playing?] :as db}]
    (when playing?
      (update db ::time inc))))

;; We add a constraint so ◊code{time} does not evolve forever.
#_(rh/defconstraint time-constraint
  [{::keys [time]}]
  (< time 10))

#_(comment

  @(r/run-model global #{play pause tick time-constraint} {:trace-example true})
  (r/random-traces-from-result (r/get-result) {:max-number-of-traces 1
                                               :max-number-of-states 10})

  (let [init (fn  [_]
               (w/navigate (str (io/resource "audioplayer.html"))))

        procs-mapping {::play
                       (fn [_]
                         (w/click :play-pause))

                       ::pause
                       (fn [_]
                         (w/click :play-pause))

                       ::tick
                       (fn [_]
                         (Thread/sleep 1000))}

        states-mapping {::playing?
                        (fn [_ {::keys [playing?]}]
                          (if playing?
                            (is (= "Pause" (w/text-content :play-pause)))
                            (is (= "Play" (w/text-content :play-pause)))))

                        ::time
                        {:check
                         (fn [previous-db db]
                           (if-let [previous-time (-> previous-db :-impl ::time)]
                             (is (<= previous-time (-> db :-impl ::time)))
                             true))

                         :snapshot
                         (fn [_]
                           (let [[minutes seconds] (->> (-> (w/text-content :time-display)
                                                            (str/split #":"))
                                                        (mapv #(Integer/parseInt %)))]
                             (+ (* 60 minutes) seconds)))}}]

    (rw/analyze (r/get-result)
                {:init init
                 :procs-mapping procs-mapping
                 :states-mapping states-mapping
                 :max-number-of-traces 1
                 :max-number-of-states 10}))

  ())

;; TODO:
;; - [ ] Wait on event
;; - [ ] Timer
;;   - https://github.com/quickstrom/quickstrom/blob/main/case-studies/timer.strom#L36
;;   - [ ] Always/eventually
;; - [ ] Could we run it from TLC?
;; - [ ] Show the trace and steps that will be run
;; - [ ] Support other workloads (https://github.com/jepsen-io/maelstrom/blob/main/doc/workloads.md)


;; From https://arxiv.org/pdf/2203.11532.pdf.
;; -------------------------------------------------
;; Currently, the Quickstrom checker makes a completely
;; random selection from the set of allowable actions for the
;; current state. Refining this action selection to be more targeted, methodically exploring previously unreached parts of
;; the state space, is left as future work (see Section 5.1).

;; Check model inference papers on section 5.1 (p. 11)
