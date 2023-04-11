(ns com.pfeodrippe.wally.timer)

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
